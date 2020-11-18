package me.proton.core.util.kotlin

import kotlinx.serialization.Serializable
import me.proton.core.util.testutil.`run only on Java 1_8-242`
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun `serialize regular class vs data class`() = `run only on Java 1_8-242` {

        @Suppress("UseDataClass")
        @Serializable
        class Regular(val name: String, val language: String)

        @Serializable
        data class Data(val name: String, val language: String)

        val inputRegular = Regular(name = "Core", language = "en")
        val inputData = Data(name = "Core", language = "en")

        val output = inputRegular.serialize().split(",")

        // {"name":"Core","language":"en"}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("en"))

        assertEquals(inputData, inputRegular.serialize().deserialize())
    }


    @Test
    fun `serialize null value`() = `run only on Java 1_8-242` {

        @Serializable
        data class Project(val name: String, val language: String?)

        val input = Project(name = "Core", language = null)
        val output = input.serialize().split(",")

        // {"name":"Core","language":null}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("null"))

        assertEquals(input, input.serialize().deserialize())
    }

    @Test
    fun `serialize default value`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = true.

        @Serializable
        data class Project(val name: String, val language: String = "en")

        val input = Project(name = "Core")
        val output = input.serialize().split(",")

        // {"name":"Core","language":"en"}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("en"))

        assertEquals(input, input.serialize().deserialize())
    }

    @Test
    fun `serialize default null value`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = true.

        @Serializable
        data class Project(val name: String, val language: String? = null)

        val input = Project(name = "Core")
        val output = input.serialize().split(",")

        // {"name":"Core","language":"null"}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("null"))

        assertEquals(input, input.serialize().deserialize())
    }
}
