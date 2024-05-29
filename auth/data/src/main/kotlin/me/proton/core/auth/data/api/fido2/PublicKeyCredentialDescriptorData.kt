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

import kotlinx.serialization.Serializable
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialDescriptor

@OptIn(ExperimentalUnsignedTypes::class)
@Serializable
data class PublicKeyCredentialDescriptorData(
    val type: String,
    val id: UByteArray,
    val transports: List<String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicKeyCredentialDescriptorData

        if (type != other.type) return false
        if (!id.contentEquals(other.id)) return false
        if (transports != other.transports) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + id.contentHashCode()
        result = 31 * result + (transports?.hashCode() ?: 0)
        return result
    }

    fun toFido2PublicKeyCredentialDescriptor() = Fido2PublicKeyCredentialDescriptor(
        type = type,
        id = id,
        transports = transports
    )
}