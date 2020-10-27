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

package me.proton.core.auth.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.Address
import me.proton.core.auth.domain.entity.AddressType
import me.proton.core.util.kotlin.toBoolean

/**
 * @author Dino Kadrikj.
 */
@Serializable
data class AddressEntity(
    @SerialName("ID")
    val id: String,
    @SerialName("DomainID")
    val domainId: String,
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
    val displayName: String,
    @SerialName("Signature")
    val signature: String,
    @SerialName("HasKeys")
    val hasKeys: Int, // boolean (binary)
    @SerialName("Keys")
    val keys: List<FullAddressKeyEntity>
) {
    fun toAddress(): Address =
        Address(
            id = id,
            domainId = domainId,
            email = email,
            canSend = send.toBoolean(),
            canReceive = receive.toBoolean(),
            status = status,
            type = AddressType.getByValue(type),
            order = order,
            displayName = displayName,
            signature = signature,
            hasKeys = hasKeys.toBoolean(),
            keys = keys.map {
                it.toAddressKey()
            }
        )
}
