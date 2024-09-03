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
import me.proton.core.auth.domain.entity.DeviceSecret
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId

@Entity(
    primaryKeys = ["userId"],
    indices = [Index("userId")],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DeviceSecretEntity(
    val userId: UserId,
    val secret: EncryptedString,
    val token: EncryptedString
)

fun DeviceSecretEntity.toDeviceSecret() = DeviceSecret(
    userId = userId,
    secret = secret,
    token = token
)

fun DeviceSecret.toDeviceSecretEntity() = DeviceSecretEntity(
    userId = userId,
    secret = secret,
    token = token
)
