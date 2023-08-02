/*
 * Copyright (c) 2023 Proton Technologies AG
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.entity.DynamicSubscription
import me.proton.core.payment.domain.usecase.GetDynamicSubscription
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class DynamicSubscriptionViewModel @Inject constructor(
    override val manager: ObservabilityManager,
    private val accountManager: AccountManager,
    private val getDynamicSubscription: GetDynamicSubscription,
) : ProtonViewModel(), ObservabilityContext {

    sealed class State {
        object Loading : State()
        object UserNotExist : State()
        data class Error(val error: Throwable) : State()
        data class Success(val dynamicSubscription: DynamicSubscription) : State()
    }

    sealed class Action {
        data class SetUserId(val userId: UserId?) : Action()
    }

    private val mutableUserId = MutableStateFlow<UserId?>(null)

    val state: StateFlow<State> = observeUserSubscription().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    private fun observeUserSubscription() = observeUserId()
        .transformLatest { userId ->
            emit(State.Loading)
            when (userId) {
                null -> emit(State.UserNotExist)
                else -> emit(State.Success(getDynamicSubscription(userId)))
            }
        }
        .catch { emit(State.Error(it)) }

    private fun observeUserId(): Flow<UserId?> = mutableUserId.flatMapLatest { userId ->
        when (userId) {
            null -> accountManager.getPrimaryUserId()
            else -> accountManager.getAccount(userId).mapLatest { it?.userId }
        }
    }

    fun perform(action: Action) = when (action) {
        is Action.SetUserId -> onSetUserId(action.userId)
    }

    private fun onSetUserId(userId: UserId?) = viewModelScope.launch {
        mutableUserId.emit(userId)
    }
}
