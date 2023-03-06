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
package me.proton.core.network.domain.handlers

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes.HTTP_BAD_REQUEST
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNAUTHORIZED
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.session.ResolvedSession.Found
import me.proton.core.network.domain.session.ResolvedSession.NotFound
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.network.domain.session.getResolvedSession
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.TimeUnit

/**
 * Handler for Authorization error, will attempt refreshing access token and repeat original call.
 *
 * @param Api API interface.
 *
 * @param sessionId optional [SessionId].
 * @param sessionProvider a [SessionProvider] to get the tokens from.
 * @param sessionListener a [SessionListener] to inform session changed.
 * @param monoClockMs Monotonic clock with millisecond resolution.
 */
class TokenErrorHandler<Api>(
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener,
    private val monoClockMs: () -> Long
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        // Recoverable with refresh or request Token ?
        if (error !is ApiResult.Error.Http || error.httpCode != HTTP_UNAUTHORIZED) return error

        // Only 1 coroutine at a time per session.
        val shouldRetry = sessionMutex(sessionId).withLock {
            when (val resolved = sessionProvider.getResolvedSession(sessionId)) {
                is NotFound.Authenticated -> false
                is NotFound.Unauthenticated -> requestToken(backend)
                is Found -> refreshToken(resolved, backend)
            }
        }
        return if (shouldRetry) backend(call) else error
    }

    // Must be called within sessionMutex.
    private suspend fun refreshToken(resolved: Found, backend: ApiBackend<Api>): Boolean {
        val lastRefreshTimeMs = sessionLastRefreshMap[resolved.session.sessionId] ?: Long.MIN_VALUE
        val refreshedRecently = monoClockMs() <= lastRefreshTimeMs + refreshDebounceMs
        // Don't attempt if refreshed recently. Prevent race issues.
        if (refreshedRecently) return true
        // Try to refresh session.
        return when (val apiResult = backend.refreshSession(resolved.session)) {
            is ApiResult.Success -> {
                sessionLastRefreshMap[resolved.session.sessionId] = monoClockMs()
                sessionListener.onSessionTokenRefreshed(apiResult.value)
                true
            }
            is ApiResult.Error.Http -> {
                when (apiResult.httpCode) {
                    in FORCE_LOGOUT_HTTP_CODES -> {
                        sessionListener.onSessionForceLogout(resolved.session)
                        // Retry the call if it was an unauthenticated session and got new one.
                        resolved is Found.Unauthenticated && requestToken(backend)
                    }
                    else -> false
                }
            }
            else -> false
        }
    }

    // Must be called within sessionMutex.
    private suspend fun requestToken(backend: ApiBackend<Api>): Boolean {
        return when (val apiResult = backend.requestSession()) {
            is ApiResult.Success -> {
                sessionListener.onSessionTokenCreated(apiResult.value)
                true
            }
            else -> false
        }
    }

    companion object {
        val FORCE_LOGOUT_HTTP_CODES = listOf(
            HTTP_UNAUTHORIZED,
            HTTP_BAD_REQUEST,
            HTTP_UNPROCESSABLE
        )

        private val refreshDebounceMs = TimeUnit.MINUTES.toMillis(1)
        private val staticMutex: Mutex = Mutex()
        private val sessionMutexMap: MutableMap<SessionId?, Mutex> = HashMap()
        private var sessionLastRefreshMap: MutableMap<SessionId?, Long> = HashMap()

        suspend fun sessionMutex(sessionId: SessionId?) =
            staticMutex.withLock { sessionMutexMap.getOrPut(sessionId) { Mutex() } }

        @TestOnly
        suspend fun reset(sessionId: SessionId?) =
            sessionMutex(sessionId).withLock { sessionLastRefreshMap[sessionId] = Long.MIN_VALUE }

        @TestOnly
        fun clear() = sessionLastRefreshMap.clear()
    }
}
