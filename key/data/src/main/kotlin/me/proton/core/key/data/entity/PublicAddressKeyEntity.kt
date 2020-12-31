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

package me.proton.core.key.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import me.proton.core.crypto.common.pgp.Armored

@Entity(
    indices = [
        Index("email")
    ],
    foreignKeys = [
        ForeignKey(
            entity = PublicAddressEntity::class,
            parentColumns = ["email"],
            childColumns = ["email"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PublicAddressKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val flags: Int,
    val publicKey: Armored,
    val isPrimary: Boolean
)
