/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.user.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyId

@Entity(
    primaryKeys = ["keyId"],
    indices = [
        Index("userId"),
        Index("keyId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserKeyEntity(
    val userId: UserId,
    val keyId: KeyId,
    val version: Int,
    val privateKey: Armored,
    val isPrimary: Boolean,
    val isUnlockable: Boolean,
    val fingerprint: String? = null,
    val activation: Armored? = null,
    val active: Boolean? = null,
    val recoverySecret: Based64Encoded? = null,
    val recoverySecretSignature: EncryptedSignature? = null
)
