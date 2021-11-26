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
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddressKeyFlags

@Entity(
    primaryKeys = ["keyId"],
    indices = [
        Index("addressId"),
        Index("keyId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AddressEntity::class,
            parentColumns = ["addressId"],
            childColumns = ["addressId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AddressKeyEntity(
    val addressId: AddressId,
    val keyId: KeyId,
    val version: Int,
    val privateKey: Armored,
    val isPrimary: Boolean,
    val isUnlockable: Boolean,
    val flags: UserAddressKeyFlags,
    val passphrase: EncryptedByteArray? = null,
    val token: Armored? = null,
    val signature: Armored? = null,
    val fingerprint: String? = null,
    val fingerprints: List<String>? = null,
    val activation: Armored? = null,
    val active: Boolean
)
