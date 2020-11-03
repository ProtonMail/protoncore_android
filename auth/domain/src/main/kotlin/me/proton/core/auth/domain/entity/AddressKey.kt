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

package me.proton.core.auth.domain.entity

/**
 * Represents address setup (locally generated) key.
 * It is a short version of the [FullAddressKey] that is usually coming from the API.
 * @author Dino Kadrikj.
 */
data class AddressKey(
    val addressId: String,
    val privateKey: String,
    val signedKeyList: SignedKeyList
)

data class SignedKeyList(
    val data: String,
    val signature: String
)
