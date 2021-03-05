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
import me.proton.core.network.domain.humanverification.HumanVerificationDetails

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
     * @param cause [Exception] exception that caused this error for debugging purposes.
     */
    sealed class Error(val cause: Throwable?) : ApiResult<Nothing>() {

        /**
         * HTTP error.
         *
         * @property httpCode HTTP code.
         * @property message HTTP message.
         * @property proton Proton-specific HTTP error data.
         */
        open class Http(
            val httpCode: Int,
            val message: String,
            val proton: ProtonData? = null,
            cause: Throwable? = null
        ) : Error(cause) {

            override fun toString() =
                "${this::class.simpleName}: httpCode=$httpCode message=$message, proton=$proton cause=$cause"
        }

        // detekt warning here is fine, the human verification details is optional, and if present in the response it is
        // set later, thus var.
        data class ProtonData(val code: Int, val error: String, var humanVerification: HumanVerificationDetails? = null)

        /**
         * Parsing error. Should not normally happen.
         */
        class Parse(cause: Throwable?) : Error(cause)

        /**
         * Base class for connection errors (no response available)
         *
         * @property potentialBlock [true] if our API might have been blocked.
         */
        open class Connection(val potentialBlock: Boolean, cause: Throwable? = null) : Error(cause) {
            override val isPotentialBlocking get() = potentialBlock
        }

        /**
         * Connection timed out.
         *
         * @param potentialBlock [true] if our API might have been blocked.
         */
        class Timeout(potentialBlock: Boolean, cause: Throwable? = null) : Connection(potentialBlock, cause)

        /**
         * Certificate verification failed.
         */
        class Certificate(cause: Throwable) : Connection(true, cause)

        /**
         * No connectivity.
         */
        object NoInternet : Connection(false, null)

        /**
         * 429 "Too Many Requests"
         *
         * @property retryAfterSeconds Number of seconds to hold all requests (network layer will
         *  automatically fail requests that don't comply)
         */
        class TooManyRequest(val retryAfterSeconds: Int, proton: ProtonData? = null) :
            Http(HTTP_TOO_MANY_REQUESTS, "Too Many Requests", proton)
    }

    /**
     * Value for successful calls or `null`.
     */
    open val valueOrNull: T? get() = null

    /**
     * Value for successful calls or throw wrapped error if exist.
     */
    val valueOrThrow: T
        get() {
            throwIfError()
            return checkNotNull(valueOrNull)
        }

    /**
     * Returns the encapsulated [Throwable] exception if this instance is [Error] or `null` otherwise.
     */
    val exceptionOrNull: Throwable? get() = if (this is Error) cause else null

    /**
     * [true] for failed calls potentially caused by blocking.
     */
    open val isPotentialBlocking: Boolean get() = false

    /**
     * Throws exception if this instance is [Error].
     */
    fun throwIfError() {
        if (this is Error) doThrow()
    }

    companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429

        /**
         * Introduce timeout for given block returning [ApiResult].
         *
         * @param T Value type for successful call.
         * @param timeoutMs Timeout in milliseconds.
         * @param block potentially long-running lambda producing [ApiResult].
         * @return block [ApiResult] or [ApiResult.Error.Timeout] on timeout.
         */
        suspend fun <T> withTimeout(timeoutMs: Long, block: suspend CoroutineScope.() -> ApiResult<T>) =
            withTimeoutOrNull(timeoutMs, block) ?: Error.Timeout(true, null)
    }
}

fun ApiResult.Error.doThrow() {
    throw ApiException(this)
}

open class ApiException(val error: ApiResult.Error) : Exception(
    if (error is ApiResult.Error.Http && error.proton?.error != null) error.proton.error
    else error.cause?.message,
    error.cause
)

/**
 * Performs the given [action] if this instance represents an [ApiResult.Error].
 * Returns the original `Result` unchanged.
 */
inline fun <T> ApiResult<T>.onError(
    action: (value: ApiResult.Error) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Error) action(this)
    return this
}

/**
 * Performs the given [action] if this instance represents an [ApiResult.Success].
 * Returns the original `Result` unchanged.
 */
inline fun <T> ApiResult<T>.onSuccess(
    action: (value: T) -> Unit
): ApiResult<T> {
    if (this is ApiResult.Success) action(value)
    return this
}
