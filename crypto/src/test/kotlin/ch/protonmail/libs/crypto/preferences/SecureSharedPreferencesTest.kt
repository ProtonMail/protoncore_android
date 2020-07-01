package ch.protonmail.libs.crypto.preferences

import ch.protonmail.libs.core.preferences.get
import ch.protonmail.libs.core.preferences.set
import ch.protonmail.libs.testAndroid.mocks.mockSharedPreferences
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import kotlin.test.Test

/**
 * Test suite for [SecureSharedPreferences]
 * @author Davide Farella
 */
internal class SecureSharedPreferencesTest {

    private val securePref = SecureSharedPreferences(mockSharedPreferences,
        encrypter = mockk {
            every { this@mockk.invoke(any<String>()) } answers {
                "XX${firstArg<String>()}XX"
            }
        },
        decrypter = mockk {
            every { this@mockk.invoke(any<String>()) } answers {
                firstArg<String>().removeSurrounding("XX")
            }
        }
    )

    @Test
    fun `SecureSharedPreferences works correctly`() {
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
