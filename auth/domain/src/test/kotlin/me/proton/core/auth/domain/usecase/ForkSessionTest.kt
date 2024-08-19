/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.assertEquals
import org.junit.Before
import org.junit.Test

class ForkSessionTest {
    private val userId = UserId("user-id")
    private lateinit var authRepository: AuthRepository
    private lateinit var sessionProvider: SessionProvider
    private lateinit var tested: ForkSession

    @Before
    fun setUp() {
        authRepository = mockk()
        sessionProvider = mockk()
        tested = ForkSession(authRepository, sessionProvider)
    }

    @Test
    fun `happy path`() = runTest {
        // GIVEN
        coEvery { sessionProvider.getSessionId(userId) } returns sessionId
        coEvery { authRepository.forkSession(any(), any(), any(), any(), null) } returns SELECTOR

        // WHEN
        val selector = tested(
            userId = userId,
            payload = PAYLOAD,
            childClientId = CHILD_CLIENT_ID,
            independent = false,
        )

        // THEN
        coVerify {
            sessionProvider.getSessionId(userId)
            authRepository.forkSession(
                sessionId = sessionId,
                payload = PAYLOAD,
                childClientId = CHILD_CLIENT_ID,
                independent = 0L,
            )
        }
        assertEquals(SELECTOR, selector) { "$SELECTOR should be returned" }
    }

    @Test(expected = IllegalStateException::class)
    fun `unauthenticated user cannot fork a session`() = runTest {
        // GIVEN
        coEvery { sessionProvider.getSessionId(userId) } returns null
        coEvery { authRepository.forkSession(any(), any(), any(), any(), null) } returns SELECTOR

        // WHEN
        tested(
            userId = userId,
            payload = PAYLOAD,
            childClientId = CHILD_CLIENT_ID,
            independent = false,
        )
    }

    private val sessionId: SessionId get() = SessionId("session-id-${userId.id}")

    companion object {
        private const val CHILD_CLIENT_ID = "child-client-id"
        private const val PAYLOAD = "payload"
        private const val SELECTOR = "selector"
    }
}
