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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.network.domain.ApiBackend
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.LoggerLogTag

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
    private val dohAlternativesListener: DohAlternativesListener? = null,
    private val createAltBackend: (baseUrl: String) -> ApiBackend<Api>,
) : ApiErrorHandler<Api> {

    suspend fun getActiveAltBackend(): ApiBackend<Api>? =
        // If some other call is currently looking for a proxy just wait for it.
        staticMutex.withLock {
            activeAltBackend
        }

    // Active proxy backend or null if we should use our primary backend.
    private var activeAltBackend: ApiBackend<Api>? = null
        get() {
            // If alt backend is outdated reset it so that primary backend is attempted.
            if (wallClockMs() - prefs.lastPrimaryApiFail >= apiClient.proxyValidityPeriodMs) {
                activeAltBackend = null
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
                staticMutex.withLock {
                    val altBackend = activeAltBackend
                    when {
                        !apiClient.shouldUseDoh -> error
                        altBackend != null -> {
                            CoreLogger.log(LOG_TAG, "Alt backend already established")
                            altBackend.invoke(call)
                        }
                        shouldUseDoh() -> {
                            val altResult = callWithAlternatives(call)
                            if (altResult == null || altResult.isPotentialBlocking)
                                dohAlternativesListener?.onProxiesFailed()
                            altResult ?: error
                        }
                        else -> error
                    }
                }
            }
        }
    }

    private suspend fun shouldUseDoh(): Boolean {
        return coroutineScope {
            // Ping primary backend (to make sure failure wasn't a random network error rather than
            // an actual block) parallel with refreshing proxy list
            val isPotentiallyBlockedAsync = async {
                primaryBackend.isPotentiallyBlocked()
            }
            val dohRefresh = async {
                dohProvider.refreshAlternatives()
            }
            // If ping on primary api succeeded don't fallback to proxy
            val isPotentiallyBlocked = isPotentiallyBlockedAsync.await()
            if (isPotentiallyBlocked) {
                dohRefresh.await()
                true
            } else {
                dohRefresh.cancel()
                activeAltBackend = null
                false
            }
        }
    }

    private suspend fun <T> callWithAlternatives(
        call: ApiManager.Call<Api, T>
    ): ApiResult<T>? {
        val alternatives = prefs.alternativeBaseUrls?.shuffled()
        val alternativesStart = monoClockMs()
        alternatives?.forEach { baseUrl ->
            // Abort alt. routing if condition changed in the meantime or deadline is reached
            if (!apiClient.shouldUseDoh || monoClockMs() - alternativesStart > apiClient.alternativesTotalTimeout) {
                return null
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

    suspend fun onBackendBlocked(failedBackend: ApiBackend<Api>) {
        if (failedBackend == primaryBackend)
            prefs.lastPrimaryApiFail = wallClockMs()
        else staticMutex.withLock {
            if (failedBackend == activeAltBackend) {
                CoreLogger.log(LOG_TAG, "Invalidating alt backend after failure")
                activeAltBackend = null
            }
        }
    }

    companion object {
        private val LOG_TAG = LoggerLogTag("core.network.api.doh")
        private val staticMutex: Mutex = Mutex()
    }
}
