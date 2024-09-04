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

import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

class CreateAuthDevice @Inject constructor(
    private val addressRepository: UserAddressRepository,
    private val authRepository: AuthRepository,
    private val context: CryptoContext
) {
    suspend operator fun invoke(
        userId: UserId,
        deviceName: String
    ): String {
        val pgp = context.pgpCrypto
        // 1. Fetch via GET /addresses the address keys of the primary address
        val userAddresses = addressRepository.getAddresses(userId, refresh = true)
        val primaryUserAddress = requireNotNull(userAddresses.primary()) {
            "No primary account found."
        }

        // 2. Generate a 32-byte random string DeviceSecret, and encode as base64
        val deviceSecret = pgp.generateNewDeviceSecret()

        // 3. Encrypt the DeviceSecret to the primary address key as ActivationToken
        val activationToken =
            // todo: validate with key transparency?
            primaryUserAddress.useKeys(context) {
                encryptText(deviceSecret)
        }

        // 4. Call POST /auth/v4/devices with ActivationToken and obtain a DeviceToken
        val result =
            authRepository.initDevice(sessionUserId = userId, name = deviceName, activationToken = activationToken)
        val deviceToken = result.token
        // todo: 5. store the device token in the DB for later use
        return deviceToken
    }
}