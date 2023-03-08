/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.network.domain.handlers

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes.HTTP_FORBIDDEN
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNAUTHORIZED
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test

class TokenErrorHandlerTest {

    private val userId = UserId("test-user-id")

    private val unauthSessionId = SessionId("test-session-id-unauth")
    private val unauthSession = mockk<Session> {
        every { sessionId } returns unauthSessionId
    }
    private val authSessionId = SessionId("test-session-id-auth")
    private val authSession = mockk<Session> {
        every { sessionId } returns authSessionId
    }

    private val call = mockk<ApiManager.Call<Any, Any>>()
    private val apiBackend = mockk<ApiBackend<Any>>(relaxed = true) {
        coEvery { this@mockk.invoke<Any>(any()) } returns ApiResult.Success("success")
        coEvery { this@mockk.requestSession() } returns ApiResult.Success(unauthSession)
        coEvery { this@mockk.refreshSession(unauthSession) } returns ApiResult.Success(unauthSession)
        coEvery { this@mockk.refreshSession(authSession) } returns ApiResult.Success(authSession)
    }

    private val sessionListener = mockk<SessionListener>(relaxed = true)
    private val sessionProvider = mockk<SessionProvider>(relaxed = true) {
        coEvery { this@mockk.getSessionId(userId) } returns authSessionId
        coEvery { this@mockk.getSessionId(null) } returns unauthSessionId
        coEvery { this@mockk.getSession(authSessionId) } returns authSession
        coEvery { this@mockk.getSession(unauthSessionId) } returns unauthSession
    }

    @Before
    fun before() = runTest {
        TokenErrorHandler.clear()
    }

    @Test
    fun `return error without retry, unless 401`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = authSessionId,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
            monoClockMs = { currentTime }
        )
        val error = ApiResult.Error.Http(HTTP_FORBIDDEN, "403")

        // When
        val result = handler.invoke(apiBackend, error, call)

        // Then
        assertIs<ApiResult.Error>(result)

        coVerify(exactly = 0) { apiBackend.invoke(call) }
        coVerify(exactly = 0) { apiBackend.refreshSession(any()) }
        coVerify(exactly = 0) { apiBackend.requestSession() }
        coVerify(exactly = 0) { sessionListener.onSessionForceLogout(any(), any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenCreated(any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenRefreshed(any()) }
    }

    @Test
    fun `request unauth on 401 given no session, then retry`() = runTest {
        // Given
        coEvery { sessionProvider.getSessionId(userId = null) } returns null

        val handler = TokenErrorHandler<Any>(
            sessionId = null,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
            monoClockMs = { currentTime }
        )
        val error = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")

        // When
        val result = handler.invoke(apiBackend, error, call)

        // Then
        assertIs<ApiResult.Success<Any>>(result)

        coVerify(exactly = 1) { apiBackend.invoke(call) }
        coVerify(exactly = 0) { apiBackend.refreshSession(any()) }
        coVerify(exactly = 1) { apiBackend.requestSession() }
        coVerify(exactly = 0) { sessionListener.onSessionForceLogout(any(), any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenCreated(any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenRefreshed(any()) }
    }

    @Test
    fun `refresh auth on 401 given auth session, then retry`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = authSessionId,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
            monoClockMs = { currentTime }
        )
        val error = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")

        // When
        val result = handler.invoke(apiBackend, error, call)

        // Then
        assertIs<ApiResult.Success<Any>>(result)

        coVerify(exactly = 1) { apiBackend.invoke(call) }
        coVerify(exactly = 1) { apiBackend.refreshSession(authSession) }
        coVerify(exactly = 0) { apiBackend.requestSession() }
        coVerify(exactly = 0) { sessionListener.onSessionForceLogout(any(), any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenCreated(any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenRefreshed(any()) }
    }

    @Test
    fun `refresh unauth on 401 given no session, then retry`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = null,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
            monoClockMs = { currentTime }
        )
        val error = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")

        // When
        val result = handler.invoke(apiBackend, error, call)

        // Then
        assertIs<ApiResult.Success<Any>>(result)

        coVerify(exactly = 1) { apiBackend.invoke(call) }
        coVerify(exactly = 1) { apiBackend.refreshSession(unauthSession) }
        coVerify(exactly = 0) { apiBackend.requestSession() }
        coVerify(exactly = 0) { sessionListener.onSessionForceLogout(any(), any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenCreated(any()) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenRefreshed(any()) }
    }

    @Test
    fun `refresh unauth on 401 given no session, refresh fail, request token, retry`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = null,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
            monoClockMs = { currentTime }
        )
        val error401 = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")
        val error422 = ApiResult.Error.Http(HTTP_UNPROCESSABLE, "422")

        coEvery { apiBackend.refreshSession(unauthSession) } returns error422

        // When
        val result = handler.invoke(apiBackend, error401, call)

        // Then
        assertIs<ApiResult.Success<Any>>(result)

        coVerify(exactly = 1) { apiBackend.invoke(call) }
        coVerify(exactly = 1) { apiBackend.refreshSession(unauthSession) }
        coVerify(exactly = 1) { apiBackend.requestSession() }
        coVerify(exactly = 1) { sessionListener.onSessionForceLogout(any(), 422) }
        coVerify(exactly = 1) { sessionListener.onSessionTokenCreated(any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenRefreshed(any()) }
    }

    @Test
    fun `refresh auth on 401 given auth session, refresh fail, no retry`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = authSessionId,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
            monoClockMs = { currentTime }
        )
        val error401 = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")
        val error422 = ApiResult.Error.Http(HTTP_UNPROCESSABLE, "422")

        coEvery { apiBackend.refreshSession(authSession) } returns error422

        // When
        val result = handler.invoke(apiBackend, error401, call)

        // Then
        assertIs<ApiResult.Error>(result)

        coVerify(exactly = 0) { apiBackend.invoke(call) }
        coVerify(exactly = 1) { apiBackend.refreshSession(authSession) }
        coVerify(exactly = 0) { apiBackend.requestSession() }
        coVerify(exactly = 1) { sessionListener.onSessionForceLogout(any(), 422) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenCreated(any()) }
        coVerify(exactly = 0) { sessionListener.onSessionTokenRefreshed(any()) }
    }
}
