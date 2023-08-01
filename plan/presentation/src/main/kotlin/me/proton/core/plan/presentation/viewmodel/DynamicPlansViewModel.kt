/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.usecase.GetDynamicPlans
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class DynamicPlansViewModel @Inject constructor(
    private val getDynamicPlans: GetDynamicPlans
) : ProtonViewModel() {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class PlansLoaded(val plans: List<DynamicPlan>) : State()
        data class Error(val throwable: Throwable) : State()
    }


    private val _state: MutableStateFlow<State> = MutableStateFlow(State.Idle)
    val state = _state.asStateFlow()

    /**
     * Starts loading available plans for the given [userId].
     */
    fun loadPlans(userId: UserId?) = flow {
        emit(State.Loading)

        val plans = getDynamicPlans(userId)
        emit(State.PlansLoaded(plans))
    }.catch { error ->
        _state.tryEmit(State.Error(error))
    }.onEach {
        _state.tryEmit(it)
    }.launchIn(viewModelScope)
}
