@file:Suppress(
    "EXPERIMENTAL_API_USAGE" // Explicit serializer
)
@file:OptIn(ImplicitReflectionSerializer::class)

package me.proton.core.util.kotlin

import kotlinx.serialization.Decoder
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Encoder
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.Serializer
import kotlinx.serialization.parse
import kotlinx.serialization.parseList
import kotlinx.serialization.parseMap
import kotlinx.serialization.stringify
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
): T = deserializer?.let { Serializer.parse(deserializer, this) } ?: Serializer.parse(this)

/**
 * @return [List] of [T] object from the receiver [String]
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any> String.deserializeList(): List<T> = Serializer.parseList(this)

/**
 * @return [Map] of [T], [V] object from the receiver [String]
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any, reified V : Any> String.deserializeMap(): Map<T, V> =
    Serializer.parseMap(this)


/**
 * @return [String] from the receiver [T] object
 *
 * Note: this function uses reflection as long as explicit [serializer] [SerializationStrategy] is
 * passed explicitly
 */
@NeedSerializable
inline fun <reified T : Any> T.serialize(
    serializer: SerializationStrategy<T>? = null
) = serializer?.let { Serializer.stringify(serializer, this) } ?: Serializer.stringify(this)

/**
 * @return [String] from the receiver [List] of [T] object
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any> List<T>.serialize() = Serializer.stringify(this)

/**
 * @return [String] from the receiver [Map] of [T] and [V] object
 * This uses reflection: TODO improve for avoid it
 */
@NeedSerializable
inline fun <reified T : Any, reified V : Any> Map<T, V>.serialize() = Serializer.stringify(this)


@PublishedApi
internal val Serializer get() = defaultJsonStringFormat


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
        fun build(value: Int) : SerializableTestSealedClass {
            return when (value) {
                1 -> One(value)
                2 -> Two(value)
                else -> unsupported
            }
        }

        @Suppress("ClassName", "ClassNaming") // Test class
        @Serializer(forClass = SerializableTestSealedClass::class)
        object _Serializer : KSerializer<SerializableTestSealedClass> {
            override val descriptor = PrimitiveDescriptor("TestCustomSerializer", PrimitiveKind.STRING)

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
