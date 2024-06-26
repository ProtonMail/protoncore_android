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
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Dino Kadrikj.
 */
class PerformSecondFactorTest {

    private val authRepository = mockk<AuthRepository>(relaxed = true)

    private lateinit var useCase: PerformSecondFactor
    private val testSessionId = "test-session-id"
    private val testSecondFactorCode = SecondFactorProof.SecondFactorCode("123456")
    private val testScope = "test-scope"
    private val testScope2 = "test-scope2"
    private val testScope3 = "test-scope3"
    private val testScopeInfo = ScopeInfo(scope = testScope, scopes = listOf(testScope2, testScope3))

    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery {
            authRepository.performSecondFactor(
                SessionId(testSessionId),
                any()
            )
        } returns testScopeInfo.copy(scope = testScope)
        useCase = PerformSecondFactor(authRepository)
    }

    @Test
    fun `second factor happy path`() = runTest {
        // WHEN
        val scopeInfo = useCase.invoke(SessionId(testSessionId), testSecondFactorCode)
        // THEN
        assertEquals(testScope, scopeInfo.scope)
        assertEquals(2, scopeInfo.scopes.size)
    }

    @Test
    fun `second factor happy path invocations are correct`() = runTest {
        // WHEN
        useCase.invoke(SessionId(testSessionId), testSecondFactorCode)
        // THEN
        coVerify(exactly = 1) {
            authRepository.performSecondFactor(
                SessionId(testSessionId),
                testSecondFactorCode
            )
        }
    }

    @Test
    fun `second factor error response`() = runTest {
        // GIVEN
        coEvery {
            authRepository.performSecondFactor(
                SessionId(testSessionId),
                any()
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 1234,
                message = "error",
                proton = ApiResult.Error.ProtonData(1234, "Invalid Second Factor code")
            )
        )

        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(SessionId(testSessionId), testSecondFactorCode)
        }

        // THEN
        assertEquals("Invalid Second Factor code", throwable.message)
    }
}
