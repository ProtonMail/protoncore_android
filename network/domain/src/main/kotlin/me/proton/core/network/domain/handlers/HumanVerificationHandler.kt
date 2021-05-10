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
import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.network.domain.session.ClientId
import me.proton.core.network.domain.session.HumanVerificationListener
import me.proton.core.network.domain.session.SessionId
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.TimeUnit

/**
 * Handles the 9001 error response code and passes the details that come together with it to the
 * client to process it further.
 */
class HumanVerificationHandler<Api>(
    private val clientId: ClientId?,
    private val humanVerificationListener: HumanVerificationListener,
    private val monoClockMs: () -> Long
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        if (clientId == null) return error

        // Recoverable with human verification ?
        if (error !is ApiResult.Error.Http || error.proton?.code != ERROR_CODE_HUMAN_VERIFICATION) return error

        // Do we have details ?
        val details = error.proton.humanVerification ?: return error

        // Do we have a session ?
        val sessionId = if (clientId is ClientId.AccountSession) clientId.sessionId else null

        // Only 1 coroutine at a time per session.
        val shouldRetry = sessionMutex(sessionId).withLock {
            // Don't attempt to verify if we did recently.
            val lastVerificationTimeMs = sessionLastVerificationMap[sessionId] ?: Long.MIN_VALUE
            val verifiedRecently = monoClockMs() <= lastVerificationTimeMs + verificationDebounceMs
            verifiedRecently || verifyHuman(clientId, details)
        }
        return if (shouldRetry) {
            val retryResult = backend(call)
            if (retryResult is ApiResult.Error.Http) {
                humanVerificationListener.onHumanVerificationFailed(clientId)
            } else if (retryResult is ApiResult.Success) {
                humanVerificationListener.onHumanVerificationPassed(clientId)
            }
            retryResult
        } else error
    }

    // Must be called within sessionMutex.
    private suspend fun verifyHuman(clientId: ClientId, details: HumanVerificationApiDetails): Boolean {
        val result = when (humanVerificationListener.onHumanVerificationNeeded(clientId, details)) {
            HumanVerificationListener.HumanVerificationResult.Success -> true
            HumanVerificationListener.HumanVerificationResult.Failure -> false
        }
        // what is this map for?
        sessionLastVerificationMap[clientId.id] = monoClockMs()
        return result
    }

    companion object {
        const val ERROR_CODE_HUMAN_VERIFICATION = 9001

        private val verificationDebounceMs = TimeUnit.MINUTES.toMillis(1)
        private val staticMutex: Mutex = Mutex()
        private val sessionMutexMap: MutableMap<SessionId?, Mutex> = HashMap()
        private var sessionLastVerificationMap: MutableMap<String, Long> = HashMap()

        suspend fun sessionMutex(sessionId: SessionId?) =
            staticMutex.withLock { sessionMutexMap.getOrPut(sessionId) { Mutex() } }

        @TestOnly
        suspend fun reset(clientId: ClientId) {
            when (clientId) {
                is ClientId.AccountSession -> sessionMutex(clientId.sessionId).withLock {
                    sessionLastVerificationMap[clientId.id] = Long.MIN_VALUE
                }
                is ClientId.CookieSession -> {
                }
            }
        }
    }
}
