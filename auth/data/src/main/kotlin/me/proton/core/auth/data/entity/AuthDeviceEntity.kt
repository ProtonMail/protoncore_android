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

package me.proton.core.auth.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.account.data.entity.AccountEntity
import me.proton.core.auth.domain.entity.AuthDevice
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import me.proton.core.auth.domain.entity.AuthDeviceState
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId

@Entity(
    primaryKeys = ["userId", "deviceId"],
    indices = [
        Index("userId"),
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
data class AuthDeviceEntity(
    val userId: UserId,
    val deviceId: String,
    val addressId: AddressId?,
    val state: Int,
    val name: String,
    val localizedClientName: String,
    val platform: String?,
    val createdAtUtcSeconds: Long,
    val activatedAtUtcSeconds: Long?,
    val rejectedAtUtcSeconds: Long?,
    val activationToken: EncryptedMessage?,
    val lastActivityAtUtcSeconds: Long,
)

fun AuthDeviceEntity.toAuthDevice() = AuthDevice(
    userId = userId,
    deviceId = AuthDeviceId(deviceId),
    addressId = addressId,
    state = AuthDeviceState.map[state] ?: AuthDeviceState.Inactive,
    name = name,
    localizedClientName = localizedClientName,
    platform = AuthDevicePlatform.enumOf(platform),
    createdAtUtcSeconds = createdAtUtcSeconds,
    activatedAtUtcSeconds = activatedAtUtcSeconds,
    rejectedAtUtcSeconds = rejectedAtUtcSeconds,
    activationToken = activationToken,
    lastActivityAtUtcSeconds = lastActivityAtUtcSeconds
)

fun AuthDevice.toAuthDeviceEntity() = AuthDeviceEntity(
    userId = userId,
    deviceId = deviceId.id,
    addressId = addressId,
    state = state.value,
    name = name,
    localizedClientName = localizedClientName,
    platform = platform?.value,
    createdAtUtcSeconds = createdAtUtcSeconds,
    activatedAtUtcSeconds = activatedAtUtcSeconds,
    rejectedAtUtcSeconds = rejectedAtUtcSeconds,
    activationToken = activationToken,
    lastActivityAtUtcSeconds = lastActivityAtUtcSeconds
)
