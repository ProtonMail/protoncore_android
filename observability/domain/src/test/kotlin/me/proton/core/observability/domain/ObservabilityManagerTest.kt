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

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.test.kotlin.TestCoroutineScopeProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds

class ObservabilityManagerTest {
    private var currentClockMillis: Long = 0L

    private lateinit var isObservabilityEnabled: IsObservabilityEnabled
    private lateinit var observabilityRepository: ObservabilityRepository
    private lateinit var scopeProvider: CoroutineScopeProvider
    private lateinit var workerManager: ObservabilityWorkerManager
    private lateinit var timeTracker: ObservabilityTimeTracker
    private lateinit var tested: ObservabilityManager

    @BeforeTest
    fun setUp() {
        currentClockMillis = 0L
        isObservabilityEnabled = mockk()
        observabilityRepository = mockk(relaxUnitFun = true)
        scopeProvider =
            TestCoroutineScopeProvider(TestDispatcherProvider(UnconfinedTestDispatcher()))
        timeTracker = ObservabilityTimeTracker { currentClockMillis }
        workerManager = mockk(relaxed = true)

        tested = ObservabilityManager(
            isObservabilityEnabled,
            observabilityRepository,
            scopeProvider,
            timeTracker,
            workerManager
        )
    }

    @Test
    fun observabilityIsDisabled() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns false

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.addEvent(any()) }

        coVerify(exactly = 1) { observabilityRepository.deleteAllEvents() }
        verify(exactly = 0) { workerManager.schedule(any()) }
    }

    @Test
    fun sendWithDelay() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT - 1

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        verify(exactly = 0) { workerManager.cancel() }

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        verify(exactly = 1) { workerManager.schedule(ObservabilityManager.MAX_DELAY_MS.milliseconds) }
    }

    @Test
    fun numberOfEventsExceeded() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        verify(exactly = 0) { workerManager.cancel() }

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        verify(exactly = 1) { workerManager.schedule(ZERO) }
    }

    @Test
    fun durationSinceLastShipmentExceeded() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT / 2
        timeTracker.setFirstEventNow()
        currentClockMillis = ObservabilityManager.MAX_DELAY_MS

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        verify(exactly = 0) { workerManager.cancel() }

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        verify(exactly = 1) { workerManager.schedule(ZERO) }
    }

    @Test
    fun durationSinceLastShipmentNotExceeded() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns ObservabilityManager.MAX_EVENT_COUNT / 2
        currentClockMillis = ObservabilityManager.MAX_DELAY_MS - 1

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        verify(exactly = 0) { workerManager.cancel() }

        coVerify(exactly = 1) { observabilityRepository.addEvent(any()) }
        verify(exactly = 1) { workerManager.schedule(ObservabilityManager.MAX_DELAY_MS.milliseconds) }
    }

    @Test
    fun schedulingEventsUsesProperDelays() = runTest {
        // 1. =========================
        // GIVEN
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventCount() } returns 1
        currentClockMillis = ObservabilityManager.MAX_DELAY_MS

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        // The first event should be enqueued with max delay.
        verify(exactly = 1) { workerManager.schedule(ObservabilityManager.MAX_DELAY_MS.milliseconds) }

        // 2. =========================
        // GIVEN
        clearMocks(workerManager)
        coEvery { observabilityRepository.getEventCount() } returns 2
        currentClockMillis += 10

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        // Subsequent events should be also enqueued with max delay.
        verify(exactly = 1) { workerManager.schedule(ObservabilityManager.MAX_DELAY_MS.milliseconds) }

        // 3. =========================
        // GIVEN
        clearMocks(workerManager)
        coEvery { observabilityRepository.getEventCount() } returns 3
        currentClockMillis += ObservabilityManager.MAX_DELAY_MS

        // WHEN
        tested.enqueue(mockk<ObservabilityData>(relaxed = true))

        // THEN
        // Events enqueued after MAX_DELAY_MS since the first event,
        // should be enqueued with no delay.
        verify(exactly = 1) { workerManager.schedule(ZERO) }
    }
}
