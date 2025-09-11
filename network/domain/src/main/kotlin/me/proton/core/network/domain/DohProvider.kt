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

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.network.domain.session.SessionId
import java.util.concurrent.TimeUnit

/**
 * Gets the list of alternative baseUrls for Proton API.
 */
fun interface DohService {
    suspend fun getAlternativeBaseUrls(sessionId: SessionId?, primaryBaseUrl: String): List<String>?
}

/**
 * Refreshes alternative urls for [baseUrl] using given list of DoH services ([primaryDohServices] + randomly selected
 * services from [secondaryDohServicesUrls]). Makes sure that only one refresh operation takes place at one time for
 * given baseUrl. Single instance should exist per baseUrl.
 */
class DohProvider(
    private val baseUrl: String,
    private val apiClient: ApiClient,
    private val primaryDohServices: List<DohService>,
    private val secondaryDohServicesUrls: List<String>,
    private val createSecondaryDohService: (String) -> DohService,
    private val protonDohService: DohService,
    private val networkMainScope: CoroutineScope,
    private val prefs: NetworkPrefs,
    private val monoClockMs: () -> Long,
    private val sessionId: SessionId?,
    private val dohAlternativesListener: DohAlternativesListener?
) {

    suspend fun refreshAlternatives() = withContext(networkMainScope.coroutineContext) {
        if (monoClockMs() >= lastAlternativesRefresh + MIN_REFRESH_INTERVAL_MS) {
            val allServicesFailed = tryDohServices()
            lastAlternativesRefresh = monoClockMs()

            if (allServicesFailed && dohAlternativesListener != null) {
                dohAlternativesListener.onAlternativesUnblock {
                    tryProtonDohService()
                }
            }
        }
    }

    private suspend fun tryProtonDohService(): Boolean {
        val success = withTimeoutOrNull(apiClient.dohServiceTimeoutMs) {
            val result = protonDohService.getAlternativeBaseUrls(sessionId, baseUrl)
            if (result != null)
                prefs.alternativeBaseUrls = result
            result != null
        }
        return success ?: false
    }

    private suspend fun tryDohServices(): Boolean = coroutineScope {
        // Select 2 random secondary services, but include successful one if available.
        val secondaryServiceUrls =
            prefs.successfulSecondaryDohServiceUrl?.let {
                listOfNotNull(it, secondaryDohServicesUrls.randomOrNull())
            } ?: secondaryDohServicesUrls.asSequence().shuffled().take(2).toList()
        val secondaryDohServices = secondaryServiceUrls.map { createSecondaryDohService(it) }
        val dohServices = primaryDohServices + secondaryDohServices

        val successfulServices = mutableListOf<Pair<DohService, List<String>>>()
        val jobs = mutableListOf<Job>()
        dohServices.mapTo(jobs) { service ->
            launch {
                val isSecondaryService = service in secondaryDohServices
                val result = withTimeoutOrNull(apiClient.dohServiceTimeoutMs) {
                    service.getAlternativeBaseUrls(sessionId, baseUrl)
                }
                if (result != null) {
                    successfulServices += service to result
                    if (!isSecondaryService) {
                        jobs.forEach { it.cancel() }
                    }
                } else if (isSecondaryService) {
                    val index = secondaryDohServices.indexOf(service)
                    if (index != -1 && secondaryServiceUrls[index] == prefs.successfulSecondaryDohServiceUrl) {
                        prefs.successfulSecondaryDohServiceUrl = null
                    }
                }
            }
        }
        jobs.joinAll()

        val firstSuccessfulPrimaryService = successfulServices.firstOrNull { (service, _) -> service in primaryDohServices }
        val firstSuccessfulSecondaryService = successfulServices.firstOrNull { (service, _) -> service in secondaryDohServices }
        if (firstSuccessfulPrimaryService != null) {
            prefs.alternativeBaseUrls = firstSuccessfulPrimaryService.second
        } else if (firstSuccessfulSecondaryService != null) {
            prefs.alternativeBaseUrls = firstSuccessfulSecondaryService.second
        }
        if (firstSuccessfulSecondaryService != null) {
            prefs.successfulSecondaryDohServiceUrl =
                secondaryServiceUrls[secondaryDohServices.indexOf(firstSuccessfulSecondaryService.first)]
        }

        val allFailed = successfulServices.isEmpty()
        allFailed
    }

    companion object {
        val MIN_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10)

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        @Volatile var lastAlternativesRefresh = Long.MIN_VALUE
    }
}
