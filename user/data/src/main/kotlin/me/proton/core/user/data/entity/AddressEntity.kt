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

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.entity.SignedKeyListEntity
import me.proton.core.key.domain.entity.key.PublicSignedKeyList
import me.proton.core.user.domain.entity.AddressId

@Entity(
    primaryKeys = ["addressId"],
    indices = [
        Index("addressId"),
        Index("userId")
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
data class AddressEntity(
    val userId: UserId,
    val addressId: AddressId,
    val email: String,
    val displayName: String? = null,
    val signature: String? = null,
    val domainId: String? = null,
    val canSend: Boolean,
    val canReceive: Boolean,
    val enabled: Boolean,
    val type: Int?,
    val order: Int,
    @Embedded(prefix = "signedKeyList_")
    val signedKeyList: SignedKeyListEntity?
)
