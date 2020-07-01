package me.proton.core.util.android.sharedpreferences

import me.proton.core.test.android.mocks.mockSharedPreferences
import me.proton.core.util.android.sharedpreferences.internal.SerializableTestChild
import me.proton.core.util.android.sharedpreferences.internal.SerializableTestClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import me.proton.core.util.kotlin.startsWith

/**
 * Test suite for serializable items within SharedPreferences
 * @author Davide Farella
 */
internal class SerializableTest {

    private val p = mockSharedPreferences

    @Test
    fun `Preferences can store and get serializable item`() {

        // GIVEN
        val s = SerializableTestClass(
            "test",
            SerializableTestChild(
                15,
                true
            )
        )

        // WHEN
        p["key1"] = s

        // THEN
        assertEquals(s, p.get<SerializableTestClass>("key1"))
    }

    private class NonSerializableTestClass

    @Test
    fun `proper message is displayed if given class is not serializable`() {

        // GIVEN
        val ns =
            NonSerializableTestClass()

        // WHEN
        val block = { p["key2"] = ns }

        // THEN
        val message = assertFails(block).localizedMessage
        assert(message startsWith "Can't locate argument-less serializer for class NonSerializableTestClass.")
    }
}
