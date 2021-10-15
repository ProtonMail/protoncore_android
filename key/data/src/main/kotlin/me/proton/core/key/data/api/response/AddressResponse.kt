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

package me.proton.core.key.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressesResponse(
    @SerialName("Addresses")
    val addresses: List<AddressResponse>
)

@Serializable
data class SingleAddressResponse(
    @SerialName("Address")
    val address: AddressResponse
)

@Serializable
data class AddressResponse(
    @SerialName("ID")
    val id: String,
    @SerialName("DomainID")
    val domainId: String? = null,
    @SerialName("Email")
    val email: String,
    @SerialName("Send")
    val send: Int, // boolean (binary)
    @SerialName("Receive")
    val receive: Int, // boolean (binary)
    @SerialName("Status")
    val status: Int,
    @SerialName("Type")
    val type: Int,
    @SerialName("Order")
    val order: Int,
    @SerialName("DisplayName")
    val displayName: String? = null,
    @SerialName("Signature")
    val signature: String? = null,
    @SerialName("HasKeys")
    val hasKeys: Int, // boolean (binary)
    @SerialName("Keys")
    val keys: List<AddressKeyResponse>? = null,
    @SerialName("SignedKeyList")
    val signedKeyList: SignedKeyListResponse? = null
)
