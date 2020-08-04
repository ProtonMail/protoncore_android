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
import me.proton.core.network.domain.UserData

/**
 * Handler for Authorization error, will attempt refreshing access token and repeat original call.
 *
 * @param Api API interface.
 * @property userData [UserData] provided by the client.
 * @property monoClockMs Monotonic clock with millisecond resolution
 * @property networkMainScope [CoroutineScope] with default single thread dispatcher.
 */
class RefreshTokenHandler<Api>(
    private val userData: UserData,
    private val monoClockMs: () -> Long,
    networkMainScope: CoroutineScope
) : OneOffJobHandler<ApiBackend<Api>, ApiResult<ApiBackend.Tokens>>(networkMainScope),
    ApiErrorHandler<Api> {

    private var lastRefreshTimeMs: Long = Long.MIN_VALUE

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> =
        if (error is ApiResult.Error.Http && error.httpCode == HTTP_UNAUTHORIZED &&
            userData.refreshToken.isNotEmpty()
        ) {
            // If request started before last token refresh there's no need for refresh
            if (call.timestampMs < lastRefreshTimeMs || startOneOffJob(backend, ::refreshTokens) is ApiResult.Success)
                backend(call)
            else
                error
        } else {
            error
        }

    // If refresh is active for another call just wait for it's result instead of starting another.
    private suspend fun refreshTokens(backend: ApiBackend<Api>): ApiResult<ApiBackend.Tokens> {
        val refreshResult = backend.refreshTokens()
        when {
            refreshResult is ApiResult.Success -> {
                userData.accessToken = refreshResult.value.access
                userData.refreshToken = refreshResult.value.refresh
                lastRefreshTimeMs = monoClockMs()
            }
            refreshResult is ApiResult.Error.Http && refreshResult.httpCode in FORCE_LOGOUT_HTTP_CODES -> {
                userData.forceLogout()
            }
        }
        return refreshResult
    }

    companion object {
        const val HTTP_UNAUTHORIZED = 401
        val FORCE_LOGOUT_HTTP_CODES = listOf(400, 422)
    }
}
