package me.proton.core.util.kotlin

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.util.testutil.`run only on Java 1_8-242`
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Suppress("UseDataClass", "unused")
    @Serializable
    class Regular(val name: String, val language: String, val country: String?, val level: String? = null)

    @Serializable
    data class Data(val name: String, val language: String, val country: String?, val level: String? = null)

    @Test
    fun `serialize regular class vs data class`() = `run only on Java 1_8-242` {

        val inputRegular = Regular(name = "Core", language = "en", country = null)
        val inputData = Data(name = "Core", language = "en", country = null)

        val output = inputRegular.serialize().split(",")

        // Expected: {"name":"Core","language":"en","country":null}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("en"))

        assertTrue(output[2].contains("country"))
        assertTrue(output[2].contains("null"))

        assertFalse(output.contains("level"))

        assertEquals(inputData, inputRegular.serialize().deserialize())
    }

    @Serializable
    data class ProjectSerializeNullValue(val name: String, val language: String?)

    @Test
    fun `serialize null value`() = `run only on Java 1_8-242` {

        val input = ProjectSerializeNullValue(name = "Core", language = null)
        val output = input.serialize().split(",")

        // Expected: {"name":"Core","language":null}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("null"))

        assertEquals(input, input.serialize().deserialize())
    }

    @Serializable
    data class ProjectDoNotSerializeDefaultValue(val name: String = "default", val language: String = "en")

    @Test
    fun `DO NOT serialize default value`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = false.

        val input = ProjectDoNotSerializeDefaultValue(name = "Core")
        val output = input.serialize().split(",")

        // Expected: {"name":"Core"}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertFalse(output.contains("language"))
        assertFalse(output.contains("en"))

        assertEquals(input, input.serialize().deserialize())
    }


    @Serializable
    data class ProjectSerializeDefaultValueRequired(val name: String, @Required val language: String = "en")

    @Test
    fun `serialize default value if required`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = false.

        val input = ProjectSerializeDefaultValueRequired(name = "Core")
        val output = input.serialize().split(",")

        // {"name":"Core","language":"en"}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertTrue(output[1].contains("language"))
        assertTrue(output[1].contains("en"))

        assertEquals(input, input.serialize().deserialize())
    }


    @Serializable
    data class ProjectDoNotSerializeDefaultNullValue(val name: String? = null, val language: String? = null)

    @Test
    fun `DO NOT serialize default null value`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = false.

        val input = ProjectDoNotSerializeDefaultNullValue(name = "Core")
        val output = input.serialize().split(",")

        // {"name":"Core"}

        assertTrue(output[0].contains("name"))
        assertTrue(output[0].contains("Core"))

        assertFalse(output.contains("language"))
        assertFalse(output.contains("null"))

        assertEquals(input, input.serialize().deserialize())
    }

    @Serializable
    data class ProjectDeserializeNullValue(val name: String? = null, val language: String? = null)

    @Test
    fun `deserialize default null value correctly`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = false.

        val input = "{\"name\":\"Core\"}"

        val expected = ProjectDeserializeNullValue(name = "Core")

        assertEquals(expected, input.deserialize())
    }


    @Serializable
    data class ProjectDeserializeDefaultValueCorrectly(val name: String? = null, val language: String? = null)

    @Test
    fun `deserialize default value correctly`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = false.

        val input = "{\"name\":\"Core\",\"language\":\"en\"}"

        val expected = ProjectDeserializeDefaultValueCorrectly(name = "Core", language = "en")

        assertEquals(expected, input.deserialize())
    }


    @Serializable
    data class ProjectDeserializeProvidedNullableValue(val name: String? = null, val language: String?)

    @Test
    fun `deserialize provided nullable value`() = `run only on Java 1_8-242` {

        // See ProtonCoreConfig.defaultJsonStringFormat -> encodeDefaults = false.

        val input = "{\"name\":\"Core\",\"language\":\"en\"}"

        val expected = ProjectDeserializeProvidedNullableValue(name = "Core", language = "en")

        assertEquals(expected, input.deserialize())
    }
}
