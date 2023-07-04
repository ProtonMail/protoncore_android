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

package me.proton.core.util.kotlin

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.Serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import me.proton.core.util.kotlin.ProtonCoreConfig.defaultJson
import me.proton.core.util.kotlin.ProtonCoreConfig.defaultJsonStringFormat

/*
 * Utilities for Serialization
 * Those utils uses kotlin-serialization
 *
 * Author: Davide Farella
 */

/**
 * Elements marked with this annotation are expecting to have a parameter which is annotated with [Serializable]
 * annotation; an exception will be thrown otherwise.
 *
 * [Target] has been set to be [AnnotationTarget.FUNCTION] because [AnnotationTarget.TYPE_PARAMETER] is not supported
 * yet, but in future we are expecting targets to be [AnnotationTarget.TYPE_PARAMETER] and
 * [AnnotationTarget.VALUE_PARAMETER]
 */
@Target(AnnotationTarget.FUNCTION)
annotation class NeedSerializable

/**
 * @return [T] object from the receiver [String]
 *
 * Note: this function uses reflection as long as explicit [deserializer] [DeserializationStrategy]
 * is passed explicitly
 */
@NeedSerializable
inline fun <reified T : Any> String.deserialize(
    deserializer: DeserializationStrategy<T>? = null
): T =
    deserializer
        ?.let { Serializer.decodeFromString(deserializer, this) }
        ?: Serializer.decodeFromString(this)

/**
 * @return [T] object from the receiver [String] or null if receiver can't be deserialized to [T].
 *
 * Note: this function uses reflection as long as explicit [deserializer] [DeserializationStrategy]
 * is passed explicitly
 */
@NeedSerializable
inline fun <reified T : Any> String.deserializeOrNull(
    deserializer: DeserializationStrategy<T>? = null
): T? = try {
    deserialize(deserializer)
} catch (e: SerializationException) {
    null
}

/**
 * @return [List] of [T] object from the receiver [String]
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any> String.deserializeList(): List<T> = Serializer.decodeFromString(this)

/**
 * @return [Map] of [T], [V] object from the receiver [String]
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any, reified V : Any> String.deserializeMap(): Map<T, V> =
    Serializer.decodeFromString(this)


/**
 * @return [String] from the receiver [T] object
 *
 * Note: this function uses reflection as long as explicit [serializer] [SerializationStrategy] is
 * passed explicitly
 */
@NeedSerializable
inline fun <reified T : Any> T.serialize(
    serializer: SerializationStrategy<T>? = null
) = serializer?.let { Serializer.encodeToString(serializer, this) } ?: Serializer.encodeToString(this)

/**
 * @return [String] from the receiver [List] of [T] object
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any> List<T>.serialize() = Serializer.encodeToString(this)

/**
 * @return [String] from the receiver [Map] of [T] and [V] object
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any, reified V : Any> Map<T, V>.serialize() = Serializer.encodeToString(this)

@NeedSerializable
inline fun <reified T> T.serializeToJsonElement(
    serializer: SerializationStrategy<T>? = null
): JsonElement =
    serializer?.let { JsonSerializer.encodeToJsonElement(serializer, this) } ?: JsonSerializer.encodeToJsonElement(this)


@PublishedApi
internal val Serializer get() = defaultJsonStringFormat

@PublishedApi
internal val JsonSerializer get() = defaultJson


// region TEST ONLY
@Serializable
internal data class SerializableTestClass(val number: Int = 0)

@Serializable(with = SerializableTestSealedClass.Companion._Serializer::class)
internal sealed class SerializableTestSealedClass(val value: Int) {

    @Serializable(with = _Serializer::class)
    class One(value: Int) : SerializableTestSealedClass(value)

    @Serializable(with = _Serializer::class)
    class Two(value: Int) : SerializableTestSealedClass(value)

    companion object {
        fun build(value: Int): SerializableTestSealedClass {
            return when (value) {
                1 -> One(value)
                2 -> Two(value)
                else -> unsupported
            }
        }

        @Suppress("ClassName", "ClassNaming") // Test class
        @Serializer(forClass = SerializableTestSealedClass::class)
        object _Serializer : KSerializer<SerializableTestSealedClass> {
            override val descriptor = PrimitiveSerialDescriptor("TestCustomSerializer", PrimitiveKind.STRING)

            override fun serialize(encoder: Encoder, value: SerializableTestSealedClass) {
                val raw = Raw(value.value)
                encoder.encodeString(raw.serialize())
            }

            override fun deserialize(decoder: Decoder): SerializableTestSealedClass {
                val raw = decoder.decodeString().deserialize<Raw>()
                return build(raw.value)
            }
        }
    }

    @Serializable
    data class Raw(val value: Int)
}
// endregion
