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
import me.proton.core.network.domain.ResponseCodes.MISSING_SCOPE
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.network.domain.session.SessionId

/**
 * Handles the Missing one of the security scopes [LOCKED or PASSWORD] that is required for some of the operations
 * the user might want to execute.
 */
class MissingScopeHandler<Api>(
    private val sessionId: SessionId?,
    private val clientIdProvider: ClientIdProvider,
    private val missingScopeListener: MissingScopeListener
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        val clientId = clientIdProvider.getClientId(sessionId) ?: return error

        if (error !is ApiResult.Error.Http || error.proton?.code != MISSING_SCOPE) return error

        val details = error.proton.missingScopes ?: return error

        val scopes = details.scopes
        if (scopes.isNullOrEmpty()) return error

        val shouldRetry = clientMutex(clientId).withLock {
            obtainMissingScope(scopes.first())
        }
        val finalResult = if (shouldRetry) {
            backend(call)
        } else {
            error
        }
        return finalResult
    }

    private suspend fun obtainMissingScope(details: Scope?): Boolean {
        return when (missingScopeListener.onMissingScope(details!!)) {
            MissingScopeResult.Success -> true
            MissingScopeResult.Failure -> false
        }
    }

    companion object {
        private val staticMutex: Mutex = Mutex()
        private val clientMutexMap: MutableMap<ClientId, Mutex> = HashMap()

        suspend fun clientMutex(clientId: ClientId): Mutex =
            staticMutex.withLock { clientMutexMap.getOrPut(clientId) { Mutex() } }
    }
}
