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

package me.proton.core.accountmanager.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionProviderImplTest {

    private val accountRepository = mockk<AccountRepository>()

    private lateinit var sessionProvider: SessionProviderImpl

    @Before
    fun beforeEveryTest() {
        sessionProvider = SessionProviderImpl(accountRepository)
    }

    @Test
    fun `on non-null session`() = runTest {
        // GIVEN
        val sessionId = SessionId("test-session-id")
        val session = Session.Authenticated(
            userId = UserId("test-user-id"),
            sessionId = sessionId,
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            scopes = emptyList(),
        )
        coEvery { accountRepository.getSessionOrNull(sessionId) } returns session
        // WHEN
        val result = sessionProvider.getSession(sessionId)

        // THEN
        coVerify { accountRepository.getSessionOrNull(sessionId) }
        assertNotNull(result)
        assertEquals(sessionId, result.sessionId)
    }

    @Test
    fun `on un-auth session`() = runTest {
        // GIVEN
        val sessionId = SessionId("test-session-id")
        val session = Session.Unauthenticated(
            sessionId = sessionId,
            accessToken = "test-access-token",
            refreshToken = "test-refresh-token",
            scopes = emptyList(),
        )
        coEvery { accountRepository.getSessionIdOrNull(null) } returns sessionId
        coEvery { accountRepository.getSessionOrNull(sessionId) } returns session
        // WHEN
        val result = sessionProvider.getSession(null)

        // THEN
        coVerify { accountRepository.getSessionIdOrNull(null) }
        coVerify { accountRepository.getSessionOrNull(sessionId) }
        assertNotNull(result)
        assertEquals(sessionId, result.sessionId)
    }

    @Test
    fun `on null session`() = runTest {
        // GIVEN
        val sessionId = SessionId("test-session-id")
        coEvery { accountRepository.getSessionIdOrNull(null) } returns null
        // WHEN
        val result = sessionProvider.getSession(null)

        // THEN
        coVerify { accountRepository.getSessionIdOrNull(null) }
        assertNull(result)
    }

    @Test
    fun `on get session id for user`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        val sessionId = SessionId("test-session-id")
        coEvery { accountRepository.getSessionIdOrNull(userId) } returns sessionId
        // WHEN
        val result = sessionProvider.getSessionId(userId)

        // THEN
        coVerify { accountRepository.getSessionIdOrNull(userId) }
        assertNotNull(result)
    }

    @Test
    fun `on get session id no user`() = runTest {
        // GIVEN
        val sessionId = SessionId("test-session-id")
        coEvery { accountRepository.getSessionIdOrNull(null) } returns sessionId
        // WHEN
        val result = sessionProvider.getSessionId(null)

        // THEN
        coVerify { accountRepository.getSessionIdOrNull(null) }
        assertNotNull(result)
    }

    @Test
    fun `on get user id`() = runTest {
        // GIVEN
        val userId = UserId("test-user-id")
        val sessionId = SessionId("test-session-id")
        val account = Account(
            userId = userId,
            username = "username",
            email = "test@example.com",
            state = AccountState.Ready,
            sessionId = sessionId,
            sessionState = SessionState.Authenticated,
            details = AccountDetails(null, null)
        )
        coEvery { accountRepository.getAccountOrNull(sessionId) } returns account
        // WHEN
        val result = sessionProvider.getUserId(sessionId)

        // THEN
        coVerify { accountRepository.getAccountOrNull(sessionId) }
        assertNotNull(result)
        assertEquals(userId, result)
    }
}