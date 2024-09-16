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
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
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
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val getEncryptedSecret: GetEncryptedSecret,
) {
    private val keyStoreCrypto = context.keyStoreCrypto

    suspend operator fun invoke(
        userId: UserId,
        passphrase: EncryptedByteArray,
        deviceId: AuthDeviceId? = null,
    ) {
        val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId))
        passphrase.decrypt(keyStoreCrypto).use { decryptedPassphrase ->
            val aesEncryptedSecret = getEncryptedSecret.invoke(decryptedPassphrase, deviceSecret.secret)
            authDeviceRepository.activateDevice(userId, deviceId ?: deviceSecret.deviceId, aesEncryptedSecret)
        }
    }

    suspend operator fun invoke(
        userId: UserId,
        encryptedSecret: AeadEncryptedString
    ) {
        val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId))
        authDeviceRepository.activateDevice(userId, deviceSecret.deviceId, encryptedSecret)
    }
}
