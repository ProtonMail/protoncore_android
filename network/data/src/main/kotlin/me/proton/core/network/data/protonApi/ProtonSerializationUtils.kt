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
package me.proton.core.network.data.protonApi

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Kotlin Boolean serializer that can deserialize Boolean from both bool and int json values
 * (where 0 = false). Needs to be explicitly used for the given field with
 * `@Serializable(with=IntToBoolSerializer::class)`
 */
class IntToBoolSerializer : KSerializer<Boolean> {

    override val descriptor =
        PrimitiveSerialDescriptor(IntToBoolSerializer::class.qualifiedName!!, PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Boolean =
        decoder.decodeString().toBooleanFromInt()

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }

    private fun String.toBooleanFromInt() =
        toBoolean() || toIntOrNull()?.let { it > 0 } ?: false
}

