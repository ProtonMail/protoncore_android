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
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.session.SessionId

/**
 * Handles the Human Verification Code Invalid 12087 error response code.
 */
class HumanVerificationInvalidHandler<Api>(
    private val sessionId: SessionId?,
    private val clientIdProvider: ClientIdProvider,
    private val humanVerificationListener: HumanVerificationListener
) : ApiErrorHandler<Api> {

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        // Do we have a clientId ?
        val clientId = clientIdProvider.getClientId(sessionId) ?: return error

        // Invalid verification code ?
        if (error is ApiResult.Error.Http && error.proton?.code == ERROR_CODE_HUMAN_VERIFICATION_INVALID_CODE) {
            humanVerificationListener.onHumanVerificationInvalid(clientId)
            // Directly retry (could raise 9001, and then be handled by HumanVerificationNeededHandler).
            return backend(call)
        }
        return error
    }

    companion object {
        const val ERROR_CODE_HUMAN_VERIFICATION_INVALID_CODE = 12087
    }
}
