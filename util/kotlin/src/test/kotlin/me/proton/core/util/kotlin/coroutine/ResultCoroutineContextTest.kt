/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.util.kotlin.coroutine

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResultCoroutineContextTest : ManagerContext {

    override val manager: Manager = mockk {
        every { enqueue(any()) } returns Unit
    }

    interface Producer<T> {
        fun produce(): T
    }

    private val producerKey1: Producer<Boolean> = mockk {
        every { produce() } returns true
    }
    private val producerKey2: Producer<List<Int>> = mockk {
        every { produce() } returns listOf(2)
    }
    private val producerKey3: Producer<String> = mockk {
        every { produce() } returns "3"
    }

    private fun Result<*>.toEvent(key: String) = when {
        isSuccess -> Event(key, 1)
        else -> Event(key, 0)
    }

    private suspend fun callProducers(): Int {
        val prod1 = runCatching { producerKey1() }.getOrDefault(false)
        val prod2 = producerKey2()
        return if (prod1) 1 else prod2.first()
    }

    private suspend fun producerKey1(): Boolean {
        return result("key1") { producerKey1.produce() }
    }

    private suspend fun producerKey2(): List<Int> {
        return result("key2") { producerKey2.produce() }
    }

    private suspend fun producerKey3(): String {
        return result("key3") { producerKey3.produce() }
    }

    @Test
    fun resultCollector() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onResult { enqueue { toEvent("any") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            callProducers()
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 3) { manager.enqueue(Event("any", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test
    fun resultCollectorFailure() = runTest {
        assertFailsWith<IllegalStateException> {
            withResultContext {
                onResult("key1") { enqueue { toEvent("key1") } }
                onResult("key2") { enqueue { toEvent("key2") } }
                onResult { enqueue { toEvent("any") } }
                onSuccess { enqueue { toEvent("success") } }
                onFailure { enqueue { toEvent("failure") } }
                onComplete { enqueue { toEvent("complete") } }

                callProducers()
                error("failure")
            }
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("any", 0)) }
        verify(exactly = 2) { manager.enqueue(Event("any", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 0)) }
    }

    @Test
    fun resultCollectorContextNestedWithContext() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onResult { enqueue { toEvent("any") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            withContext(Dispatchers.Default) {
                withContext(Dispatchers.IO) {
                    callProducers()
                }
            }
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 3) { manager.enqueue(Event("any", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test
    fun resultCollectorContextNewContext() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            launch { callProducers() }.join()
        }

        verify(exactly = 0) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test
    fun resultCollectorContextNewContextWithCurrent() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            launch(currentCoroutineContext()) { callProducers() }.join()
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test
    fun resultCollectorContextNestedWithResultContext() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onResult("key3") { enqueue { toEvent("key3") } }
            onResult { enqueue { toEvent("any") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            withResultContext {
                onResult("key3") { enqueue { toEvent("nested.key3") } }
                onResult { enqueue { toEvent("nested.any") } }
                onSuccess { enqueue { toEvent("nested.success") } }
                onFailure { enqueue { toEvent("nested.failure") } }
                onComplete { enqueue { toEvent("nested.complete") } }

                callProducers()
                producerKey3()
            }
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key3", 1)) }
        verify(exactly = 5) { manager.enqueue(Event("any", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }

        verify(exactly = 1) { manager.enqueue(Event("nested.key3", 1)) }
        verify(exactly = 5) { manager.enqueue(Event("nested.any", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("nested.success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("nested.failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("nested.complete", 1)) }
    }

    @Test
    fun resultCollectorEnqueueExtension() = runTest {
        val value = withResultContext {
            onResultEnqueue("key1") { toEvent("key1") }
            onResultEnqueue("key2") { toEvent("key2") }
            onCompleteEnqueue { toEvent("complete") }

            callProducers()
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }

        assertEquals(expected = 1, actual = value)
    }

    @Test
    fun resultCollectorEnqueueExtensionFailure() = runTest {
        every { producerKey1.produce() } throws IllegalStateException()
        every { producerKey2.produce() } returns listOf(4)

        val value = withResultContext {
            onResultEnqueue("key1") { toEvent("key1") }
            onResultEnqueue("key2") { toEvent("key2") }
            onCompleteEnqueue { toEvent("complete") }

            callProducers()
        }

        verify(exactly = 0) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }

        assertEquals(expected = 4, actual = value)
    }

    @Test
    fun resultCollectorSuspend() = runTest {
        withResultContext {
            onResult("key1") { delay(100) }
            onComplete { delay(100) }

            callProducers()
        }
    }

    @Test
    fun resultCollectorMultipleOnResult() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1.1") } }
            onResult("key1") { enqueue { toEvent("key1.2") } }
            onResult { enqueue { toEvent("any") } }

            producerKey1()
        }

        verify(exactly = 1) { manager.enqueue(Event("key1.1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key1.2", 1)) }
        verify(exactly = 2) { manager.enqueue(Event("any", 1)) }
    }

    @Test
    fun resultCollectorMultipleResultKeyNotAllowed() = runTest {
        assertFailsWith<IllegalStateException> {
            withResultContext(allowDuplicateResultKey = false) {
                onResult("key1") { enqueue { toEvent("key1.1") } }
                onResult("key1") { enqueue { toEvent("key1.2") } }
                onResult { enqueue { toEvent("any") } }

                producerKey1()
            }
        }
    }

    @Test(expected = IllegalStateException::class)
    fun resultCollectorThrowOnResult() = runTest {
        withResultContext {
            onResult("key1") { error("failure") }

            producerKey1()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun resultCollectorThrowOnResultWithoutKey() = runTest {
        withResultContext {
            onResult { error("failure") }

            producerKey1()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun resultCollectorThrowOnComplete() = runTest {
        withResultContext {
            onComplete { error("failure") }

            callProducers()
        }
    }

    @Test
    fun resultCollectorThrowOnResultCatching() = runTest {
        withResultContext {
            onResultCatching("key1") { error("failure") }

            producerKey1()
        }
    }

    @Test
    fun resultCollectorThrowOnResultCatchingWithoutKey() = runTest {
        withResultContext {
            onResultCatching { error("failure") }

            producerKey1()
        }
    }

    @Test
    fun resultCollectorThrowOnCompleteCatching() = runTest {
        withResultContext {
            onCompleteCatching { error("failure") }

            callProducers()
        }
    }

    @Test
    fun resultCollectorOnCatching() = runTest {
        withResultContext {
            onResultCatching("key1") { enqueue { toEvent("onResultCatching") } }
            onCompleteCatching { enqueue { toEvent("onCompleteCatching") } }

            producerKey1()
        }

        verify(exactly = 1) { manager.enqueue(Event("onResultCatching", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("onCompleteCatching", 1)) }
    }

    @Test
    fun resultCollectorOnSuccess() = runTest {
        withResultContext {
            onSuccess { enqueue { toEvent("onSuccess") } }

            callProducers()
        }

        verify(exactly = 1) { manager.enqueue(Event("onSuccess", 1)) }
    }

    @Test
    fun resultCollectorOnFailure() = runTest {
        assertFailsWith<IllegalStateException> {
            withResultContext {
                onFailure { enqueue { toEvent("onFailure") } }

                error("failure")
            }
        }

        verify(exactly = 1) { manager.enqueue(Event("onFailure", 0)) }
    }

    @Test
    fun resultCollectorTwoSequentialBlocks() = runTest {
        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onSuccess { enqueue { toEvent("success1") } }
            onComplete { enqueue { toEvent("complete1") } }

            callProducers()
        }

        withResultContext {
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success2") } }
            onComplete { enqueue { toEvent("complete2") } }

            callProducers()
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }

        verify(exactly = 1) { manager.enqueue(Event("success1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete1", 1)) }

        verify(exactly = 1) { manager.enqueue(Event("success2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete2", 1)) }
    }

    @Test
    fun resultCollectorThrowProducer1() = runTest {
        coEvery { producerKey1() } throws IllegalStateException()

        withResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            callProducers()
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test
    fun resultCollectorThrowProducer2() = runTest {
        coEvery { producerKey2() } throws IllegalStateException()

        assertFailsWith<IllegalStateException> {
            withResultContext {
                onResult("key1") { enqueue { toEvent("key1") } }
                onResult("key2") { enqueue { toEvent("key2") } }
                onSuccess { enqueue { toEvent("success") } }
                onFailure { enqueue { toEvent("failure") } }
                onComplete { enqueue { toEvent("complete") } }

                callProducers()
            }
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 0)) }
        verify(exactly = 0) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 0)) }
    }

    @Test
    fun resultCollectorThrowProducer3() = runTest {
        coEvery { producerKey3() } throws IllegalStateException()

        assertFailsWith<IllegalStateException> {
            withResultContext {
                onResult("key1") { enqueue { toEvent("key1") } }
                onResult("key2") { enqueue { toEvent("key2") } }
                onSuccess { enqueue { toEvent("success") } }
                onFailure { enqueue { toEvent("failure") } }
                onComplete { enqueue { toEvent("complete") } }

                withResultContext {
                    onResult("key3") { enqueue { toEvent("key3") } }
                    onSuccess { enqueue { toEvent("success3") } }
                    onFailure { enqueue { toEvent("failure3") } }
                    onComplete { enqueue { toEvent("complete3") } }

                    callProducers()
                    producerKey3()
                }
            }
        }

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("success", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 0)) }

        verify(exactly = 1) { manager.enqueue(Event("key3", 0)) }
        verify(exactly = 0) { manager.enqueue(Event("success3", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("failure3", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete3", 0)) }
    }

    @Test
    fun launchWithResultContext() = runTest {
        launchWithResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            callProducers()
        }.join()

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test
    fun launchWithResultContextThrowProducer1() = runTest {
        coEvery { producerKey1() } throws IllegalStateException()

        launchWithResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            callProducers()
        }.join()

        verify(exactly = 1) { manager.enqueue(Event("key1", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("success", 1)) }
        verify(exactly = 0) { manager.enqueue(Event("failure", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 1)) }
    }

    @Test(expected = IllegalStateException::class)
    fun launchWithResultContextThrowProducer2() = runTest {
        coEvery { producerKey2() } throws IllegalStateException()

        launchWithResultContext {
            onResult("key1") { enqueue { toEvent("key1") } }
            onResult("key2") { enqueue { toEvent("key2") } }
            onSuccess { enqueue { toEvent("success") } }
            onFailure { enqueue { toEvent("failure") } }
            onComplete { enqueue { toEvent("complete") } }

            callProducers()
        }.join()

        verify(exactly = 1) { manager.enqueue(Event("key1", 1)) }
        verify(exactly = 1) { manager.enqueue(Event("key2", 0)) }
        verify(exactly = 0) { manager.enqueue(Event("success", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("failure", 0)) }
        verify(exactly = 1) { manager.enqueue(Event("complete", 0)) }
    }
}
