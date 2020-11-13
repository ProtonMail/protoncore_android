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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class GetUserTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)

    // endregion
    // region test data
    private val testSessionId = "test-session-id"
    private lateinit var useCase: GetUser
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = GetUser(authRepository)
    }

    @Test
    fun `get user happy path works correctly`() = runBlockingTest {
        // GIVEN
        val userMock = mockk<User>()
        every { userMock.id } returns "test-id"
        coEvery {
            authRepository.getUser(SessionId(testSessionId))
        } returns DataResult.Success(
            ResponseSource.Remote,
            userMock
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId)).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is GetUser.State.Processing)
        val successEvent = listOfEvents[1]
        assertTrue(successEvent is GetUser.State.Success)
        val user = successEvent.user
        assertNotNull(user)
        assertEquals("test-id", user.id)
    }

    @Test
    fun `get user error path works correctly`() = runBlockingTest {
        // GIVEN
        val userMock = mockk<User>()
        every { userMock.id } returns "test-id"
        coEvery {
            authRepository.getUser(SessionId(testSessionId))
        } returns DataResult.Error.Remote(
            message = "user-error",
            protonCode = 1234,
            httpCode = 401
        )
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId)).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is GetUser.State.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is GetUser.State.Error.Message)
        assertEquals("user-error", errorEvent.message)
    }
}
