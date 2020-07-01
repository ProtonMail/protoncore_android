package ch.protonmail.libs.core.utils

import ch.protonmail.libs.testKotlin.`run only on Java 1_8-242`
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Test suite for Serialization utils
 * @author Davide Farella
 */
internal class SerializationUtilsTest {

    @Test
    fun `serialize Serializable annotated class`() = `run only on Java 1_8-242` {
        val input = SerializableTestClass()
        assertEquals(input, input.serialize().deserialize())
    }

    @Test
    fun `serialize list of Serializable annotated class`() = `run only on Java 1_8-242` {
        val input = listOf(SerializableTestClass())
        assertEquals(input, input.serialize().deserializeList())
    }

    @Test
    fun `serialize map of Serializable annotated class`() = `run only on Java 1_8-242` {
        val input = mapOf(1 to SerializableTestClass())
        assertEquals(input, input.serialize().deserializeMap())
    }

    @Test
    fun `serialize sealed classes with explicit serializer`() = `run only on Java 1_8-242` {
        val input = SerializableTestSealedClass.Two(2)
        val s = SerializableTestSealedClass.Companion._Serializer

        val output = input.serialize(s).deserialize(s)
        assertEquals(input.value, output.value)
        assert(output is SerializableTestSealedClass.Two)
    }

    @Test
    fun `serialize sealed classes with implicit serializer`() = `run only on Java 1_8-242` {
        val input = SerializableTestSealedClass.Two(2)

        val output: SerializableTestSealedClass = input.serialize().deserialize()
        assertEquals(input.value, output.value)
        assert(output is SerializableTestSealedClass.Two)
    }
}
