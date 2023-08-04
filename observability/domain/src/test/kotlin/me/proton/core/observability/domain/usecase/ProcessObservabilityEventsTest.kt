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

package me.proton.core.observability.domain.usecase

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.entity.ObservabilityEvent
import org.junit.Before
import org.junit.Test


class ProcessObservabilityEventsTest {
    // region mocks
    private val isObservabilityEnabled = mockk<IsObservabilityEnabled>(relaxed = true)
    private val observabilityRepository = mockk<ObservabilityRepository>(relaxed = true)
    private val sendObservabilityEvents = mockk<SendObservabilityEvents>(relaxed = true)
    // endregion

    private lateinit var useCase: ProcessObservabilityEvents

    @Before
    fun beforeEveryTest() {
        useCase = ProcessObservabilityEvents(
            isObservabilityEnabled, observabilityRepository, sendObservabilityEvents
        )
    }

    @Test
    fun `observability disabled`() = runTest {
        coEvery { isObservabilityEnabled.invoke() } returns false
        useCase.invoke()
        coVerify(exactly = 1) { observabilityRepository.deleteAllEvents() }
        coVerify(exactly = 0) { observabilityRepository.getEventsAndSanitizeDb() }
        coVerify(exactly = 0) { sendObservabilityEvents.invoke(any()) }
    }

    @Test
    fun `observability enabled`() = runTest {
        val testObservabilityEvents = listOf(
            ObservabilityEvent(id = 1, name = "first", version = 1, data = "first data"),
            ObservabilityEvent(id = 1, name = "second", version = 1, data = "second data"),
            ObservabilityEvent(id = 1, name = "third", version = 1, data = "third data"),
        )
        coEvery { isObservabilityEnabled.invoke() } returns true
        coEvery { observabilityRepository.getEventsAndSanitizeDb(100) } returns
            testObservabilityEvents andThen emptyList()
        useCase.invoke()
        coVerify(exactly = 0) { observabilityRepository.deleteAllEvents() }
        coVerify(exactly = 1) { observabilityRepository.deleteEvents(testObservabilityEvents) }
        coVerify(exactly = 2) { observabilityRepository.getEventsAndSanitizeDb(100) }
        coVerify(exactly = 1) { sendObservabilityEvents.invoke(testObservabilityEvents) }
    }
}