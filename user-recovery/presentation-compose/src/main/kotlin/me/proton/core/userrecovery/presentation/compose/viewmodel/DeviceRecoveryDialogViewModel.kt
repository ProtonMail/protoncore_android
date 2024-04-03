/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.presentation.compose.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.user.domain.extension.getDisplayName
import me.proton.core.user.domain.extension.getEmail
import me.proton.core.user.domain.usecase.ObserveUser
import me.proton.core.userrecovery.domain.LogTag
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryDeeplink.Arg
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class DeviceRecoveryDialogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeUser: ObserveUser,
    override val observabilityManager: ObservabilityManager
) : ViewModel(), ObservabilityContext {

    private val userId = UserId(requireNotNull(savedStateHandle.get<String>(Arg.UserId)))

    private val currentAction = MutableStateFlow<Action>(Action.ObserveState)

    val state: StateFlow<State> = currentAction.flatMapLatest {
        when (it) {
            is Action.ObserveState -> observeState()
        }
    }.catch {
        CoreLogger.e(LogTag.ERROR_OBSERVING_STATE, it)
        emit(State.Error(it))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = State.Loading
    )

    private fun observeState(): Flow<State> = observeUser(userId).filterNotNull().mapLatest {
        State.Idle(it.getEmail() ?: it.getDisplayName() ?: "")
    }

    sealed class Action {
        data object ObserveState : Action()
    }

    sealed class State {
        data object Loading : State()
        data class Idle(val email: String) : State()
        data class Error(val throwable: Throwable?) : State()
    }
}
