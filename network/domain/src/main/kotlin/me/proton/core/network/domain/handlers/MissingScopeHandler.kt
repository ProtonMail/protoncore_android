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
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import java.net.HttpURLConnection.HTTP_FORBIDDEN

/**
 * Handles the Missing one of the security scopes [LOCKED or PASSWORD] that is required for some of the operations
 * the user might want to execute.
 */
class MissingScopeHandler<Api>(
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val missingScopeListener: MissingScopeListener
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        if (sessionId == null) return error

        if (error !is ApiResult.Error.Http || error.httpCode != HTTP_FORBIDDEN) return error

        val userId = sessionProvider.getUserId(sessionId) ?: return error

        val details = error.proton?.missingScopes ?: return error

        val scopes = details.scopes
        if (scopes.isNullOrEmpty()) return error

        val shouldRetry = sessionMutex(sessionId).withLock {
            obtainMissingScope(userId, scopes)
        }
        return if (shouldRetry) {
            backend(call)
        } else {
            error
        }
    }

    private suspend fun obtainMissingScope(userId: UserId, details: List<Scope>): Boolean {
        return when (missingScopeListener.onMissingScope(userId, details)) {
            MissingScopeResult.Success -> true
            MissingScopeResult.Failure -> false
        }
    }

    companion object {
        private val staticMutex: Mutex = Mutex()
        private val clientMutexMap: MutableMap<SessionId, Mutex> = HashMap()

        suspend fun sessionMutex(sessionId: SessionId): Mutex =
            staticMutex.withLock { clientMutexMap.getOrPut(sessionId) { Mutex() } }
    }
}
