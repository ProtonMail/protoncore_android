@file:Suppress("TestFunctionName")

package ch.protonmail.libs.crypto.preferences

import androidx.test.platform.app.InstrumentationRegistry
import ch.protonmail.libs.core.preferences.get
import ch.protonmail.libs.core.preferences.set
import ch.protonmail.libs.testAndroid.mocks.newMockSharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Instrumented Test Suite for [SecureSharedPreferences]
 * @author Davide Farella
 */
internal class SecureSharedPreferencesInstrumentedTest {

    private val securePref = SecureSharedPreferences(
        InstrumentationRegistry.getInstrumentation().targetContext,
        newMockSharedPreferences,
        newMockSharedPreferences
    )

    @Test
    fun SecureSharedPreferences_works_correctly() {
        val b = true
        securePref["booleanKey"] = b
        assertEquals(b, securePref.get<Boolean>("booleanKey"))

        val f = 0.123f
        securePref["floatKey"] = f
        assertEquals(f, securePref.get<Float>("floatKey"))

        val i = 24
        securePref["intKey"] = i
        assertEquals(i, securePref.get<Int>("intKey"))

        val l = 35L
        securePref["longKey"] = l
        assertEquals(l, securePref.get<Long>("longKey"))

        val s = "Hello"
        securePref["stringKey"] = s
        assertEquals(s, securePref.get<String>("stringKey"))
    }
}
