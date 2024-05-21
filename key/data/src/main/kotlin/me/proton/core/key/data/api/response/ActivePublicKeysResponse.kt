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

package me.proton.core.key.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivePublicKeysResponse(
    @SerialName("Address")
    val address: AddressDataResponse,
    @SerialName("CatchAll")
    val catchAll: AddressDataResponse? = null,
    @SerialName("Unverified")
    val unverified: AddressDataResponse? = null,
    @SerialName("Warnings")
    val warnings: List<String>,
    @SerialName("ProtonMX")
    val protonMx: Boolean,
    @SerialName("IsProton")
    val isProton: Int
)

@Serializable
data class AddressDataResponse(
    @SerialName("Keys")
    val keys: List<PublicAddressKeyResponse>,
    @SerialName("SignedKeyList")
    val signedKeyList: SignedKeyListResponse? = null
)
