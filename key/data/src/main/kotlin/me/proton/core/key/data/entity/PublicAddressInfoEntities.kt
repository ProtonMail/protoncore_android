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

package me.proton.core.key.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.PublicAddressKeyFlags

data class PublicAddressInfoWithKeys(
    @Embedded
    val entity: PublicAddressInfoEntity,
    @Relation(
        parentColumn = "email",
        entityColumn = "email"
    )
    val keys: List<PublicAddressKeyDataEntity>
)

@Entity(
    primaryKeys = ["email"],
    indices = [
        Index("email")
    ]
)
data class PublicAddressInfoEntity(
    val email: String,
    val warnings: List<String>,
    val protonMx: Boolean,
    val isProton: Int,
    @Embedded(prefix = "addressSignedKeyList_")
    val addressSignedKeyList: SignedKeyListEntity?,
    @Embedded(prefix = "catchAllSignedKeyList_")
    val catchAllSignedKeyList: SignedKeyListEntity?,
)

@Entity(
    primaryKeys = ["email", "publicKey"],
    indices = [
        Index("email")
    ],
    foreignKeys = [
        ForeignKey(
            entity = PublicAddressInfoEntity::class,
            parentColumns = ["email"],
            childColumns = ["email"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PublicAddressKeyDataEntity(
    val email: String,
    val emailAddressType: Int,
    val flags: PublicAddressKeyFlags,
    val publicKey: Armored,
    val isPrimary: Boolean,
    val source: Int?,
)

object EmailAddressType {
    const val REGULAR = 0
    const val CATCH_ALL = 1
    const val UNVERIFIED = 2
}
