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

package me.proton.core.key.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.key.domain.entity.key.PublicSignedKeyList

@Serializable
data class SignedKeyListRequest(
    @SerialName("Data")
    val data: String,
    @SerialName("Signature")
    val signature: String
) {
    companion object {
        fun from(publicSignedKeyList: PublicSignedKeyList) = SignedKeyListRequest(
            data = checkNotNull(publicSignedKeyList.data) { "Signed key list's data can't be null" },
            signature = checkNotNull(publicSignedKeyList.signature) { "Signed key list's signature can't be null" },
        )
    }
}