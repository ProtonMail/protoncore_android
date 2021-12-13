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

package me.proton.core.auth.presentation.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.proton.core.auth.presentation.ConfirmPasswordOrchestrator
import me.proton.core.auth.presentation.onConfirmPasswordResult
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class ConfirmPasswordViewModel @Inject constructor(
    private val confirmPasswordOrchestrator: ConfirmPasswordOrchestrator
) : ViewModel() {

    private val _confirmPassword = MutableStateFlow<State>(State.Idle)

    sealed class State {
        object Idle : State()
        object Success : State()
        object Error : State()
    }

    fun register(context: ComponentActivity) {
        confirmPasswordOrchestrator.register(context)
    }

    /**
     * Public interface that should trigger the confirm password workflow.
     */
    suspend fun confirmPassword(missingScope: Scope): MissingScopeResult {
        with(confirmPasswordOrchestrator) {
            onConfirmPasswordResult {
                viewModelScope.launch {
                    if (it?.confirmed == null || !it.confirmed) {
                        _confirmPassword.tryEmit(State.Error)
                    } else {
                        _confirmPassword.tryEmit(State.Success)
                    }
                }
            }
            startConfirmPasswordWorkflow(missingScope)
        }

        val state = _confirmPassword.filter {
            it in listOf(State.Success, State.Error)
        }.map {
            when (it) {
                is State.Error -> MissingScopeResult.Failure
                is State.Success -> MissingScopeResult.Success
                else -> MissingScopeResult.Failure
            }.exhaustive
        }.first()
        return state
    }
}
