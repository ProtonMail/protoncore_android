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

import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import kotlin.math.pow
import kotlin.random.Random

/**
 * Implementation of [ApiManager].
 *
 * @param Api API interface
 * @property client [ApiClient] for client-library integration.
 * @property primaryBackend [ApiBackend] for regular API calls.
 * @property dohApiHandler [DohApiHandler] instance to handle DoH logic for API calls.
 * @property networkManager [NetworkManager] for connectivity checks.
 * @property errorHandlers list of [ApiErrorHandler] for call error recovery.
 * @property monoClockMs Monotonic clock with millisecond resolution.
 */
class ApiManagerImpl<Api>(
    private val client: ApiClient,
    private val primaryBackend: ApiBackend<Api>,
    private val dohApiHandler: DohApiHandler<Api>,
    private val networkManager: NetworkManager,
    private val errorHandlers: List<ApiErrorHandler<Api>>,
    private val monoClockMs: () -> Long
) : ApiManager<Api> {

    override suspend operator fun <T> invoke(
        forceNoRetryOnConnectionErrors: Boolean,
        block: suspend (Api) -> T
    ): ApiResult<T> {
        val call = ApiManager.Call(monoClockMs(), block)
        return when {
            !networkManager.isConnectedToNetwork() ->
                ApiResult.Error.NoInternet
            call.timestampMs < failRequestBeforeTimeMs -> {
                ApiResult.Error.TooManyRequest(
                    TimeUnit.MILLISECONDS.toSeconds(
                        failRequestBeforeTimeMs - call.timestampMs
                    ).toInt() + 1
                )
            }
            forceNoRetryOnConnectionErrors ->
                handledCall(primaryBackend, call)
            client.shouldUseDoh ->
                dohApiHandler(::handledCall, call)
            else ->
                callWithBackoff(call)
        }
    }

    private suspend fun <T> callWithBackoff(call: ApiManager.Call<Api, T>): ApiResult<T> {
        var retryCount = 0
        while (true) {
            val result = handledCall(primaryBackend, call)
            if (retryCount < client.backoffRetryCount && shouldRetry(result)) {
                val delayCoefficient = sample(
                    2.0.pow(retryCount.toDouble()),
                    2.0.pow(retryCount + 1.0)
                )
                delay(client.backoffBaseDelayMs * delayCoefficient.toLong())
                retryCount++
            } else {
                return result
            }
        }
    }

    private fun sample(min: Double, max: Double) =
        min + Random.nextDouble() * (max - min)

    private fun <T> shouldRetry(result: ApiResult<T>) =
        result is ApiResult.Error.Connection // TODO: improve condition

    private suspend fun <T> handledCall(
        backend: ApiBackend<Api>,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        val firstResult = backend(call)
        return if (firstResult is ApiResult.Error.TooManyRequest) {
            failRequestBeforeTimeMs =
                monoClockMs() + TimeUnit.SECONDS.toMillis(firstResult.retryAfterSeconds.toLong())
            firstResult
        } else {
            errorHandlers.fold(firstResult) { result, handler ->
                if (result is ApiResult.Error)
                    handler.invoke(backend, result, call)
                else
                    result
            }
        }
    }

    companion object {
        // All Request before this time fail automatically to prevent DDoS-ing API.
        // Global so that "too many requests" in one instance will affect all.
        var failRequestBeforeTimeMs = Long.MIN_VALUE
    }
}
