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
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class PerformLogoutTest {
    private val authRepository = mockk<AuthRepository>(relaxed = true)

    private lateinit var useCase: PerformLogout
    private val testSessionId = "test-session-id"

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = PerformLogout(authRepository)
        coEvery { authRepository.revokeSession(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            true
        )
    }

    @Test
    fun `logout happy path events list is correct`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId)).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformLogout.LogoutState.Processing)
        val successEvent = listOfEvents[1]
        assertTrue(successEvent is PerformLogout.LogoutState.Success)
        assertTrue(successEvent.sessionRevoked)
    }

    @Test
    fun `logout happy path invocations are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(SessionId(testSessionId)).toList()
        // THEN
        coVerify(exactly = 1) { authRepository.revokeSession(SessionId(testSessionId)) }
    }

    @Test
    fun `logout api returns error events list is correct`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.revokeSession(SessionId(testSessionId)) } returns DataResult.Error.Remote(
            "Invalid input"
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId)).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformLogout.LogoutState.Processing)
        val successEvent = listOfEvents[1]
        assertTrue(successEvent is PerformLogout.LogoutState.Error)
    }

    @Test
    fun `logout api returns false events list is correct`() = runBlockingTest {
        // GIVEN
        coEvery { authRepository.revokeSession(SessionId(testSessionId)) } returns DataResult.Success(
            ResponseSource.Remote,
            false
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId)).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformLogout.LogoutState.Processing)
        val successEvent = listOfEvents[1]
        assertTrue(successEvent is PerformLogout.LogoutState.Success)
        assertFalse(successEvent.sessionRevoked)
    }
}
