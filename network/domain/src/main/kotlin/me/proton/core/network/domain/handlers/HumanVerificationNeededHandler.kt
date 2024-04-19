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
import me.proton.core.network.domain.LogTag
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationAvailableMethods
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.CoreLogger
import org.jetbrains.annotations.TestOnly
import java.util.concurrent.TimeUnit

/**
 * Handles the Human Verification 9001 error response code and passes the details that come together with it to the
 * client to process it further.
 */
class HumanVerificationNeededHandler<Api>(
    private val sessionId: SessionId?,
    private val clientIdProvider: ClientIdProvider,
    private val humanVerificationListener: HumanVerificationListener,
    private val monoClockMs: () -> Long
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        // Do we have a clientId ?
        val clientId = clientIdProvider.getClientId(sessionId)
        if (clientId == null) {
            CoreLogger.w(LogTag.DEFAULT, "HumanVerificationNeededHandler: clientId not found.")
            return error
        }

        // Recoverable with human verification ?
        if (error !is ApiResult.Error.Http || error.proton?.code != ResponseCodes.HUMAN_VERIFICATION_REQUIRED) {
            return error
        }

        // Do we have details ?
        val details = error.proton.humanVerification ?: return error

        // Only 1 coroutine at a time per clientId.
        val shouldRetry = clientMutex(clientId).withLock {
            // Don't attempt to verify if we did recently.
            val lastVerificationTimeMs = clientLastVerificationMap[clientId] ?: Long.MIN_VALUE
            val verifiedRecently = monoClockMs() <= lastVerificationTimeMs + verificationDebounceMs
            verifiedRecently || verifyHuman(clientId, details)
        }
        return if (shouldRetry) {
            backend(call)
        } else {
            error
        }
    }

    // Must be called within clientMutex.
    private suspend fun verifyHuman(clientId: ClientId, methods: HumanVerificationAvailableMethods): Boolean {
        return try {
            when (humanVerificationListener.onHumanVerificationNeeded(clientId, methods)) {
                HumanVerificationListener.HumanVerificationResult.Success -> true
                HumanVerificationListener.HumanVerificationResult.Failure -> false
            }
        } finally {
            // Make sure we update the last verification time even if the coroutine is cancelled.
            clientLastVerificationMap[clientId] = monoClockMs()
        }
    }

    companion object {
        private val verificationDebounceMs = TimeUnit.SECONDS.toMillis(5)
        private val staticMutex: Mutex = Mutex()
        private val clientMutexMap: MutableMap<ClientId, Mutex> = HashMap()
        private var clientLastVerificationMap: MutableMap<ClientId, Long> = HashMap()

        suspend fun clientMutex(clientId: ClientId) =
            staticMutex.withLock { clientMutexMap.getOrPut(clientId) { Mutex() } }

        @TestOnly
        suspend fun reset(clientId: ClientId) =
            clientMutex(clientId).withLock { clientLastVerificationMap[clientId] = Long.MIN_VALUE }
    }
}
