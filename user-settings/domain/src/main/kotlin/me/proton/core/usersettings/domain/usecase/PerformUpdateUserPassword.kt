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

import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.decryptWith
import me.proton.core.crypto.common.keystore.use
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.hasSubscription
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.repository.OrganizationRepository
import javax.inject.Inject

class PerformUpdateUserPassword @Inject constructor(
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    private val organizationRepository: OrganizationRepository,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val cryptoContext: CryptoContext,
    @ClientSecret private val clientSecret: String
) {
    suspend operator fun invoke(
        twoPasswordMode: Boolean,
        userId: UserId,
        loginPassword: EncryptedString,
        newPassword: EncryptedString,
        secondFactorCode: String = ""
    ): Boolean {
        val user = userRepository.getUser(userId)
        val username = requireNotNull(user.name ?: user.email)
        val paid = user.hasSubscription()

        val loginInfo = authRepository.getLoginInfo(
            username = username,
            clientSecret = clientSecret
        )
        val modulus = authRepository.randomModulus()

        val organizationKeys = if (paid) {
            organizationRepository.getOrganizationKeys(userId)
        } else null

        loginPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedLoginPassword ->
            newPassword.decryptWith(keyStoreCrypto).toByteArray().use { decryptedNewPassphrase ->
                val clientProofs: SrpProofs = cryptoContext.srpCrypto.generateSrpProofs(
                    username = username,
                    password = decryptedLoginPassword.array,
                    version = loginInfo.version.toLong(),
                    salt = loginInfo.salt,
                    modulus = loginInfo.modulus,
                    serverEphemeral = loginInfo.serverEphemeral
                )

                val auth = if (!twoPasswordMode) cryptoContext.srpCrypto.calculatePasswordVerifier(
                    username = username,
                    password = decryptedNewPassphrase.array,
                    modulusId = modulus.modulusId,
                    modulus = modulus.modulus
                ) else null

                return userManager.changePassword(
                    userId = userId,
                    oldPassword = loginPassword,
                    newPassword = newPassword,
                    secondFactorCode = secondFactorCode,
                    proofs = clientProofs,
                    srpSession = loginInfo.srpSession,
                    auth = auth,
                    organizationPrivateKey = organizationKeys?.privateKey
                )
            }
        }
    }
}
