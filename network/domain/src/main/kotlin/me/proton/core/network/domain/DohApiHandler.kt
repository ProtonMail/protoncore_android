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
package me.proton.core.network.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Responsible for making an API call according to DoH feature logic: when our API seems blocked
 * will refresh the list of alternative proxies with DoH queries and try to repeat a call on those
 * proxies.
 */
class DohApiHandler<Api>(
    private val apiClient: ApiClient,
    private val primaryBackend: ApiBackend<Api>,
    private val dohProvider: DohProvider,
    private val prefs: NetworkPrefs,
    private val wallClockMs: () -> Long,
    private val createAltBackend: (baseUrl: String) -> ApiBackend<Api>
) {

    // Active proxy backend or null if we should use our primary backend.
    var activeAltBackend: ApiBackend<Api>? = null
        get() {
            // If alt backend is outdated reset it so that primary backend is attempted. 
            if (wallClockMs() - prefs.lastPrimaryApiFail >= apiClient.proxyValidityPeriodMs) {
                field = null
            } else if (field == null) {
                val baseUrl = prefs.activeAltBaseUrl
                if (baseUrl != null)
                    activeAltBackend = createAltBackend(baseUrl)
            }
            return field
        }
        set(value) {
            field = value
            prefs.activeAltBaseUrl = value?.baseUrl
        }

    /**
     * Makes an API [call] according to DoH feature logic.
     * @param callHandler Function that should be used to make a call with a reachable
     *   backend.
     */
    suspend operator fun <T> invoke(
        callHandler: suspend (ApiBackend<Api>, ApiManager.Call<Api, T>) -> ApiResult<T>,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        val activeBackend = activeAltBackend ?: primaryBackend
        val result = callHandler(activeBackend, call)
        return if (!result.isPotentialBlocking)
            result
        else coroutineScope {
            // Ping primary backend (to make sure failure wasn't a random network error rather than
            // an actual block) parallel with refreshing proxy list
            val isPotentiallyBlockedAsync = async {
                primaryBackend.isPotentiallyBlocked()
            }
            val dohRefresh = async {
                withTimeoutOrNull(apiClient.dohProxyRefreshTimeoutMs) {
                    dohProvider.refreshAlternatives()
                }
            }
            // If ping on primary api succeeded don't fallback to proxy
            val isPotentiallyBlocked = isPotentiallyBlockedAsync.await()
            if (isPotentiallyBlocked) {
                dohRefresh.await()

                if (activeBackend == primaryBackend)
                    prefs.lastPrimaryApiFail = wallClockMs()
                else
                    activeAltBackend = null

                callWithAlternatives(callHandler, call) ?: result
            } else {
                dohRefresh.cancel()
                activeAltBackend = null
                result
            }
        }
    }

    private suspend fun <T> callWithAlternatives(
        callHandler: suspend (ApiBackend<Api>, ApiManager.Call<Api, T>) -> ApiResult<T>,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T>? {
        val alternatives = prefs.alternativeBaseUrls?.shuffled()
        alternatives?.forEach { baseUrl ->
            val backend = createAltBackend(baseUrl)
            val result = callHandler(backend, call)
            if (!result.isPotentialBlocking) {
                activeAltBackend = backend
                return result
            }
        }
        return null
    }
}
