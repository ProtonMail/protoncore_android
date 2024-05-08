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

package me.proton.core.userrecovery.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.domain.entity.UserId
import me.proton.core.user.data.entity.UserEntity
import me.proton.core.userrecovery.domain.entity.RecoveryFile

@Entity(
    primaryKeys = ["recoverySecretHash"],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId")
    ]
)
data class RecoveryFileEntity(
    val userId: UserId,
    val createdAtUtcMillis: Long,
    val recoveryFile: String,
    val recoverySecretHash: String
)

fun RecoveryFileEntity.toRecoveryFile(): RecoveryFile = RecoveryFile(
    userId = userId,
    createdAtUtcMillis = createdAtUtcMillis,
    recoveryFile = recoveryFile,
    recoverySecretHash = recoverySecretHash
)

fun RecoveryFile.toRecoveryFileEntity(): RecoveryFileEntity = RecoveryFileEntity(
    userId = userId,
    createdAtUtcMillis = createdAtUtcMillis,
    recoveryFile = recoveryFile,
    recoverySecretHash = recoverySecretHash
)
