/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.util.kotlin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen

/**
 * Catches an error that matches a given [predicate], and performs an [action] if such an error is caught.
 * If an error from upstream doesn't match the [predicate], it's passed downstream.
 */
inline fun <T> Flow<T>.catchWhen(
    crossinline predicate: suspend (Throwable) -> Boolean,
    crossinline action: suspend FlowCollector<T>.() -> Unit
): Flow<T> {
    return catch { error ->
        if (predicate(error)) {
            action()
        } else {
            throw error
        }
    }
}

/**
 * Retries the collection of the flow, in case an exception occurred in upstream flow, and [predicate] returns `true`.
 * If the flow will be retried, [onBeforeRetryAction] will be called before, with a [Throwable] that caused the retry.
 * @see retryWhen
 */
inline fun <T> Flow<T>.retryOnceWhen(
    crossinline predicate: suspend (Throwable) -> Boolean,
    crossinline onBeforeRetryAction: suspend (cause: Throwable) -> Unit
): Flow<T> {
    return retryWhen { cause, attempt ->
        val willRetry = predicate(cause) && attempt < 1
        if (willRetry) onBeforeRetryAction.invoke(cause)
        willRetry
    }
}
