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
sealed class DataResult<out T> {

    abstract val source: ResponseSource

    data class Success<T>(
        val value: T,
        override val source: ResponseSource
    ) : DataResult<T>()

    sealed class Error<T> : DataResult<T>() {

        abstract val message: String?
        abstract val code: Int // the error code, for any potential business logic handling, default is 0

        data class Message<T>(
            override val message: String?,
            override val source: ResponseSource,
            override val code: Int = 0,
            val validation: Boolean = false // if it is validation error, default false
        ) : Error<T>()
    }
}

/**
 * Performs the given [action] if this instance represents an [DataResult.Error].
 * Returns the original `DataResult` unchanged.
 */
inline fun <T> DataResult<T>.onFailure(action: (message: String?, code: Int) -> Unit): DataResult<T> {
    if (this is DataResult.Error) action(message, code)
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
