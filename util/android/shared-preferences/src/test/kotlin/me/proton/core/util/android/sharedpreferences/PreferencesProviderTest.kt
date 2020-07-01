package me.proton.core.util.android.sharedpreferences

import android.content.SharedPreferences
import me.proton.core.test.android.mocks.newMockSharedPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Test suite for [PreferencesProvider]
 * @author Davide Farella
 */
internal class PreferencesProviderTest {

    class LazyPreferencesProvider : PreferencesProvider {
        var prefs: SharedPreferences? = null
        override val preferences: SharedPreferences
            get() = prefs ?: throw IllegalStateException("Not initialized")

        var prop by int()
    }

    @Test
    fun `throw exception when accessing delegate if preferences are not set`() = with(LazyPreferencesProvider()) {
        assertFailsWith<IllegalStateException> { prop }
        Unit
    }

    @Test
    fun `works correctly when preferences are initialized lazily`() = with(LazyPreferencesProvider()) {
        prefs = newMockSharedPreferences
        prop = 0
        Unit
    }

    // region Property
    object SomePreferencesProvider :
        PreferencesProvider {
        override val preferences = newMockSharedPreferences

        var nonNullInt by int(default = 4)
        var nullableInt by int()

        var nonNullString by string(default = "4")
        var nullableString by string()

        var explicitKey by string(key = "KEY")

        var nonNullIntByInference by any(default = 4)
        var nullableIntByInference: Int? by any()
    }

    @Test
    fun `non nullable Int property works correctly`() = with(SomePreferencesProvider) {
        assertEquals(4,
            nonNullInt
        )
        nonNullInt = 5
        assertEquals(5,
            nonNullInt
        )
    }

    @Test
    fun `nullable Int property works correctly`() = with(SomePreferencesProvider) {
        assertEquals(null,
            nullableInt
        )
        nullableInt = 5
        assertEquals(5,
            nullableInt
        )
        nullableInt = null
        assertEquals(null,
            nullableInt
        )
    }

    @Test
    fun `non nullable String property works correctly`() = with(SomePreferencesProvider) {
        assertEquals("4",
            nonNullString
        )
        nonNullString = "5"
        assertEquals("5",
            nonNullString
        )
    }

    @Test
    fun `nullable String property works correctly`() = with(SomePreferencesProvider) {
        assertEquals(null,
            nullableString
        )
        nullableString = "5"
        assertEquals("5",
            nullableString
        )
        nullableString = null
        assertEquals(null,
            nullableString
        )
    }

    @Test
    fun `property with explicit key works correctly`() = with(SomePreferencesProvider) {
        explicitKey = "hello"
        assertEquals(
            preferences.getString("KEY", ""),
            explicitKey
        )
    }

    @Test
    fun `non nullable property with type inference works correctly`() = with(SomePreferencesProvider) {
        assertEquals(4,
            nonNullIntByInference
        )
        nonNullIntByInference = 5
        assertEquals(5,
            nonNullIntByInference
        )
    }

    @Test
    fun `nullable property with type inference works correctly`() = with(SomePreferencesProvider) {
        assertEquals(null,
            nullableIntByInference
        )
        nullableIntByInference = 5
        assertEquals(5,
            nullableIntByInference
        )
    }
    // endregion
}
