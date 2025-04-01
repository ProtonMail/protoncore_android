package me.proton.core.compose.effect

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EffectTest {
    @Test
    fun `empty effect`() = runTest {
        var consumed = 0
        val effect = Effect.empty<Int>()

        assertNull(effect.peek())
        assertNull(effect.consume { consumed = it })
        assertEquals(0, consumed)
    }

    @Test
    fun `consumed effect`() = runTest {
        var consumed = 0
        val effect = Effect.of(42)

        assertEquals(42, effect.peek())
        assertEquals("OK", effect.consume { consumed = it; "OK" })
        assertEquals(42, consumed)

        consumed = 0
        assertNull(effect.peek())
        assertNull(effect.consume { consumed = it })
        assertEquals(0, consumed)
    }
}
