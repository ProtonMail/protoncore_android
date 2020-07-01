package me.proton.core.util.android.sharedpreferences

import me.proton.core.test.android.mocks.newMockSharedPreferences
import kotlin.test.*

/**
 * Test suite for Preferences's DSL
 * @author Davide Farella
 */
internal class PreferencesDelegationTest {

    private val p = newMockSharedPreferences

    private var nonNullInt by p.int(default = 4)
    private var nullableInt by p.int()

    private var explicitKey by p.string(key = "KEY")

    private var nonNullIntByInference by p(default = 4)
    private var nullableIntByInference: Int? by p.any()

    @Test
    fun `non nullable property works correctly`() {
        assertEquals(4, nonNullInt)
        nonNullInt = 5
        assertEquals(5, nonNullInt)
    }

    @Test
    fun `nullable property works correctly`() {
        assertEquals(null, nullableInt)
        nullableInt = 5
        assertEquals(5, nullableInt)
        nullableInt = null
        assertEquals(null, nullableInt)
    }

    @Test
    fun `property with explicit key works correctly`() {
        explicitKey = "hello"
        assertEquals(p.getString("KEY", ""), explicitKey)
    }

    @Test
    fun `non nullable property with type inference works correctly`() {
        assertEquals(4, nonNullIntByInference)
        nonNullIntByInference = 5
        assertEquals(5, nonNullIntByInference)
    }

    @Test
    fun `nullable property with type inference works correctly`() {
        assertEquals(null, nullableIntByInference)
        nullableIntByInference = 5
        assertEquals(5, nullableIntByInference)
    }
}
