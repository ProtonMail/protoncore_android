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
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class PerformSecondFactorTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)

    private lateinit var useCase: PerformSecondFactor
    private val testSessionId = "test-session-id"
    private val testSecondFactorCode = "123456"
    private val testScope = "test-scope"
    private val testScope2 = "test-scope2"
    private val testScope3 = "test-scope3"
    private val testScopeInfo = ScopeInfo(scope = testScope, scopes = listOf(testScope2, testScope3))

    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { authRepository.performSecondFactor(SessionId(testSessionId), any()) } returns DataResult.Success(
            testScopeInfo.copy(scope = testScope), ResponseSource.Remote
        )
        useCase = PerformSecondFactor(authRepository)
    }

    @Test
    fun `second factor happy path events list is correct`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testSecondFactorCode).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformSecondFactor.SecondFactorState.Processing)
        val successEvent = listOfEvents[1]
        assertTrue(successEvent is PerformSecondFactor.SecondFactorState.Success)
        assertEquals(testSessionId, successEvent.sessionId.id)
        assertEquals(testScope, successEvent.scopeInfo.scope)
        assertEquals(2, successEvent.scopeInfo.scopes.size)
    }

    @Test
    fun `second factor happy path invocations are correct`() = runBlockingTest {
        // WHEN
        useCase.invoke(SessionId(testSessionId), testSecondFactorCode).toList()
        // THEN
        coVerify(exactly = 1) {
            authRepository.performSecondFactor(
                SessionId(testSessionId),
                SecondFactorProof.SecondFactorCode(testSecondFactorCode)
            )
        }
    }

    @Test
    fun `second factor error response events list is correct`() = runBlockingTest {
        // GIVEN
        coEvery {
            authRepository.performSecondFactor(
                SessionId(testSessionId),
                any()
            )
        } returns DataResult.Error.Message("Invalid Second Factor code", ResponseSource.Remote)
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), testSecondFactorCode).toList()
        // THEN
        assertEquals(2, listOfEvents.size)
        assertTrue(listOfEvents[0] is PerformSecondFactor.SecondFactorState.Processing)
        val errorEvent = listOfEvents[1]
        assertTrue(errorEvent is PerformSecondFactor.SecondFactorState.Error.Message)
        assertEquals("Invalid Second Factor code", errorEvent.message)
    }

    @Test
    fun `empty second factor error response events list is correct`() = runBlockingTest {
        // WHEN
        val listOfEvents = useCase.invoke(SessionId(testSessionId), "").toList()
        assertEquals(1, listOfEvents.size)
        assertIs<PerformSecondFactor.SecondFactorState.Error.EmptyCredentials>(listOfEvents[0])
    }
}
