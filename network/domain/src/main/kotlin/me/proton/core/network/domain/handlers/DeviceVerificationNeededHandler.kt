/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener
import me.proton.core.network.domain.deviceverification.DeviceVerificationListener.DeviceVerificationResult
import me.proton.core.network.domain.session.ResolvedSession
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.network.domain.session.getResolvedSession

class DeviceVerificationNeededHandler<Api>(
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val deviceVerificationListener: DeviceVerificationListener,
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        // Return the error if it's not a 9002 error
        if (error !is ApiResult.Error.Http || error.proton?.code != ResponseCodes.DEVICE_VERIFICATION_REQUIRED) {
            return error
        }
        // Return the error if there are no details in the error
        val details = error.proton.deviceVerification ?: return error

        val sessionId = when (val resolvedSession = sessionProvider.getResolvedSession(sessionId)) {
            is ResolvedSession.NotFound -> return error
            is ResolvedSession.Found -> resolvedSession.session.sessionId
        }

        // Allow only one coroutine at a time per sessionId
        return sessionMutex(sessionId).withLock {
            when (deviceVerificationListener.onDeviceVerification(sessionId, details)) {
                is DeviceVerificationResult.Success -> backend(call)
                is DeviceVerificationResult.Failure -> error
            }
        }
    }

    companion object {
        private val staticMutex: Mutex = Mutex()
        private val sessionMutexMap: MutableMap<SessionId, Mutex> = HashMap()

        suspend fun sessionMutex(sessionId: SessionId) =
            staticMutex.withLock { sessionMutexMap.getOrPut(sessionId) { Mutex() } }
    }
}

