/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.ui

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class VerificationResponseMessage(
    val type: Type,
    val payload: Payload? = null,
) {

    @Serializable
    data class Payload(
        val type: String? = null,
        val text: String? = null,
        val token: String? = null,
        val height: Int? = null,
    )

    @Serializable(with = VerificationMessageTypeSerializer::class)
    enum class Type(val value: String) {
        Success("HUMAN_VERIFICATION_SUCCESS"),
        Notification("NOTIFICATION"),
        Resize("RESIZE"),
        Loaded("LOADED");

        companion object {
            val map = values().associateBy { it.value }
        }
    }

    enum class MessageType(val value: String) {
        Success("success"),
        Info("info"),
        Warning("warning"),
        Error("error");

        companion object {
            val map = values().associateBy { it.value }
        }
    }

    @Serializer(forClass = Type::class)
    object VerificationMessageTypeSerializer {
        override val descriptor = PrimitiveSerialDescriptor(
            "VerificationMessageTypeDescriptor",
            PrimitiveKind.STRING
        )
        override fun deserialize(decoder: Decoder): Type = Type.map[decoder.decodeString()]
            ?: throw SerializationException("Invalid value for VerificationMessage.Type: ${decoder.decodeString()}")
        override fun serialize(encoder: Encoder, value: Type) = encoder.encodeString(value.value)
    }
}
