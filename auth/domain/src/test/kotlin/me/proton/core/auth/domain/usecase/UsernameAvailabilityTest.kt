/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.test.kotlin.assertEquals
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class UsernameAvailabilityTest {
    private val authRepository = mockk<AuthRepository>(relaxed = true)

    private lateinit var useCase: UsernameAvailability
    private val testUsername = "test-username"

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = UsernameAvailability(authRepository)
        coEvery { authRepository.isUsernameAvailable(testUsername) } returns DataResult.Success(true, ResponseSource.Remote)
    }

    @Test
    fun `username is available test`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke(testUsername).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UsernameAvailability.UsernameAvailabilityState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UsernameAvailability.UsernameAvailabilityState.Success)
        assertTrue(secondEvent.available)
    }

    @Test
    fun `username is unavailable test`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.isUsernameAvailable(testUsername) } returns DataResult.Error.Message(message = "username unavailaable",
            source = ResponseSource.Remote,
            code = 12106,
            validation = false)
        // WHEN
        val listOfEvents = useCase.invoke(testUsername).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UsernameAvailability.UsernameAvailabilityState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UsernameAvailability.UsernameAvailabilityState.Error.UsernameUnavailable)
    }

    @Test
    fun `empty username returns error state`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.isUsernameAvailable(testUsername) } returns DataResult.Success(false, ResponseSource.Remote)
        // WHEN
        val listOfEvents = useCase.invoke("").toList()
        // THEN
        assertEquals(1, listOfEvents.size)
        val event = listOfEvents[0]
        assertTrue(event is UsernameAvailability.UsernameAvailabilityState.Error.EmptyUsername)
    }

    @Test
    fun `username availability api error returns error state`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.isUsernameAvailable(testUsername) } returns DataResult.Error.Message(message = "api error",
            source = ResponseSource.Remote,
            code = 401,
            validation = false)
        // WHEN
        val listOfEvents = useCase.invoke(testUsername).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertIs<UsernameAvailability.UsernameAvailabilityState.Processing>(listOfEvents[0])
        val secondEvent = listOfEvents[1]
        assertTrue(secondEvent is UsernameAvailability.UsernameAvailabilityState.Error.Message)
        assertEquals("api error", secondEvent.message)
    }
}
