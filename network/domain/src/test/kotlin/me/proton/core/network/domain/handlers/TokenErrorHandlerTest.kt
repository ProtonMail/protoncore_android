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
    private val unauthSession = mockk<Session.Unauthenticated> {
        every { sessionId } returns unauthSessionId
    }
    private val authSessionId = SessionId("test-session-id-auth")
    private val authSession = mockk<Session.Authenticated> {
        every { sessionId } returns authSessionId
    }

    private val call = mockk<ApiManager.Call<Any, Any>>()
    private val apiBackend = mockk<ApiBackend<Any>>(relaxed = true) {
        coEvery { this@mockk.invoke<Any>(any()) } returns ApiResult.Success("success")
    }

    private val sessionListener = mockk<SessionListener>(relaxed = true) {
        coEvery { this@mockk.requestSession() } returns true
        coEvery { this@mockk.refreshSession(unauthSession) } returns true
        coEvery { this@mockk.refreshSession(authSession) } returns true
    }
    private val sessionProvider = mockk<SessionProvider>(relaxed = true) {
        coEvery { this@mockk.getSessionId(userId) } returns authSessionId
        coEvery { this@mockk.getSessionId(null) } returns unauthSessionId
        coEvery { this@mockk.getSession(authSessionId) } returns authSession
        coEvery { this@mockk.getSession(unauthSessionId) } returns unauthSession
    }

    @Test
    fun `return error without retry, unless 401`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = authSessionId,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
        )
        val error = ApiResult.Error.Http(HTTP_FORBIDDEN, "403")

        // When
        val result = handler.invoke(apiBackend, error, call)

        // Then
        assertIs<ApiResult.Error>(result)

        coVerify(exactly = 0) { apiBackend.invoke(call) }
        coVerify(exactly = 0) { sessionListener.refreshSession(any()) }
        coVerify(exactly = 0) { sessionListener.requestSession() }
    }

    @Test
    fun `request unauth on 401 given no session, then retry`() = runTest {
        // Given
        coEvery { sessionProvider.getSession(any()) } returns null

        val handler = TokenErrorHandler<Any>(
            sessionId = null,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
        )
        val error = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")

        // When
        handler.invoke(apiBackend, error, call)

        // Then
        coVerify(exactly = 0) { sessionListener.refreshSession(any()) }
        coVerify(exactly = 1) { sessionListener.requestSession() }
        coVerify(exactly = 1) { apiBackend.invoke(call) }
    }

    @Test
    fun `refresh auth on 401, then retry`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = authSessionId,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
        )
        val error = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")

        // When
        handler.invoke(apiBackend, error, call)

        // Then
        coVerify(exactly = 1) { sessionListener.refreshSession(authSession) }
        coVerify(exactly = 0) { sessionListener.requestSession() }
        coVerify(exactly = 1) { apiBackend.invoke(call) }
    }

    @Test
    fun `refresh auth on 401 given auth session, refresh fail`() = runTest {
        // Given
        val handler = TokenErrorHandler<Any>(
            sessionId = authSessionId,
            sessionProvider = sessionProvider,
            sessionListener = sessionListener,
        )
        val error401 = ApiResult.Error.Http(HTTP_UNAUTHORIZED, "401")

        coEvery { sessionListener.refreshSession(authSession) } returns false

        // When
        val result = handler.invoke(apiBackend, error401, call)

        // Then
        assertIs<ApiResult.Error>(result)

        coVerify(exactly = 1) { sessionListener.refreshSession(authSession) }
        coVerify(exactly = 0) { sessionListener.requestSession() }
        coVerify(exactly = 0) { apiBackend.invoke(call) }
    }
}
