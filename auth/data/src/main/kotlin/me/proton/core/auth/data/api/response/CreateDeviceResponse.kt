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

package me.proton.core.auth.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.CreatedDevice
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt

@Serializable
data class CreateDeviceResponse(
    @SerialName("Device")
    val device: DeviceResource
) {
    fun toCreatedDevice(context: CryptoContext): CreatedDevice = CreatedDevice(
        deviceId = AuthDeviceId(device.id),
        deviceToken = device.deviceToken.encrypt(context.keyStoreCrypto)
    )
}

@Serializable
data class DeviceResource(
    @SerialName("ID")
    val id: String,
    @SerialName("DeviceToken")
    val deviceToken: String
)
