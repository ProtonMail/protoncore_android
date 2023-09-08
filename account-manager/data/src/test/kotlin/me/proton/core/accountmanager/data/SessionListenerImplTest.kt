/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.accountmanager.domain.LogTag.SESSION_CREATE
import me.proton.core.accountmanager.domain.LogTag.SESSION_FORCE_LOGOUT
import me.proton.core.accountmanager.domain.LogTag.SESSION_REFRESH
import me.proton.core.accountmanager.domain.LogTag.SESSION_SCOPES
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.CoreLogger
import org.junit.Before
import org.junit.Test

class SessionListenerImplTest {

    private val userId = UserId("test-user-id")

    private val error400 = HttpResponseCodes.HTTP_BAD_REQUEST
    private val error401 = HttpResponseCodes.HTTP_UNAUTHORIZED
    private val error422 = HttpResponseCodes.HTTP_UNPROCESSABLE

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

    private val logger = mockk<CoreLogger>(relaxed = true)
    private val sessionManager = mockk<SessionManager>(relaxed = true) {
        coEvery { this@mockk.getSession(unauthSessionId) } returns unauthSession
        coEvery { this@mockk.getSession(authSessionId) } returns authSession
    }
    private lateinit var sessionListener: SessionListenerImpl

    @Before
    fun setup() {
        CoreLogger.set(logger)
        sessionListener = SessionListenerImpl { sessionManager }
    }

    @Test
    fun requestSession() = runTest {
        // When
        sessionListener.requestSession()
        // Then
        coVerify { sessionManager.requestSession() }
    }

    @Test
    fun refreshSession() = runTest {
        // When
        sessionListener.refreshSession(authSession)
        sessionListener.refreshSession(unauthSession)
        // Then
        coVerify { sessionManager.refreshSession(authSession) }
        coVerify { sessionManager.refreshSession(unauthSession) }
    }

    @Test
    fun onSessionTokenCreated() = runTest {
        // When
        sessionListener.onSessionTokenCreated(userId, authSession)
        // Then
        coVerify { logger.i(SESSION_CREATE, any()) }
    }

    @Test
    fun onSessionTokenRefreshed() = runTest {
        // When
        sessionListener.onSessionTokenRefreshed(authSession)
        // Then
        coVerify { logger.i(SESSION_REFRESH, any()) }
    }

    @Test
    fun onSessionScopesRefreshed() = runTest {
        // When
        sessionListener.onSessionScopesRefreshed(authSession.sessionId, listOf("scope1", "scope2"))
        // Then
        coVerify { logger.i(SESSION_SCOPES, any()) }
    }

    @Test
    fun onSessionForceLogout422() = runTest {
        // When
        sessionListener.onSessionForceLogout(authSession, error422)
        sessionListener.onSessionForceLogout(unauthSession, error422)
        // Then
        coVerify { logger.i(SESSION_FORCE_LOGOUT, any()) }
    }

    @Test
    fun onSessionForceLogout401() = runTest {
        // When
        sessionListener.onSessionForceLogout(authSession, error401)
        sessionListener.onSessionForceLogout(unauthSession, error401)
        // Then
        coVerify { logger.i(SESSION_FORCE_LOGOUT, any()) }
    }

    @Test
    fun onSessionForceLogout400() = runTest {
        // When
        sessionListener.onSessionForceLogout(authSession, error400)
        // Then
        coVerify { logger.e(SESSION_FORCE_LOGOUT, any<String>()) }
    }

    @Test
    fun onSessionForceLogout400Unauthenticated() = runTest {
        // When
        sessionListener.onSessionForceLogout(unauthSession, error400)
        // Then
        coVerify(exactly = 0) { logger.e(SESSION_FORCE_LOGOUT, any<String>()) }
    }

    @Test
    fun onSessionForceLogout400Authenticated() = runTest {
        // When
        sessionListener.onSessionForceLogout(authSession, error400)
        // Then
        coVerify(exactly = 1) { logger.e(SESSION_FORCE_LOGOUT, any<String>()) }
    }
}
