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
import kotlin.math.pow
import kotlin.random.Random

/**
 * Implementation of [ApiManager].
 *
 * @param Api API interface
 * @property client [ApiClient] for client-library integration.
 * @property primaryBackend [ApiBackend] for regular API calls.
 * @property dohApiHandler [DohApiHandler] instance to handle DoH logic for API calls.
 * @property errorHandlers list of [ApiErrorHandler] for call error recovery.
 * @property monoClockMs Monotonic clock with millisecond resolution.
 */
class ApiManagerImpl<Api>(
    private val client: ApiClient,
    private val primaryBackend: ApiBackend<Api>,
    private val dohApiHandler: DohApiHandler<Api>,
    private val errorHandlers: List<ApiErrorHandler<Api>>,
    private val monoClockMs: () -> Long
) : ApiManager<Api> {

    override suspend operator fun <T> invoke(
        forceNoRetryOnConnectionErrors: Boolean,
        block: suspend (Api) -> T
    ): ApiResult<T> {
        val call = ApiManager.Call(monoClockMs(), block)
        return when {
            forceNoRetryOnConnectionErrors -> handledCall(primaryBackend, call)
            client.shouldUseDoh -> dohApiHandler(::handledCall, call)
            else -> callWithBackoff(call)
        }
    }

    private suspend fun <T> callWithBackoff(call: ApiManager.Call<Api, T>): ApiResult<T> {
        fun sample(min: Double, max: Double) = min + Random.nextDouble() * (max - min)
        var retryCount = 0
        while (true) {
            val result = handledCall(primaryBackend, call)
            if (retryCount < client.backoffRetryCount && result.isRetryable()) {
                val delayCoefficient = sample(
                    min = 2.0.pow(retryCount),
                    max = 2.0.pow(retryCount + 1)
                )
                delay(client.backoffBaseDelayMs * delayCoefficient.toLong())
                retryCount++
            } else {
                return result
            }
        }
    }

    private suspend fun <T> handledCall(
        backend: ApiBackend<Api>,
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        val backendResult = backend(call)
        val handlersToRetry = mutableListOf<ApiErrorHandler<Api>>()
        var currentResult = backendResult

        // all of the handlers should be asked to handle any possible error of the original api call
        // but because some of the handlers will do an api call retry, some of the handlers should be applied to the
        // result of the retried api call
        for (handler in errorHandlers) {
            if (currentResult is ApiResult.Error) {
                val result = handler.invoke(backend, currentResult, call)
                // add the handler in the retry list only if it produced a different error than the initial error it
                // has been dealing with
                if (result is ApiResult.Error && currentResult != result) {
                    handlersToRetry.add(handler)
                }
                currentResult = result
            }
        }
        for (handler in handlersToRetry) {
            if (currentResult is ApiResult.Error) {
                currentResult = handler.invoke(backend, currentResult, call)
            }
        }
        return currentResult
    }
}
