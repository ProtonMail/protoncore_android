package me.proton.core.util.kotlin

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFails

class RetryKtTest {
    @Test
    fun defaultArgumentsRetry() = runTest {
        // GIVEN
        val block = mockk<() -> Unit> {
            every { this@mockk.invoke() } throws Throwable("Test error")
        }

        // WHEN
        assertFails("Test error") {
            retry(block = block)
        }

        // THEN
        verify(exactly = 3) { block() }
    }

    @Test
    fun retryZeroTimes() = runTest {
        // GIVEN
        val block = mockk<() -> Unit> {
            every { this@mockk.invoke() } throws Throwable("Test error")
        }

        // WHEN
        assertFails("Test error") {
            retry(times = 0, block = block)
        }

        // THEN
        verify(exactly = 1) { block() }
    }

    @Test
    fun retryOnceButFalsePredicate() = runTest {
        // GIVEN
        val block = mockk<() -> Unit> {
            every { this@mockk.invoke() } throws Throwable("Test error")
        }

        // WHEN
        assertFails("Test error") {
            retry(times = 1, predicate = { false }, block = block)
        }

        // THEN
        verify(exactly = 1) { block() }
    }
}
