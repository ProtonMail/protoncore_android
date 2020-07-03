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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

/**
 * Gets the list of alternative baseUrls for Proton API.
 */
interface DohService {
    suspend fun getAlternativeBaseUrls(primaryBaseUrl: String): List<String>?
}

/**
 * Refreshes alternative urls for [baseUrl] using given list of DoH services ([dohServices]). Makes
 * sure that only one refresh operation takes place at one time for given baseUrl. Single instance
 * should exist per baseUrl.
 */
class DohProvider(
    private val baseUrl: String,
    private val apiClient: ApiClient,
    private val dohServices: List<DohService>,
    private val networkMainScope: CoroutineScope,
    private val prefs: NetworkPrefs,
    private val monoClockMs: () -> Long
) {
    private var ongoingRefresh: Deferred<Unit>? = null
    private var lastRefresh = Long.MIN_VALUE

    suspend fun refreshAlternatives() = withContext(networkMainScope.coroutineContext) {
        if (monoClockMs() >= lastRefresh + MIN_REFRESH_INTERVAL_MS) {
            ongoingRefresh = ongoingRefresh ?: async(start = CoroutineStart.LAZY) {
                for (service in dohServices) {
                    val success = withTimeoutOrNull(apiClient.dohServiceTimeoutMs) {
                        val result = service.getAlternativeBaseUrls(baseUrl)
                        if (result != null)
                            prefs.alternativeBaseUrls = result
                        result != null
                    }
                    if (success == true)
                        break
                }
                lastRefresh = monoClockMs()
                ongoingRefresh = null
            }
            ongoingRefresh!!.join()
        }
    }

    companion object {
        val MIN_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10)
    }
}
