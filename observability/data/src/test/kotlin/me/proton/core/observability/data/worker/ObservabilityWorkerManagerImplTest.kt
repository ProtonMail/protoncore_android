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

package me.proton.core.observability.data.worker

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkContinuation
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class ObservabilityWorkerManagerImplTest {
    private lateinit var clock: FakeClock
    private lateinit var tested: ObservabilityWorkerManagerImpl
    private lateinit var workManager: WorkManager

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        workManager = mockk()
        tested = ObservabilityWorkerManagerImpl(clock::now, workManager)
    }

    @Test
    fun durationSinceLastShipment() = runTest {
        assertNull(tested.getDurationSinceLastShipment())
        tested.setLastSentNow()
        clock.current = 1000
        assertEquals(1000.milliseconds, tested.getDurationSinceLastShipment())

        tested.setLastSentNow()
        clock.current = 1500
        assertEquals(500.milliseconds, tested.getDurationSinceLastShipment())
    }

    @Test
    fun scheduling() {
        val workNameSlot = slot<String>()
        val workPolicySlot = slot<ExistingWorkPolicy>()
        val requestSlot = slot<OneTimeWorkRequest>()
        val workContinuation = mockk<WorkContinuation>(relaxed = true)
        every {
            workManager.beginUniqueWork(
                capture(workNameSlot),
                capture(workPolicySlot),
                capture(requestSlot)
            )
        } returns workContinuation
        every { workManager.cancelUniqueWork(any()) } returns mockk()

        // WHEN
        tested.schedule(ZERO)

        // THEN
        verify { workContinuation.enqueue() }
        assertEquals(ExistingWorkPolicy.REPLACE, workPolicySlot.captured)
        assertEquals(0, requestSlot.captured.workSpec.initialDelay)

        // WHEN
        tested.cancel()

        // THEN
        verify { workManager.cancelUniqueWork(workNameSlot.captured) }

        // WHEN
        tested.schedule(2.minutes)

        // THEN
        assertEquals(ExistingWorkPolicy.KEEP, workPolicySlot.captured)
        assertEquals(2.minutes.inWholeMilliseconds, requestSlot.captured.workSpec.initialDelay)
    }

    private class FakeClock {
        var current: Long = 0

        fun now(): Long = current
    }
}