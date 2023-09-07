/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.usersettings.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity

@Entity(
    primaryKeys = ["userId"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserSettingsEntity(
    val userId: UserId,
    @Embedded(prefix = "email_")
    val email: RecoverySettingEntity?,
    @Embedded(prefix = "phone_")
    val phone: RecoverySettingEntity?,
    @Embedded(prefix = "password_")
    val password: PasswordEntity,
    @Embedded(prefix = "twoFA_")
    val twoFA: TwoFAEntity?,
    val news: Int?,
    val locale: String?,
    val logAuth: Int?,
    val density: Int?,
    val weekStart: Int?,
    val dateFormat: Int?,
    val timeFormat: Int?,
    val earlyAccess: Boolean?,
    val telemetry: Boolean?,
)

data class RecoverySettingEntity(
    val value: String?,
    val status: Int?,
    val notify: Int?,
    val reset: Int?
)

data class PasswordEntity(
    val mode: Int?,
    val expirationTime: Int?
)

data class TwoFAEntity(
    val enabled: Int?,
    val allowed: Int?,
    val expirationTime: Int?,
)

@Serializable
data class RegisteredKeyEntity(
    val attestationFormat: String,
    val credentialID: List<Int>,
    val name: String,
)

data class FlagsEntity(
    val welcomed: Boolean?
)
