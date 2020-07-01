package me.proton.core.test.kotlin

import kotlin.test.*

/**
 * Asserts that the [expected] value is equal to the [actual] value, with a [message]
 * Note that the [message] is not evaluated lazily, the lambda is only cosmetic
 */
fun <T> assertEquals(expected: T, actual: T, message: () -> String) {
    assertEquals(expected, actual, message())
}

/** Asserts that the expression is `true` with a lazy message */
fun assertTrue(actual: Boolean, lazyMessage: () -> String) {
    asserter.assertTrue(lazyMessage, actual)
}

/**
 * Asserts that [actual] [`is`] [EXPECTED].
 * Ignored if [EXPECTED] is nullable and [actual] is null
 */
inline fun <reified EXPECTED> assertIs(actual: Any?) {
    // If `EXPECTED` is not nullable, assert that `actual` is not null
    if (null !is EXPECTED) assertNotNull(actual)

    if (null !is EXPECTED || actual != null)
        assertTrue(actual is EXPECTED) {
            // Usage of unsafe operator `!!` since if `EXPECTED` is not nullable, we already assert
            // that `actual` is not null
            "Expected to be '${EXPECTED::class.qualifiedName}'. " +
                    "Actual: '${actual!!::class.qualifiedName}'"
        }
}
