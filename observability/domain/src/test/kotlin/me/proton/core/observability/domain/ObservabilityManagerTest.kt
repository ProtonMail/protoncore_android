/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.observability.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.assertTrue
import me.proton.core.util.kotlin.CoroutineScopeProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ObservabilityManagerTest {
    private lateinit var isObservabilityEnabled: IsObservabilityEnabled
    private lateinit var observabilityRepository: ObservabilityRepository
    private lateinit var scopeProvider: CoroutineScopeProvider
    private lateinit var workerManager: FakeWorkerManager
    private lateinit var tested: ObservabilityManager

    @BeforeTest
    fun setUp() {
        isObservabilityEnabled = mockk()
        observabilityRepository = mockk(relaxUnitFun = true)
        scopeProvider = TestCoroutineScopeProvider(TestDispatcherProvider(UnconfinedTestDispatcher()))
        workerManager = FakeWorkerManager()

        tested = ObservabilityManager(isObservabilityEnabled, observabilityRepository, scopeProvider, workerManager)
    }

    @Test
    fun observabilityIsDisabled() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns false

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.addEvent(any()) }

        coVerify(exactly = 1) { observabilityRepository.deleteAllEvents() }
        assertTrue(workerManager.scheduleCalls.isEmpty()) {
            "Unexpected call to `ObservabilityWorkerManager.schedule`."
        }
    }

    @Test
    fun sendWithDelay() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT - 1

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        assertEquals(0, workerManager.cancelCount)

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        assertContentEquals(listOf(ObservabilityManager.MAX_DELAY_MS.milliseconds), workerManager.scheduleCalls)
    }

    @Test
    fun numberOfEventsExceeded() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        assertEquals(0, workerManager.cancelCount)

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        assertContentEquals(listOf(Duration.ZERO), workerManager.scheduleCalls)
    }

    @Test
    fun durationSinceLastShipmentExceeded() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT / 2
        workerManager.duration = ObservabilityManager.MAX_DELAY_MS.milliseconds

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        assertEquals(0, workerManager.cancelCount)

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        assertContentEquals(listOf(Duration.ZERO), workerManager.scheduleCalls)
    }

    @Test
    fun durationSinceLastShipmentNotExceeded() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT / 2
        workerManager.duration = (ObservabilityManager.MAX_DELAY_MS - 1).milliseconds

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        assertEquals(0, workerManager.cancelCount)

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        assertContentEquals(listOf(ObservabilityManager.MAX_DELAY_MS.milliseconds), workerManager.scheduleCalls)
    }

    /**
     * The [getDurationSinceLastShipment] method returns inline class (Duration) which is not supported by mockk,
     * so we need to use a fake class.
     */
    private class FakeWorkerManager : ObservabilityWorkerManager {
        var cancelCount = 0
            private set
        var scheduleCalls = mutableListOf<Duration>()
            private set

        var duration: Duration? = null

        override fun cancel() {
            cancelCount += 1
        }

        override suspend fun getDurationSinceLastShipment(): Duration? = duration
        override suspend fun setLastSentNow() = Unit
        override fun schedule(delay: Duration) {
            scheduleCalls.add(delay)
        }
    }
}
