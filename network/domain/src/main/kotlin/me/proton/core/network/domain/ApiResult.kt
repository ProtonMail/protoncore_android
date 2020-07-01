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
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Result of the safe API call.
 *
 * @param T value type of the successful call result.
 */
sealed class ApiResult<out T> {

    /**
     * Successful call result.
     *
     * @param T Value type.
     * @property value Value.
     */
    class Success<T>(val value: T) : ApiResult<T>() {
        override val valueOrNull get() = value
    }

    /**
     * Base class for error result.
     */
    sealed class Error : ApiResult<Nothing>() {

        /**
         * HTTP error.
         *
         * @property httpCode HTTP code.
         * @property message HTTP message.
         */
        open class Http(val httpCode: Int, val message: String) : Error()

        /**
         * Response with Proton error.
         *
         * @property httpCode Response HTTP code.
         * @property protonCode Response Proton code.
         * @property error Error string.
         */
        class Proton(httpCode: Int, val protonCode: Int, val error: String) : Http(httpCode, error)

        /**
         * Parsing error. Should not normally happen.
         */
        object Parse : Error()

        /**
         * Base class for connection errors (no response available)
         *
         * @property potentialBlock [true] if our API might have been blocked.
         */
        open class Connection(val potentialBlock: Boolean) : Error() {
            override val isPotentialBlocking get() = potentialBlock
        }

        /**
         * Connection timed out.
         *
         * @param potentialBlock [true] if our API might have been blocked.
         */
        class Timeout(potentialBlock: Boolean) : Connection(potentialBlock)

        /**
         * Certificate verification failed.
         */
        object Certificate : Connection(true)

        /**
         * No connectivity.
         */
        object NoInternet : Connection(false)

        /**
         * 429 "Too Many Requests"
         *
         * @property retryAfterSeconds Number of seconds to hold all requests (network layer will
         *  automatically fail requests that don't comply)
         */
        class TooManyRequest(val retryAfterSeconds: Int) : Http(HTTP_TOO_MANY_REQUESTS, "Too Many Requests")
    }

    /**
     * Value for successful calls or [null].
     */
    open val valueOrNull: T? get() = null

    /**
     * [true] for failed calls potentially caused by blocking.
     */
    open val isPotentialBlocking: Boolean get() = false

    companion object {

        /**
         * Introduce timeout for given block returning [ApiResult].
         *
         * @param T Value type for successful call.
         * @param timeoutMs Timeout in milliseconds.
         * @param block potentially long-running lambda producing [ApiResult].
         * @return block [ApiResult] or [ApiResult.Error.Timeout] on timeout.
         */
        suspend fun <T> withTimeout(timeoutMs: Long, block: suspend CoroutineScope.() -> ApiResult<T>) =
            withTimeoutOrNull(timeoutMs, block) ?: Error.Timeout(true)

        const val HTTP_TOO_MANY_REQUESTS = 429
    }
}
