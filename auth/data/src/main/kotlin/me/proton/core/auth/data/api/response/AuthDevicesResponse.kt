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
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

@Serializable
data class AuthDevicesResponse(
    @SerialName("Devices")
    val devices: List<AuthDeviceResponse>
)

@Serializable
data class AuthDeviceResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("State")
    val state: Int,
    @SerialName("Name")
    val name: String,
    @SerialName("LocalizedClientName")
    val localizedClientName: String, // todo: double check
    @SerialName("CreateTime")
    val createTime: Long, // todo: double check
    @SerialName("ActivateTime")
    val activateTime: Long?, // todo: double check
    @SerialName("RejectTime")
    val rejectTime: Long?, // todo: double check
    @SerialName("LastActivityTime")
    val lastActivityTime: Long, // todo: double check
    @SerialName("ActivationToken")
    val activationToken: String?,
    @SerialName("ActivationAddressID")
    val activationAddressID: String?
) {
    fun toAuthDevice(userId: UserId): AuthDevice {
        return AuthDevice(
            userId = userId,
            deviceId = AuthDeviceId(id),
            name = name,
            localizedClientName = localizedClientName,
            createdAtUtcSeconds = createTime,
            activatedAtUtcSeconds = activateTime,
            rejectedAtUtcSeconds = rejectTime,
            lastActivityAtUtcSeconds = lastActivityTime,
            state = AuthDeviceState.map[state] ?: AuthDeviceState.Inactive, // todo: inactive?
            addressId = activationAddressID?.let { AddressId(it) },
            activationToken = activationToken
        )
    }
}