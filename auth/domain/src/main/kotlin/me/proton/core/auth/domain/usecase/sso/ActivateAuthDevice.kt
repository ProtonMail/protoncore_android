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

import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.aead.encrypt
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.use
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.extension.getByKeyId
import me.proton.core.key.domain.extension.primary
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Upload new EncryptedSecret (derived from password), and activate device (final step of LoginWithBackup).
 *
 * When: We can decrypt the keys, but we have a local invalid secret.
 *
 * @see RejectAuthDevice
 */
class ActivateAuthDevice @Inject constructor(
    context: CryptoContext,
    private val userRepository: UserRepository,
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val keySaltRepository: KeySaltRepository,
) {
    private val keyStoreCrypto = context.keyStoreCrypto
    private val aeadCrypto = context.aeadCrypto
    private val pgpCrypto = context.pgpCrypto

    suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId,
        password: EncryptedString
    ) {
        val user = userRepository.getUser(userId)
        val userPrimaryKey = requireNotNull(user.keys.primary())
        val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId))
        val salts = keySaltRepository.getKeySalts(userId)
        val primaryKeySalt = requireNotNull(salts.getByKeyId(userPrimaryKey.keyId))
        password.decrypt(keyStoreCrypto).toByteArray().use { decryptedPassword ->
            pgpCrypto.getPassphrase(decryptedPassword.array, primaryKeySalt).use { passphrase ->
                pgpCrypto.getBase64Decoded(deviceSecret.secret.decrypt(keyStoreCrypto))
                    .use { key -> passphrase.encrypt(aeadCrypto, key = key.array).array }
                    .use { encryptedSecret ->
                        val base64Encoded = pgpCrypto.getBase64Encoded(encryptedSecret.array)
                        authDeviceRepository.activateDevice(userId, deviceId, base64Encoded)
                    }
            }
        }
    }
}
