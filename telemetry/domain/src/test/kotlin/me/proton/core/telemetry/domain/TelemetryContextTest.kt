/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.telemetry.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.coroutine.result
import me.proton.core.util.kotlin.coroutine.withResultContext
import org.junit.Test
import kotlin.test.assertEquals

class TelemetryContextTest : TelemetryContext {

    private val userId = UserId("user-id")

    override val telemetryManager: TelemetryManager = mockk {
        every { enqueue(userId = any(), event = any()) } returns Unit
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

    private fun Result<*>.toEvent(): TelemetryEvent = when {
        isSuccess -> mockk()
        else -> mockk()
    }

    private suspend fun callProducers(): Int {
        val prod1 = runCatching { producerKey1() }.getOrDefault(false)
        val prod2 = producerKey2()
        return if (prod1) 1 else prod2.first()
    }

    private suspend fun producerKey1(): Boolean = result("key1") { producerKey1.produce() }

    private suspend fun producerKey2(): List<Int> = result("key2") { producerKey2.produce() }

    @Test
    fun resultCollectorEnqueueExtension() = runTest {
        val value = withResultContext {
            onResultEnqueueTelemetry("key1", userId) { toEvent() }
            onResultEnqueueTelemetry("key2", userId) { toEvent() }
            onCompleteEnqueueTelemetry(userId) { toEvent() }

            callProducers()
        }

        verify(exactly = 3) { telemetryManager.enqueue(userId = userId, event = any()) }

        assertEquals(expected = 1, actual = value)
    }

    @Test
    fun resultCollectorEnqueueExtensionFailure() = runTest {
        every { producerKey1.produce() } throws IllegalStateException()
        every { producerKey2.produce() } returns listOf(4)

        val value = withResultContext {
            onResultEnqueueTelemetry("key1", userId) { toEvent() }
            onResultEnqueueTelemetry("key2", userId) { toEvent() }
            onCompleteEnqueueTelemetry(userId) { toEvent() }

            callProducers()
        }

        verify(exactly = 3) { telemetryManager.enqueue(userId = userId, event = any()) }

        assertEquals(expected = 4, actual = value)
    }

    @Test
    fun launchWithResultContext() = runTest {
        launchWithResultContext {
            onResultEnqueueTelemetry("key1", userId) { toEvent() }
            onResultEnqueueTelemetry("key2", userId) { toEvent() }
            onCompleteEnqueueTelemetry(userId) { toEvent() }

            callProducers()
        }.join()

        verify(exactly = 3) { telemetryManager.enqueue(userId = userId, event = any()) }
    }
    @Test
    fun resultCollectorEnqueueExtensionUnAuth() = runTest {
        val value = withResultContext {
            onResultEnqueueTelemetry("key1") { toEvent() }
            onResultEnqueueTelemetry("key2") { toEvent() }
            onCompleteEnqueueTelemetry { toEvent() }

            callProducers()
        }

        verify(exactly = 3) { telemetryManager.enqueue(userId = null, event = any()) }

        assertEquals(expected = 1, actual = value)
    }

    @Test
    fun resultCollectorEnqueueExtensionFailureUnAuth() = runTest {
        every { producerKey1.produce() } throws IllegalStateException()
        every { producerKey2.produce() } returns listOf(4)

        val value = withResultContext {
            onResultEnqueueTelemetry("key1") { toEvent() }
            onResultEnqueueTelemetry("key2") { toEvent() }
            onCompleteEnqueueTelemetry { toEvent() }

            callProducers()
        }

        verify(exactly = 3) { telemetryManager.enqueue(userId = null, event = any()) }

        assertEquals(expected = 4, actual = value)
    }

    @Test
    fun launchWithResultContextUnAuth() = runTest {
        launchWithResultContext {
            onResultEnqueueTelemetry("key1") { toEvent() }
            onResultEnqueueTelemetry("key2") { toEvent() }
            onCompleteEnqueueTelemetry { toEvent() }

            callProducers()
        }.join()

        verify(exactly = 3) { telemetryManager.enqueue(userId = null, event = any()) }
    }
}
