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
import me.proton.core.key.data.extension.toPublicSignedKeyList
import me.proton.core.key.domain.entity.key.PublicAddress
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicAddressKeyFlags
import me.proton.core.key.domain.entity.key.PublicAddressKeySource
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.isCompromised
import me.proton.core.key.domain.entity.key.isObsolete

@Serializable
@Deprecated("Deprecated on BE.")
data class PublicAddressKeysResponse(
    @SerialName("RecipientType")
    val recipientType: Int = 0,
    @SerialName("MIMEType")
    val mimeType: String? = null,
    @SerialName("Keys")
    val keys: List<PublicAddressKeyResponse>? = null,
    @SerialName("SignedKeyList")
    val signedKeyList: SignedKeyListResponse? = null,
    @SerialName("IgnoreKT")
    val ignoreKT: Int? = null
) {
    fun toPublicAddress(email: String) = PublicAddress(
        email = email,
        recipientType = recipientType,
        mimeType = mimeType,
        keys = keys.orEmpty().mapIndexed { index, response -> response.toPublicAddressKey(email, index == 0) },
        signedKeyList = signedKeyList?.toPublicSignedKeyList(),
        ignoreKT = ignoreKT
    )
}

@Serializable
data class PublicAddressKeyResponse(
    @SerialName("Flags")
    val flags: PublicAddressKeyFlags,
    @SerialName("PublicKey")
    val publicKey: String,
    @SerialName("Source")
    val source: Int? = null
) {
    fun toPublicAddressKey(email: String, isPrimary: Boolean) = PublicAddressKey(
        email = email,
        flags = flags,
        publicKey = PublicKey(
            key = publicKey,
            isPrimary = isPrimary,
            isActive = true,
            canEncrypt = flags.isObsolete().not(),
            canVerify = flags.isCompromised().not()
        ),
        source = PublicAddressKeySource.fromCode(source)
    )
}
