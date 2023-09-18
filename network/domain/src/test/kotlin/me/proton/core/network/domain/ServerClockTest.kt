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

package me.proton.core.network.domain

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.network.domain.server.ServerClock
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.server.ServerTimeManager
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

class ServerClockTest {

    @Test
    fun `utcTimeFlow returns correct values for offsets`() = runTest {
        val currentTime = Clock.systemUTC()
        val mockOffsetManager = mockk<ServerTimeManager>(relaxed = true)
        every { mockOffsetManager.offsetMilliseconds } returns MutableStateFlow(10000L)

        val appClock = ServerClock(mockOffsetManager)

        assertTimeWithTolerance(appClock.utcTimeFlow.first(), currentTime.instant().plusMillis(10000L))
    }

    @Test
    fun `localTimeFlow returns correct values for offsets`() = runTest {
        val currentTime = Clock.systemDefaultZone()
        val mockOffsetManager = mockk<ServerTimeManager>(relaxed = true)
        every { mockOffsetManager.offsetMilliseconds } returns MutableStateFlow(10000L)

        val appClock = ServerClock(mockOffsetManager)

        assertTimeWithTolerance(appClock.timeFlow.first(), currentTime.instant().plusMillis(10000L))
    }

    @Test
    fun `localTimeFlow returns correct values for negative offsets`() = runTest {
        val currentTime = Clock.systemDefaultZone()
        val mockOffsetManager = mockk<ServerTimeManager>(relaxed = true)
        every { mockOffsetManager.offsetMilliseconds } returns MutableStateFlow(-10000L)

        val appClock = ServerClock(mockOffsetManager)

        assertTimeWithTolerance(appClock.timeFlow.first(), currentTime.instant().plusMillis(-10000L))
    }

    private fun assertTimeWithTolerance(time: Instant, expected: Instant) {
        val toleranceInMillis = 100L  // Add 100ms tolerance for execution
        val difference = Duration.between(time, expected).toMillis()
        if (abs(difference) > toleranceInMillis) {
            println("time: $time, expected: $expected, difference: $difference")
        }
        assertTrue(abs(difference) <= toleranceInMillis)
    }
}