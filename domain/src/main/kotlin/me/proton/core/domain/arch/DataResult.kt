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

package me.proton.core.domain.arch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.util.kotlin.exhaustive

enum class ResponseSource {
    Local,
    Remote
}

/**
 * Sealed class for processing data result, whether from API as [ResponseSource.Remote] or locally from DB/preferences
 * [ResponseSource.Local].
 * It also classifies the data into [Success] or [Error] result for easier manipulation.
 * Lets the usecase (that use repository) to be aware that something is loading and from where.
 * Useful when client needs to support offline mode.
 */
sealed class DataResult<out T>(open val source: ResponseSource) {

    data class Processing(override val source: ResponseSource) : DataResult<Nothing>(source)

    data class Success<T>(override val source: ResponseSource, val value: T) : DataResult<T>(source)

    sealed class Error(
        override val source: ResponseSource,
        open val message: String?,
        open val cause: Throwable?
    ) : DataResult<Nothing>(source) {

        data class Local(
            override val message: String?,
            override val cause: Throwable?
        ) : Error(ResponseSource.Local, message, cause)

        data class Remote(
            override val message: String?,
            override val cause: Throwable?,
            val protonCode: Int = 0,
            val httpCode: Int = 0
        ) : Error(ResponseSource.Remote, message, cause)
    }
}

/**
 * Performs the given [action] if this instance represents an [DataResult.Error].
 * Returns the original `DataResult` unchanged.
 */
inline fun <T> DataResult<T>.onFailure(
    action: (message: String?, protonCode: Int?, httpCode: Int?) -> Unit
): DataResult<T> {
    if (this is DataResult.Error.Local)
        action(message, null, null)
    else if (this is DataResult.Error.Remote)
        action(message, protonCode, httpCode)
    return this
}

/**
 * Performs the given [action] if this instance represents a [DataResult.Success].
 * Returns the original `DataResult` unchanged.
 */
inline fun <T> DataResult<T>.onSuccess(action: (value: T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(value)
    return this
}

/**
 * Applies transform function to successful [DataResult] and it does not modify unsuccessful ones.
 */
suspend inline fun <T : Any, R : Any> DataResult<T>.mapSuccess(
    crossinline transform: suspend (value: DataResult.Success<T>) -> DataResult<R>
): DataResult<R> = when (this) {
    is DataResult.Processing -> this
    is DataResult.Error -> this
    is DataResult.Success -> transform(this)
}.exhaustive

/**
 * Returns a flow containing the result of applying transform function to each successful value while unsuccessful ones
 * are not transformed.
 */
inline fun <T : Any, R : Any> Flow<DataResult<T>>.mapSuccess(
    crossinline transform: suspend (value: DataResult.Success<T>) -> DataResult<R>
): Flow<DataResult<R>> = map { it.mapSuccess { value -> transform(value) } }

/**
 * Applies transform function to each successful value of the given flow and re-emits unsuccessful ones.
 */
@ExperimentalCoroutinesApi
inline fun <T : Any, R : Any> Flow<DataResult<T>>.transformSuccess(
    crossinline transform: suspend FlowCollector<DataResult<R>>.(value: DataResult.Success<T>) -> Unit
): Flow<DataResult<R>> = transformLatest {
    when (it) {
        is DataResult.Processing -> emit(it)
        is DataResult.Error -> emit(it)
        is DataResult.Success -> transform(it)
    }.exhaustive
}
