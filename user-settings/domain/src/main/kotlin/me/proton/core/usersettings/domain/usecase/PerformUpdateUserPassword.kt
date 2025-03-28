/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.usersettings.domain.usecase

import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.areAllInactive
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.hasKeys
import me.proton.core.user.domain.extension.nameNotNull
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class PerformUpdateUserPassword @Inject constructor(
    context: CryptoContext,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val userRepository: UserRepository
) {
    private val keyStore = context.keyStoreCrypto
    private val srp = context.srpCrypto

    suspend operator fun invoke(
        twoPasswordMode: Boolean,
        userId: UserId,
        loginPassword: EncryptedString,
        newPassword: EncryptedString,
        secondFactorProof: SecondFactorProof?
    ): Boolean {
        val user = userRepository.getUser(userId)
        val username = user.nameNotNull()
        val account = accountRepository.getAccountOrNull(userId)
        val sessionId = requireNotNull(account?.sessionId)
        val loginInfo = authRepository.getAuthInfoSrp(sessionId, username)
        val modulus = authRepository.randomModulus(sessionId)
        loginPassword.decrypt(keyStore).toByteArray().use { decryptedLoginPassword ->
            newPassword.decrypt(keyStore).toByteArray().use { decryptedNewPassword ->
                val clientProofs: SrpProofs = srp.generateSrpProofs(
                    username = username,
                    password = decryptedLoginPassword.array,
                    version = loginInfo.version.toLong(),
                    salt = loginInfo.salt,
                    modulus = loginInfo.modulus,
                    serverEphemeral = loginInfo.serverEphemeral
                )

                val auth = if (!twoPasswordMode) srp.calculatePasswordVerifier(
                    username = username,
                    password = decryptedNewPassword.array,
                    modulusId = modulus.modulusId,
                    modulus = modulus.modulus
                ) else null

                // Unlock user if locked/inactive.
                if (user.hasKeys() && user.keys.areAllInactive()) {
                    userManager.unlockWithPassword(userId, decryptedLoginPassword)
                }

                return userManager.changePassword(
                    userId = userId,
                    newPassword = newPassword,
                    secondFactorProof = secondFactorProof,
                    proofs = clientProofs,
                    srpSession = loginInfo.srpSession,
                    auth = auth,
                    encryptedSecret = null
                )
            }
        }
    }
}
