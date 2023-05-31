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

package me.proton.core.observability.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.coroutine.result
import me.proton.core.util.kotlin.coroutine.withResultContext
import org.junit.Test
import kotlin.test.assertEquals

class ObservabilityContextTest : ObservabilityContext {

    override val manager: ObservabilityManager = mockk {
        every { enqueue(any<ObservabilityData>(), any()) } returns Unit
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

    private fun Result<*>.toEvent() = when {
        isSuccess -> mockk<ObservabilityData>()
        else -> mockk<ObservabilityData>()
    }

    @Test
    fun resultCollectorEnqueueExtension() = runTest {
        val value = withResultContext {
            onResultEnqueue("key1") { toEvent() }
            onResultEnqueue("key2") { toEvent() }
            onCompleteEnqueue { toEvent() }

            callProducers()
        }

        verify(exactly = 3) { manager.enqueue(any<ObservabilityData>(), any()) }

        assertEquals(expected = 1, actual = value)
    }

    @Test
    fun resultCollectorEnqueueExtensionFailure() = runTest {
        every { producerKey1.produce() } throws IllegalStateException()
        every { producerKey2.produce() } returns listOf(4)

        val value = withResultContext {
            onResultEnqueue("key1") { toEvent() }
            onResultEnqueue("key2") { toEvent() }
            onCompleteEnqueue { toEvent() }

            callProducers()
        }

        verify(exactly = 3) { manager.enqueue(any<ObservabilityData>(), any()) }

        assertEquals(expected = 4, actual = value)
    }

    @Test
    fun launchWithResultContext() = runTest {
        launchWithResultContext {
            onResultEnqueue("key1") { toEvent() }
            onResultEnqueue("key2") { toEvent() }
            onCompleteEnqueue { toEvent() }

            callProducers()
        }.join()

        verify(exactly = 3) { manager.enqueue(any<ObservabilityData>(), any()) }
    }
}
