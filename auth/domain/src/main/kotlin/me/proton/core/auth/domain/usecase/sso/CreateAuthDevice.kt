/*
 * Copyright (c) 2024 Proton Technologies AG
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

import me.proton.core.auth.domain.entity.DeviceSecret
import me.proton.core.auth.domain.repository.AuthDeviceRepository
import me.proton.core.auth.domain.repository.DeviceSecretRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

class CreateAuthDevice @Inject constructor(
    private val context: CryptoContext,
    private val addressRepository: UserAddressRepository,
    private val authDeviceRepository: AuthDeviceRepository,
    private val generateDeviceSecret: GenerateDeviceSecret,
    private val deviceSecretRepository: DeviceSecretRepository,
) {
    suspend operator fun invoke(
        userId: UserId,
        deviceName: String
    ) {
        // Fetch via GET /addresses the address keys of the primary address
        val userAddresses = addressRepository.getAddresses(userId, refresh = true)
        val primaryUserAddress = requireNotNull(userAddresses.primary()) {
            "No primary account found."
        }

        // Generate a 32-byte random string DeviceSecret, and encode as base64
        val deviceSecret = generateDeviceSecret()

        // Encrypt the DeviceSecret to the primary address key as ActivationToken
        val activationToken = primaryUserAddress.useKeys(context) {
            encryptText(deviceSecret)
        }

        // Call POST /auth/v4/devices with ActivationToken and obtain a DeviceToken
        val result = authDeviceRepository.createDevice(
            userId = userId,
            deviceName = deviceName,
            activationToken = activationToken
        )

        // Persist DeviceSecret (secret, deviceId, token).
        deviceSecretRepository.upsert(
            DeviceSecret(
                userId = userId,
                deviceId = result.deviceId,
                secret = deviceSecret,
                token = result.deviceToken
            )
        )
    }
}
