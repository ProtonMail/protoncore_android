/*
 * Copyright (c) 2025 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.util.kotlin.catchAll

abstract class BaseViewModel<Action : Any, State : Any>(
    initialAction: Action,
    initialState: State
) : ViewModel() {
    private val mutableAction = MutableStateFlow(initialAction)

    val state: StateFlow<State> = mutableAction.flatMapLatest { action ->
        onAction(action).catchAll(logTag = javaClass.simpleName) { onError(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), initialState)

    protected abstract fun onAction(action: Action): Flow<State>
    protected abstract suspend fun FlowCollector<State>.onError(throwable: Throwable)

    fun perform(action: Action) = viewModelScope.launch {
        mutableAction.emit(action)
    }
}
