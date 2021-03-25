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
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import org.jetbrains.annotations.TestOnly
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Handles the 9001 error response code and passes the details that come together with it to the
 * client to process it further.
 */
class HumanVerificationHandler<Api>(
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
        // Recoverable with human verification ?
        if (error !is ApiResult.Error.Http || error.proton?.code != ERROR_CODE_HUMAN_VERIFICATION) return error

        // Do we have details ?
        val details = error.proton.humanVerification ?: return error

        // Do we have a session ?
        val session = sessionId?.let { sessionProvider.getSession(sessionId) } ?: return error

        // Only 1 coroutine at a time per session.
        val shouldRetry = sessionMutex(sessionId).withLock {
            // Don't attempt to verify if we did recently.
            val lastVerificationTimeMs = sessionLastVerificationMap[sessionId] ?: Long.MIN_VALUE
            val verifiedRecently = monoClockMs() <= lastVerificationTimeMs + verificationDebounceMs
            verifiedRecently || verifyHuman(session, details)
        }
        return if (shouldRetry) backend(call) else error
    }

    // Must be called within sessionMutex.
    private suspend fun verifyHuman(session: Session, details: HumanVerificationDetails): Boolean {
        val result = when (sessionListener.onHumanVerificationNeeded(session, details)) {
            SessionListener.HumanVerificationResult.Success -> true
            SessionListener.HumanVerificationResult.Failure -> false
        }
        sessionLastVerificationMap[session.sessionId] = monoClockMs()
        return result
    }

    companion object {
        const val ERROR_CODE_HUMAN_VERIFICATION = 9001

        private val verificationDebounceMs = TimeUnit.MINUTES.toMillis(1)
        private val staticMutex: Mutex = Mutex()
        private val sessionMutexMap: MutableMap<SessionId?, Mutex> = HashMap()
        private var sessionLastVerificationMap: MutableMap<SessionId, Long> = HashMap()

        suspend fun sessionMutex(sessionId: SessionId?) =
            staticMutex.withLock { sessionMutexMap.getOrPut(sessionId) { Mutex() } }

        @TestOnly
        suspend fun reset(sessionId: SessionId) =
            sessionMutex(sessionId).withLock { sessionLastVerificationMap[sessionId] = Long.MIN_VALUE }
    }
}
