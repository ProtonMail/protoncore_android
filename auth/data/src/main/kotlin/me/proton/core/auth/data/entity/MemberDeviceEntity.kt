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

package me.proton.core.auth.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.auth.domain.entity.MemberDevice
import me.proton.core.auth.domain.entity.MemberDeviceId
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

@Entity(
    primaryKeys = ["userId", "deviceId"],
    indices = [
        Index("userId"),
        Index("memberId"),
        Index("addressId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MemberDeviceEntity(
    val userId: UserId,
    val deviceId: String,
    val memberId: UserId,
    val addressId: AddressId?,
    val state: Int, // enum
    val name: String,
    val localizedClientName: String,
    val platform: String?,
    val createdAtUtcSeconds: Long,
    val activatedAtUtcSeconds: Long?,
    val rejectedAtUtcSeconds: Long?,
    val activationToken: EncryptedMessage?,
    val lastActivityAtUtcSeconds: Long
)

fun MemberDeviceEntity.toMemberDevice() = MemberDevice(
    userId = userId,
    deviceId = MemberDeviceId(deviceId),
    memberId = memberId,
    addressId = addressId,
    state = AuthDeviceState.map[state] ?: AuthDeviceState.Inactive,
    name = name,
    localizedClientName = localizedClientName,
    platform = platform,
    createdAtUtcSeconds = createdAtUtcSeconds,
    activatedAtUtcSeconds = activatedAtUtcSeconds,
    rejectedAtUtcSeconds = rejectedAtUtcSeconds,
    activationToken = activationToken,
    lastActivityAtUtcSeconds = lastActivityAtUtcSeconds
)

fun MemberDevice.toMemberDeviceEntity() = MemberDeviceEntity(
    userId = userId,
    deviceId = deviceId.id,
    memberId = memberId,
    addressId = addressId,
    state = state.value,
    name = name,
    localizedClientName = localizedClientName,
    platform = platform,
    createdAtUtcSeconds = createdAtUtcSeconds,
    activatedAtUtcSeconds = activatedAtUtcSeconds,
    rejectedAtUtcSeconds = rejectedAtUtcSeconds,
    activationToken = activationToken,
    lastActivityAtUtcSeconds = lastActivityAtUtcSeconds
)
