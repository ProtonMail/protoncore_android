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
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider

/**
 * Handles the 9001 error response code and passes the details that come together with it to the
 * client to process it further.
 */
class HumanVerificationHandler<Api>(
    private val sessionId: SessionId?,
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener,
    networkMainScope: CoroutineScope
) : OneOffJobHandler<HumanVerificationDetails, Boolean>(networkMainScope),
    ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> =
        if (error is ApiResult.Error.Http && error.proton?.code == ERROR_CODE_HUMAN_VERIFICATION) {
            // here we always expect the human verification details, this is why it is safe to use !!
            // since the API guarantees there will be details ALWAYS with the 9001 code
            val isHumanVerified = startOneOffJob(error.proton.humanVerification!!) {
                val session = sessionId?.let { sessionProvider.getSession(sessionId) } ?: return@startOneOffJob false
                // Suspending call to let the client handle HumanVerification needed.
                when (sessionListener.onHumanVerificationNeeded(session, it)) {
                    SessionListener.HumanVerificationResult.Failure -> false
                    SessionListener.HumanVerificationResult.Success -> true
                }
            }
            if (isHumanVerified)
                backend(call) // retry the same call that returned the error
            else
                error
        } else {
            error
        }

    companion object {
        const val ERROR_CODE_HUMAN_VERIFICATION = 9001
    }
}
