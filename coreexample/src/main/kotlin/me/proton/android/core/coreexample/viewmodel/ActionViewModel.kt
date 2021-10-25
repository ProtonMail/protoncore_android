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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class ActionViewModel<Action, State> : ViewModel() {

    private val mutableState = MutableSharedFlow<State>(replay = 1, onBufferOverflow = BufferOverflow.SUSPEND)
    private val mutableAction = MutableSharedFlow<Action>(extraBufferCapacity = 1)

    protected abstract fun process(action: Action): Flow<State>

    fun dispatch(action: Action) = mutableAction.tryEmit(action)

    val state: Flow<State> = mutableState.asSharedFlow()

    init {
        mutableAction.flatMapLatest { action ->
            process(action)
        }.onEach {
            mutableState.emit(it)
        }.launchIn(viewModelScope)
    }
}
