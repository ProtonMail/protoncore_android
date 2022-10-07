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
import me.proton.core.network.domain.HttpResponseCodes.HTTP_REQUEST_TIMEOUT
import me.proton.core.network.domain.HttpResponseCodes.HTTP_SERVICE_UNAVAILABLE
import me.proton.core.network.domain.HttpResponseCodes.HTTP_TOO_MANY_REQUESTS
import me.proton.core.network.domain.handlers.DohApiHandler
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    private val errorHandlers: List<ApiErrorHandler<Api>>,
    private val monoClockMs: () -> Long
) : ApiManager<Api> {

    private val dohApiHandler = errorHandlers.firstNotNullOfOrNull { it as? DohApiHandler<Api> }

    private suspend fun activeBackend() =
        if (client.shouldUseDoh)
            dohApiHandler?.getActiveAltBackend() ?: primaryBackend
        else
            primaryBackend

    override suspend operator fun <T> invoke(
        forceNoRetryOnConnectionErrors: Boolean,
        block: suspend (Api) -> T
    ): ApiResult<T> {
        val call = ApiManager.Call(monoClockMs(), block)
        return if (forceNoRetryOnConnectionErrors) {
            handledCall(call)
        } else {
            callWithBackoff(call)
        }
    }

    private suspend fun <T> callWithBackoff(call: ApiManager.Call<Api, T>): ApiResult<T> {
        var retryCount = 0
        var result = handledCall(call)

        while (result.needsRetry(retryCount, maxRetryCount = client.backoffRetryCount)) {
            delay(result.getRetryDelay(retryCount))
            result = handledCall(call)
            retryCount++
        }
        return result
    }

    private fun <T> ApiResult<T>.getRetryDelay(retryCount: Int): Duration {
        return retryAfter()?.exponentialDelay(retryCount, base = 1.2)
            ?: client.backoffBaseDelayMs.milliseconds.exponentialDelay(retryCount)
    }

    private fun Duration.exponentialDelay(retryCount: Int, base: Double = 2.0): Duration {
        fun sample(min: Double, max: Double) = min + Random.nextDouble() * (max - min)
        val delayCoefficient = sample(
            min = base.pow(retryCount),
            max = base.pow(retryCount + 1)
        )
        return this * delayCoefficient
    }

    private suspend fun <T> handledCall(
        call: ApiManager.Call<Api, T>
    ): ApiResult<T> {
        val backendResult = activeBackend().let { backend ->
            backend.invoke(call).also { result ->
                if (result.isPotentialBlocking)
                    dohApiHandler?.onBackendBlocked(backend)
            }
        }
        val handlersToRetry = mutableListOf<ApiErrorHandler<Api>>()
        var currentResult = backendResult

        // all of the handlers should be asked to handle any possible error of the original api call
        // but because some of the handlers will do an api call retry, some of the handlers should be applied to the
        // result of the retried api call
        for (handler in errorHandlers) {
            if (currentResult is ApiResult.Error) {
                val result = handler.invoke(activeBackend(), currentResult, call)
                // add the handler in the retry list only if it produced a different error than the initial error it
                // has been dealing with
                if (result is ApiResult.Error && currentResult != result && handler != dohApiHandler) {
                    handlersToRetry.add(handler)
                }
                currentResult = result
            }
        }
        for (handler in handlersToRetry) {
            if (currentResult is ApiResult.Error) {
                currentResult = handler.invoke(activeBackend(), currentResult, call)
            }
        }
        return currentResult
    }
}

/**
 * Sometimes, a result is [isRetryable], but [ApiManagerImpl] may sometimes resign from retrying.
 * Visible for testing.
 * @param retryCount The retry counter (starting from 0, which means the request was executed once, but hasn't been retried yet).
 * @param maxRetryCount Maximum number of retries allowed.
 * @param maxRetryAfter Maximum duration for which we can retry.
 */
internal fun <T> ApiResult<T>.needsRetry(
    retryCount: Int,
    maxRetryCount: Int,
    maxRetryAfter: Duration = 10.seconds
): Boolean {
    if (retryCount >= maxRetryCount) return false
    if (!isRetryable()) return false

    val httpCode = (this as? ApiResult.Error.Http)?.httpCode

    if (httpCode == HTTP_REQUEST_TIMEOUT) return retryCount == 0

    return when (val retryAfter = retryAfter()) {
        null -> httpCode !in arrayOf(HTTP_TOO_MANY_REQUESTS, HTTP_SERVICE_UNAVAILABLE)
        else -> retryAfter <= maxRetryAfter
    }
}
