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

package me.proton.core.user.domain.entity

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.keyholder.KeyHolderPrivateKey

data class UserAddressKey(
    val addressId: AddressId,
    val version: Int,
    val flags: UserAddressKeyFlags,
    val token: Armored? = null,
    val signature: Armored? = null,
    val activation: Armored? = null,
    val active: Boolean,
    override val keyId: KeyId,
    override val privateKey: PrivateKey
) : KeyHolderPrivateKey

/**
 * Flags (bitmap): 1: Can use key to verify signatures, 2: Can use key to encrypt new data.
 */
typealias UserAddressKeyFlags = Int
