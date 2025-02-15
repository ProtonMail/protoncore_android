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

package me.proton.core.auth.domain.entity

import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId

data class DeviceSecret(
    val userId: UserId,
    val deviceId: AuthDeviceId,
    val secret: DeviceSecretString,
    val token: DeviceTokenString
) {
    companion object {
        const val DEVICE_SECRET_CONTEXT = "account.device-secret"
    }
}

/** base64Encode(random(32bytes)), then encrypted via KeyStore. */
typealias DeviceSecretString = EncryptedString

/** Opaque string obtained from BE, then encrypted via KeyStore. */
typealias DeviceTokenString = EncryptedString
