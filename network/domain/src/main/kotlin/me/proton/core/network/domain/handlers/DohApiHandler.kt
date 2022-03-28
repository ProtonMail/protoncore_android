/*
 * Copyright (c) 2022 Proton Technologies AG
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

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.NetworkPrefs

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
    private val monoClockMs: () -> Long,
    private val createAltBackend: (baseUrl: String) -> ApiBackend<Api>,
) : ApiErrorHandler<Api> {

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

    override suspend fun <T> invoke(
        backend: ApiBackend<Api>,
        error: ApiResult.Error,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        return when {
            !apiClient.shouldUseDoh -> error
            !error.isPotentialBlocking -> error
            error !is ApiResult.Error.Connection -> error
            else -> {
                coroutineScope {
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

                        if (activeAltBackend == null)
                            prefs.lastPrimaryApiFail = wallClockMs()
                        else
                            activeAltBackend = null

                        callWithAlternatives(call) ?: error
                    } else {
                        dohRefresh.cancel()
                        activeAltBackend = null
                        error
                    }
                }
            }
        }
    }

    private suspend fun <T> callWithAlternatives(
        call: ApiManager.Call<Api, T>
    ): ApiResult<T>? {
        val alternatives = prefs.alternativeBaseUrls?.shuffled()
        alternatives?.forEach { baseUrl ->
            if (monoClockMs() - call.timestampMs > apiClient.dohTimeoutMs) {
                return ApiResult.Error.Timeout(true, null)
            }
            val backend = createAltBackend(baseUrl)
            val result = backend.invoke(call)
            if (!result.isPotentialBlocking) {
                activeAltBackend = backend
                return result
            }
        }
        return null
    }
}
