package ch.protonmail.libs.core.utils

import android.os.Bundle
import androidx.core.os.bundleOf
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.Serializable
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * __Unit__ test suite for Bundle utils
 * @author Davide Farella
 */
// TODO: remove ignore on Robolectric 4.3.2
@Ignore("Robolectric bug: https://github.com/robolectric/robolectric/issues/5303")
@RunWith(RobolectricTestRunner::class)
class BundleUtilsTest {

    @Test
    fun `contains works correctly`() {
        // GIVEN
        val bundle = bundleOf("key" to 0)

        // WHEN - THEN
        assertTrue("key" in bundle)
        assertFalse("noKey" in bundle)
    }

    @Test
    fun `set and getAny works correctly`() {
        // GIVEN
        val bundle = Bundle()

        // WHEN
        bundle["key"] = 15

        // THEN
        assertEquals(15, bundle.getAny<Int>("key"))
    }

    @Test
    fun `set and getAny works correctly with Java Serializable`() {
        // GIVEN
        data class S(val int: Int): Serializable
        val bundle = Bundle()

        // WHEN
        bundle["key"] = S(15)

        // THEN
        assertEquals(15, bundle.getAny<S>("key")?.int)
    }

    @Test
    fun `set and getAny works correctly with Kotlin Serializable`() {
        // GIVEN
        val bundle = Bundle()

        // WHEN
        bundle["key"] = SerializableTestClass(15)

        // THEN
        assertEquals(15, bundle.getAny<SerializableTestClass>("key")?.number)
    }
}
