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

package me.proton.core.presentation.viewmodel

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach

sealed class ViewModelResult<out T> {
    object None : ViewModelResult<Nothing>()
    data class Success<T>(val value: T) : ViewModelResult<T>()
    data class Error(val throwable: Throwable?) : ViewModelResult<Nothing>()
}

fun <T> ViewModelResult<T>.onSuccess(action: (T) -> Unit): ViewModelResult<T> {
    if (this is ViewModelResult.Success) {
        action(value)
    }
    return this
}

fun <T> ViewModelResult<T>.onError(action: (Throwable?) -> Unit): ViewModelResult<T> {
    if (this is ViewModelResult.Error) {
        action(throwable)
    }
    return this
}

fun <T> StateFlow<ViewModelResult<T>>.onSuccess(action: (T) -> Unit): StateFlow<ViewModelResult<T>> {
    onEach { it.onSuccess { value -> action(value) } }
    return this
}

fun <T> StateFlow<ViewModelResult<T>>.onError(action: (Throwable?) -> Unit): StateFlow<ViewModelResult<T>> {
    onEach { it.onError { error -> action(error) } }
    return this
}
