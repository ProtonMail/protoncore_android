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

package me.proton.core.telemetry.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.repository.TelemetryRepository
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import org.junit.Before
import org.junit.Test

class ProcessTelemetryEventsTest {
    // region mocks
    private val isTelemetryEnabled = mockk<IsTelemetryEnabled>(relaxed = true)
    private val telemetryRepository = mockk<TelemetryRepository>(relaxed = true)
    // endregion

    private lateinit var useCase: ProcessTelemetryEvents

    private val userId = UserId("user-id")

    @Before
    fun beforeEveryTest() {
        useCase = ProcessTelemetryEvents(
            isTelemetryEnabled,
            telemetryRepository
        )
    }

    @Test
    fun `telemetry disabled`() = runTest {
        coEvery { isTelemetryEnabled(userId) } returns false
        useCase(userId)
        coVerify(exactly = 1) { telemetryRepository.deleteAllEvents(userId) }
        coVerify(exactly = 0) { telemetryRepository.getEvents(userId, any()) }
        coVerify(exactly = 0) { telemetryRepository.sendEvents(userId, any()) }
    }

    @Test
    fun `telemetry enabled`() = runTest {
        val testTelemetryEvents = listOf(
            TelemetryEvent(group = "group", name = "first"),
            TelemetryEvent(group = "group", name = "second"),
            TelemetryEvent(group = "group", name = "third")
        )
        coEvery { isTelemetryEnabled(userId) } returns true
        coEvery { telemetryRepository.getEvents(userId, 100) } returns
            testTelemetryEvents andThen emptyList()
        useCase(userId)
        coVerify(exactly = 0) { telemetryRepository.deleteAllEvents(any()) }
        coVerify(exactly = 1) { telemetryRepository.deleteEvents(userId, testTelemetryEvents) }
        coVerify(exactly = 2) { telemetryRepository.getEvents(userId, 100) }
        coVerify(exactly = 1) { telemetryRepository.sendEvents(userId, testTelemetryEvents) }
    }
}
