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
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey

@Serializable
data class PublicAddressKeysResponse(
    @SerialName("RecipientType")
    val recipientType: Int = 0,
    @SerialName("MIMEType")
    val mimeType: String? = null,
    @SerialName("Keys")
    val keys: List<PublicAddressKeyResponse>? = null
) {
    fun toPublicAddress(email: String) = PublicAddress(
        email = email,
        recipientType = recipientType,
        mimeType = mimeType,
        keys = keys.orEmpty().mapIndexed { index, response -> response.toPublicAddressKey(email, index == 0) }
    )
}

@Serializable
data class PublicAddressKeyResponse(
    @SerialName("Flags")
    val flags: Int,
    @SerialName("PublicKey")
    val publicKey: String
) {
    fun toPublicAddressKey(email: String, isPrimary: Boolean) = PublicAddressKey(
        email = email,
        flags = flags,
        publicKey = PublicKey(publicKey, isPrimary)
    )
}
