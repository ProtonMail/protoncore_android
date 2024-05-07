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

package me.proton.core.auth.data.api.fido2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.account.domain.entity.Fido2RegisteredKey

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class Fido2RegisteredKeyData(
    @SerialName("AttestationFormat")
    val attestationFormat: String,

    @SerialName("CredentialID")
    val credentialID: UByteArray,

    @SerialName("Name")
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Fido2RegisteredKeyData

        if (attestationFormat != other.attestationFormat) return false
        if (!credentialID.contentEquals(other.credentialID)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attestationFormat.hashCode()
        result = 31 * result + credentialID.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    fun toFido2RegisteredKey() = Fido2RegisteredKey(
        attestationFormat = attestationFormat,
        credentialID = credentialID,
        name = name
    )
}
