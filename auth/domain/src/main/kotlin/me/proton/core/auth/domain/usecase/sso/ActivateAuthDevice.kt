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
import me.proton.core.auth.domain.entity.DeviceSecretString
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.aead.AeadEncryptedString
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.extension.suspend
import javax.inject.Inject

/**
 * Activate device, by upload new EncryptedSecret.
 *
 * @see RejectAuthDevice
 */
class ActivateAuthDevice @Inject constructor(
    private val context: CryptoContext,
    private val authDeviceRepository: AuthDeviceRepository,
    private val deviceSecretRepository: DeviceSecretRepository,
    private val getEncryptedSecret: GetEncryptedSecret,
    private val eventManagerProvider: EventManagerProvider,
) {

    private suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId,
        encryptedSecret: AeadEncryptedString
    ) {
        authDeviceRepository.activateDevice(userId, deviceId, encryptedSecret)
    }

    private suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId,
        passphrase: EncryptedByteArray,
        deviceSecret: DeviceSecretString,
    ) {
        passphrase.decrypt(context.keyStoreCrypto).use { decryptedPassphrase ->
            val aesEncryptedSecret = getEncryptedSecret.invoke(decryptedPassphrase, deviceSecret)
            invoke(userId, deviceId, aesEncryptedSecret)
        }
    }

    /** Activate my local device using AeadEncryptedString. */
    suspend operator fun invoke(
        userId: UserId,
        encryptedSecret: AeadEncryptedString
    ) {
        val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId))
        invoke(userId, deviceSecret.deviceId, encryptedSecret)
    }

    /** Activate my local device using EncryptedByteArray. */
    suspend operator fun invoke(
        userId: UserId,
        passphrase: EncryptedByteArray
    ) {
        val deviceSecret = requireNotNull(deviceSecretRepository.getByUserId(userId))
        invoke(userId, deviceSecret.deviceId, passphrase, deviceSecret.secret)
    }

    /** Activate another device. */
    suspend operator fun invoke(
        userId: UserId,
        deviceId: AuthDeviceId,
        deviceSecret: DeviceSecretString,
        passphrase: EncryptedByteArray,
    ) {
        // Force Event Loop to update Pushes/Notifications.
        eventManagerProvider.suspend(EventManagerConfig.Core(userId)) {
            invoke(userId, deviceId, passphrase, deviceSecret)
        }
    }
}
