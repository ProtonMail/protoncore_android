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

import kotlinx.coroutines.CoroutineScope
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
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
 * @param networkMainScope [CoroutineScope] with default single thread dispatcher.
 */
class RefreshTokenHandler<Api>(
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener,
    private val monoClockMs: () -> Long,
    networkMainScope: CoroutineScope
) : OneOffJobHandler<ApiBackend<Api>, ApiResult<Session>>(networkMainScope),
    ApiErrorHandler<Api> {

    private var lastRefreshTimeMs: Long = Long.MIN_VALUE

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        // Recoverable with refreshToken ?
        if (error !is ApiResult.Error.Http || error.httpCode != HTTP_UNAUTHORIZED) return error

        // Do we have a refreshToken ?
        val session: Session? = sessionId?.let { sessionProvider.getSession(it) }
        if (session == null || session.refreshToken.isBlank()) return error

        // Don't attempt to refresh if successful refresh completed recently.
        val refreshedRecently = call.timestampMs <= lastRefreshTimeMs + REFRESH_COOL_DOWN_MS
        if (refreshedRecently || startOneOffJob(backend) { refreshTokens(session, backend) } is ApiResult.Success)
            return backend(call)

        return error
    }

    // If refresh is active for another call just wait for it's result instead of starting another.
    private suspend fun refreshTokens(session: Session, backend: ApiBackend<Api>): ApiResult<Session> {
        val apiResult = backend.refreshSession(session)
        when {
            apiResult is ApiResult.Success -> {
                sessionListener.onSessionTokenRefreshed(apiResult.value)
                lastRefreshTimeMs = monoClockMs()
            }
            apiResult is ApiResult.Error.Http && apiResult.httpCode in FORCE_LOGOUT_HTTP_CODES -> {
                sessionListener.onSessionForceLogout(session)
            }
        }
        return apiResult
    }

    companion object {
        const val HTTP_UNAUTHORIZED = 401
        val FORCE_LOGOUT_HTTP_CODES = listOf(400, 422)
        val REFRESH_COOL_DOWN_MS = TimeUnit.MINUTES.toMillis(1)
    }
}
