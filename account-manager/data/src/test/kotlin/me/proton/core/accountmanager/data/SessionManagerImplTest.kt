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
package me.proton.core.accountmanager.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.SessionState.ForceLogout
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SessionManagerImplTest {

    private val userId = UserId("test-user-id")

    private val error401 = ApiResult.Error.Http(HttpResponseCodes.HTTP_UNAUTHORIZED, "401")
    private val error422 = ApiResult.Error.Http(HttpResponseCodes.HTTP_UNPROCESSABLE, "422")

    private val unauthSessionId = SessionId("test-session-id-unauth")
    private val unauthSession = Session.Unauthenticated(
        sessionId = unauthSessionId,
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = emptyList()
    )
    private val authSessionId = SessionId("test-session-id-auth")
    private val authSession = Session.Authenticated(
        userId = userId,
        sessionId = authSessionId,
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        scopes = emptyList()
    )

    private val sessionListener = mockk<SessionListener>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider>(relaxed = true) {
        coEvery { this@mockk.getSessionId(userId) } returns authSessionId
        coEvery { this@mockk.getSessionId(null) } returns unauthSessionId
        coEvery { this@mockk.getSession(authSessionId) } returns authSession
        coEvery { this@mockk.getSession(unauthSessionId) } returns unauthSession
    }

    private val authRepository = mockk<AuthRepository>(relaxed = true) {
        coEvery { this@mockk.requestSession() } returns ApiResult.Success(unauthSession)
        coEvery { this@mockk.refreshSession(authSession) } returns ApiResult.Success(authSession)
        coEvery { this@mockk.refreshSession(unauthSession) } returns ApiResult.Success(unauthSession)
    }

    private val accountRepository = mockk<AccountRepository>(relaxed = true)

    private var time = 0L

    private fun mockedManager() = SessionManagerImpl(
        sessionListener = sessionListener,
        sessionProvider = sessionProvider,
        authRepository = authRepository,
        accountRepository = accountRepository,
        monoClock = { time }
    )

    @Before
    fun before() {
        SessionManagerImpl.clear()
    }

    @Test
    fun `requestSession success`() = runTest {
        // Given
        coEvery { sessionProvider.getSessionId(any()) } returns null
        coEvery { sessionProvider.getSession(any()) } returns null

        // When
        mockedManager().requestSession()

        // Then
        coVerify(exactly = 1) { authRepository.requestSession() }
        coVerify(exactly = 1) { accountRepository.createOrUpdateSession(any(), unauthSession) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenCreated(any(), unauthSession) }
    }

    @Test
    fun `requestSession error`() = runTest {
        // Given
        coEvery { sessionProvider.getSessionId(any()) } returns null
        coEvery { sessionProvider.getSession(any()) } returns null
        coEvery { authRepository.requestSession() } returns error401

        // When
        mockedManager().requestSession()

        // Then
        coVerify(exactly = 1) { authRepository.requestSession() }
        coVerify(exactly = 0) { accountRepository.createOrUpdateSession(any(), unauthSession) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenCreated(any(), unauthSession) }
    }

    @Test
    fun `requestSession unneeded`() = runTest {
        // When
        mockedManager().requestSession()

        // Then
        coVerify(exactly = 0) { authRepository.requestSession() }
        coVerify(exactly = 0) { accountRepository.createOrUpdateSession(any(), unauthSession) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenCreated(any(), unauthSession) }
    }

    @Test
    fun `refreshSession success`() = runTest {
        // When
        mockedManager().refreshSession(unauthSession)

        // Then
        coVerify(exactly = 1) { authRepository.refreshSession(unauthSession) }
        coVerify(exactly = 1) { accountRepository.updateSessionToken(unauthSession.sessionId, any(), any()) }
        coVerify(exactly = 1) { accountRepository.updateSessionScopes(unauthSession.sessionId, any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenRefreshed(unauthSession) }
    }

    @Test
    fun `refreshSession authenticated then forceLogout`() = runTest {
        // Given
        coEvery { authRepository.refreshSession(any()) } returns error422

        // When
        mockedManager().refreshSession(authSession)

        // Then
        coVerify(exactly = 1) { authRepository.refreshSession(authSession) }
        coVerify(exactly = 1) { accountRepository.updateSessionState(authSession.sessionId, ForceLogout) }
        coVerify(exactly = 1) { accountRepository.deleteSession(authSession.sessionId) }
        coVerify(exactly = 0) { authRepository.requestSession() }
        coVerify(exactly = 0) { accountRepository.createOrUpdateSession(any(), authSession) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenRefreshed(authSession) }
        coVerify(exactly = 1) { sessionListener.onSessionForceLogout(authSession, error422.httpCode) }
    }

    @Test
    fun `refreshSession unauthenticated then requestSession`() = runTest {
        // Given
        coEvery { sessionProvider.getSessionId(any()) } returns null
        coEvery { sessionProvider.getSession(any()) } returns null
        coEvery { authRepository.refreshSession(any()) } returns error422

        // When
        mockedManager().refreshSession(unauthSession)

        // Then
        coVerify(exactly = 1) { authRepository.refreshSession(unauthSession) }
        coVerify(exactly = 1) { accountRepository.updateSessionState(unauthSession.sessionId, ForceLogout) }
        coVerify(exactly = 1) { accountRepository.deleteSession(unauthSession.sessionId) }
        coVerify(exactly = 1) { authRepository.requestSession() }
        coVerify(exactly = 1) { accountRepository.createOrUpdateSession(any(), unauthSession) }
        coVerify(exactly = 1) { sessionListener.onSessionForceLogout(unauthSession, error422.httpCode) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenCreated(any(), unauthSession) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenRefreshed(unauthSession) }
    }

    @Test
    fun `refreshScopes then updateSessionScopes`() = runTest {
        // When
        mockedManager().refreshScopes(authSession.sessionId)

        // Then
        coVerify(exactly = 1) { authRepository.getScopes(any()) }
        coVerify(exactly = 1) { accountRepository.updateSessionScopes(any(), any()) }
        coVerify(exactly = 1) { sessionListener.onSessionScopesRefreshed(authSession.sessionId, any()) }
    }

    @Test
    fun `requestSession concurrent`() = runTest {
        // Given
        var current: Session.Unauthenticated? = null
        coEvery { sessionProvider.getSessionId(any()) } answers { current?.sessionId }
        coEvery { sessionProvider.getSession(any()) } answers { current }

        val sessionSlot = slot<Session.Unauthenticated>()
        coEvery { accountRepository.createOrUpdateSession(any(), capture(sessionSlot)) } answers {
            current = sessionSlot.captured
        }

        // When
        val jobs = (1..10).map { async { mockedManager().requestSession() } }

        // Then
        jobs.awaitAll()

        coVerify(exactly = 1) { authRepository.requestSession() }
        coVerify(exactly = 1) { accountRepository.createOrUpdateSession(any(), unauthSession) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenCreated(any(), unauthSession) }
    }

    @Test
    fun `refreshSession concurrent`() = runTest {
        // When
        val jobs = (1..10).map { async { mockedManager().refreshSession(unauthSession) } }

        // Then
        jobs.awaitAll()

        coVerify(exactly = 1) { authRepository.refreshSession(unauthSession) }
        coVerify(exactly = 1) { accountRepository.updateSessionToken(unauthSession.sessionId, any(), any()) }
        coVerify(exactly = 1) { accountRepository.updateSessionScopes(unauthSession.sessionId, any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenRefreshed(unauthSession) }
    }

    @Test
    fun `refreshSession concurrent but different session`() = runTest {
        // When
        val jobs1 = (1..10).map { async { mockedManager().refreshSession(unauthSession) } }
        val jobs2 = (1..10).map { async { mockedManager().refreshSession(authSession) } }

        // Then
        jobs1.awaitAll()
        jobs2.awaitAll()

        coVerify(exactly = 1) { authRepository.refreshSession(unauthSession) }
        coVerify(exactly = 1) { accountRepository.updateSessionToken(unauthSession.sessionId, any(), any()) }
        coVerify(exactly = 1) { accountRepository.updateSessionScopes(unauthSession.sessionId, any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenRefreshed(unauthSession) }

        coVerify(exactly = 1) { authRepository.refreshSession(authSession) }
        coVerify(exactly = 1) { accountRepository.updateSessionToken(authSession.sessionId, any(), any()) }
        coVerify(exactly = 1) { accountRepository.updateSessionScopes(authSession.sessionId, any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenRefreshed(authSession) }
    }

    @Test
    fun `withLock concurrent`() = runTest {
        // Given
        var zero = 0
        suspend fun modify() {
            assertEquals(expected = 0, actual = zero)
            zero++
            delay(100)
            zero--
            assertEquals(expected = 0, actual = zero)
        }
        // When
        val jobs = (1..10).map { async { mockedManager().withLock(null) { modify() } } }

        // Then
        jobs.awaitAll()

        assertEquals(expected = 0, actual = zero)
    }
}
