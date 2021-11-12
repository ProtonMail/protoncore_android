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
import kotlinx.coroutines.flow.retryWhen

/**
 * Retries the collection of the flow, in case an exception occurred in upstream flow, and [predicate] returns `true`.
 * If the flow will be retried, [onRetryAction] will be called before, with a [Throwable] that caused the retry.
 * @see retryWhen
 */
fun <T> Flow<T>.retryOnceWhen(
    predicate: suspend (Throwable) -> Boolean,
    onRetryAction: suspend (cause: Throwable) -> Unit
): Flow<T> {
    return retryWhen { cause, attempt ->
        val willRetry = predicate(cause) && attempt < 1
        if (willRetry) onRetryAction.invoke(cause)
        willRetry
    }
}
