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

import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNAUTHORIZED
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider

/**
 * Handler for Authorization error, will attempt refreshing access token and repeat original call.
 *
 * @param Api API interface.
 *
 * @param sessionId optional [SessionId].
 * @param sessionProvider a [SessionProvider] to get the tokens from.
 */
class TokenErrorHandler<Api>(
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        // Recoverable with refresh or request Token ?
        if (error !is ApiResult.Error.Http || error.httpCode != HTTP_UNAUTHORIZED) return error

        // Any existing Session ?
        val shouldRetry = when (val session = sessionProvider.getSession(sessionId)) {
            null -> sessionListener.requestSession()
            else -> sessionListener.refreshSession(session)
        }
        return if (shouldRetry) backend(call) else error
    }
}
