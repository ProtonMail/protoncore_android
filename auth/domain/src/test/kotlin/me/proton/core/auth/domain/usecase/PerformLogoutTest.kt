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
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class PerformLogoutTest {
    private val authRepository = mockk<AuthRepository>(relaxed = true)

    private lateinit var useCase: PerformLogout
    private val testSessionId = "test-session-id"

    @Before
    fun beforeEveryTest() {
        // GIVEN
        useCase = PerformLogout(authRepository)
        coEvery { authRepository.revokeSession(SessionId(testSessionId)) } returns true
    }

    @Test
    fun `logout happy path`() = runTest {
        // WHEN
        val response = useCase.invoke(SessionId(testSessionId))
        // THEN
        assertTrue(response)
    }

    @Test
    fun `logout happy path invocations are correct`() = runTest {
        // WHEN
        useCase.invoke(SessionId(testSessionId))
        // THEN
        coVerify(exactly = 1) { authRepository.revokeSession(SessionId(testSessionId)) }
    }
}
