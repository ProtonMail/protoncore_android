/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.domain.usecase.sso

import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.nameNotNull
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class ChangeBackupPassword @Inject constructor(
    context: CryptoContext,
    private val accountRepository: AccountRepository,
    private val authRepository: AuthRepository,
    private val userManager: UserManager,
    private val userRepository: UserRepository,
    private val passphraseRepository: PassphraseRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val getEncryptedSecret: GetEncryptedSecret,
) {
    private val keyStore = context.keyStoreCrypto
    private val srp = context.srpCrypto

    suspend operator fun invoke(
        userId: UserId,
        newBackupPassword: EncryptedString,
    ): Boolean {
        val user = userRepository.getUser(userId)
        val username = user.nameNotNull()
        val account = accountRepository.getAccountOrNull(userId)
        val sessionId = requireNotNull(account?.sessionId)
        val modulus = authRepository.randomModulus(sessionId)
        val currentPassphrase = requireNotNull(passphraseRepository.getPassphrase(userId))
        val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId)?.secret)
        currentPassphrase.decrypt(keyStore).use { decryptedCurrentPassphrase ->
            newBackupPassword.decrypt(keyStore).toByteArray().use { decryptedBackupPassword ->
                val auth = srp.calculatePasswordVerifier(
                    username = username,
                    password = decryptedBackupPassword.array,
                    modulusId = modulus.modulusId,
                    modulus = modulus.modulus
                )
                return userManager.changePassword(
                    userId = userId,
                    newPassword = newBackupPassword,
                    secondFactorProof = null,
                    proofs = null,
                    srpSession = null,
                    auth = auth,
                    encryptedSecret = getEncryptedSecret.invoke(
                        passphrase = decryptedCurrentPassphrase,
                        deviceSecret = deviceSecret
                    )
                )
            }
        }
    }
}
