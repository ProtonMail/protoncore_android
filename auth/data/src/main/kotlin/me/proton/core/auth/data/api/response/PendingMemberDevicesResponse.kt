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
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.entity.MemberDevice
import me.proton.core.auth.domain.entity.MemberDeviceId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

@Serializable
data class PendingMemberDevicesResponse(
    @SerialName("AuthDevices")
    val devices: List<MemberDeviceResource>
)

@Serializable
data class MemberDeviceResource(
    @SerialName("ID")
    val id: String,
    @SerialName("MemberID")
    val memberId: String,
    @SerialName("State")
    val state: Int,
    @SerialName("Name")
    val name: String,
    @SerialName("LocalizedClientName")
    val localizedClientName: String,
    @SerialName("Platform")
    val platform: String?,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("ActivateTime")
    val activateTime: Long?,
    @SerialName("RejectTime")
    val rejectTime: Long?,
    @SerialName("LastActivityTime")
    val lastActivityTime: Long,
    @SerialName("ActivationToken")
    val activationToken: String?,
    @SerialName("ActivationAddressID")
    val activationAddressID: String?
) {
    fun toMemberDevice(userId: UserId): MemberDevice = MemberDevice(
        userId = userId,
        deviceId = MemberDeviceId(id),
        memberId = UserId(memberId),
        name = name,
        localizedClientName = localizedClientName,
        platform = platform,
        createdAtUtcSeconds = createTime,
        activatedAtUtcSeconds = activateTime,
        rejectedAtUtcSeconds = rejectTime,
        lastActivityAtUtcSeconds = lastActivityTime,
        state = AuthDeviceState.map[state] ?: AuthDeviceState.Inactive,
        addressId = activationAddressID?.let { AddressId(it) },
        activationToken = activationToken
    )
}
