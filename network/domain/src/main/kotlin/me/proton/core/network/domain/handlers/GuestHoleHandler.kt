/*
 * Copyright (c) 2021 Proton Technologies AG
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
import me.proton.core.network.domain.guesthole.DefaultGuestHoleFallbackListener
import me.proton.core.network.domain.guesthole.GuestHoleFallbackListener

class GuestHoleHandler<Api>(
    private val guestHoleFallbackListener: GuestHoleFallbackListener = DefaultGuestHoleFallbackListener()
) : ApiErrorHandler<Api> {
    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        if (!error.isPotentialBlocking) return error
        // it should suspend and ask the client to establish guest hole
        if (error !is ApiResult.Error.Connection) return error
        val result =
            globalMutex.withLock {
                // Don't attempt to fallback if we did recently.
                guestHoleFallbackListener.fallbackCall(error.path, error.query) { backend(call) }
            }
        return result ?: error
    }

    companion object {
        private val globalMutex: Mutex = Mutex()
    }
}
