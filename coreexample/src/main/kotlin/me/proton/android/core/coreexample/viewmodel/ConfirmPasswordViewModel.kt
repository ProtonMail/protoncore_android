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

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.proton.core.auth.presentation.ConfirmPasswordOrchestrator
import me.proton.core.auth.presentation.onConfirmPasswordResult
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
class ConfirmPasswordViewModel @Inject constructor(
    private val confirmPasswordOrchestrator: ConfirmPasswordOrchestrator
) : ViewModel() {

    private val _confirmPassword = MutableSharedFlow<State>(replay = 1, extraBufferCapacity = 3)

    sealed class State {
        object Success : State()
        object Error : State()
    }


    fun register(context: FragmentActivity) {
        confirmPasswordOrchestrator.register(context)
    }

    /**
     * Public interface that should trigger the confirm password workflow.
     */
    suspend fun confirmPassword(showSecondFactorCode: Boolean): MissingScopeListener.MissingScopeResult {
        with(confirmPasswordOrchestrator) {
            onConfirmPasswordResult {
                viewModelScope.launch {
                    if (it == null) {
                        _confirmPassword.emit(State.Error)
                    } else {
                        _confirmPassword.emit(State.Success)
                    }
                }
            }
            startConfirmPasswordWorkflow(showSecondFactorCode)
        }

        val state = _confirmPassword
            .filter {
                it in listOf(
                    State.Success,
                    State.Error
                )
            }.map {
                when (it) {
                    is State.Error -> MissingScopeListener.MissingScopeResult.Failure
                    is State.Success -> MissingScopeListener.MissingScopeResult.Success
                }.exhaustive
            }.first()
        return state
    }
}
