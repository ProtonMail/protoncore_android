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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGetDynamicSubscriptionTotal
import me.proton.core.observability.domain.metrics.CheckoutScreenViewTotal
import me.proton.core.payment.domain.entity.DynamicSubscription
import me.proton.core.payment.domain.usecase.CanUpgradeFromMobile
import me.proton.core.payment.domain.usecase.GetDynamicSubscription
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.plan.presentation.usecase.ObserveUserCurrency
import me.proton.core.plan.presentation.usecase.ObserveUserId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.coroutine.withResultContextFlow
import javax.inject.Inject

@HiltViewModel
internal class DynamicSubscriptionViewModel @Inject constructor(
    override val manager: ObservabilityManager,
    private val observeUserId: ObserveUserId,
    private val observeUserCurrency: ObserveUserCurrency,
    private val getDynamicSubscription: GetDynamicSubscription,
    private val canUpgradeFromMobile: CanUpgradeFromMobile,
) : ProtonViewModel(), ObservabilityContext {

    sealed class State {
        object Loading : State()
        object UserNotExist : State()
        data class Error(val error: Throwable) : State()
        data class Success(
            val dynamicSubscription: DynamicSubscription,
            val canUpgradeFromMobile: Boolean,
            val userCurrency: String,
        ) : State()
    }

    sealed class Action {
        object Load : Action()
        data class SetUser(val user: DynamicUser) : Action()
    }

    private val mutableLoadCount = MutableStateFlow(1)

    val state: StateFlow<State> = observeState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    private fun observeState() = mutableLoadCount
        .flatMapLatest { observeUserId().distinctUntilChanged() }
        .flatMapLatest { userId -> observeUserCurrency(userId).distinctUntilChanged().map { userId to it } }
        .flatMapLatest { (userId, currency) -> loadDynamicSubscription(userId, currency) }

    private suspend fun loadDynamicSubscription(userId: UserId?, currency: String) = withResultContextFlow {
        it.onResultEnqueue("getDynamicSubscriptions") { CheckoutGetDynamicSubscriptionTotal(this) }
        emit(State.Loading)
        when (userId) {
            null -> emit(State.UserNotExist)
            else -> emit(
                State.Success(
                    dynamicSubscription = getDynamicSubscription(userId),
                    canUpgradeFromMobile = canUpgradeFromMobile.invoke(userId),
                    userCurrency = currency
                )
            )
        }
    }.catch {
        emit(State.Error(it))
    }

    fun perform(action: Action) = when (action) {
        is Action.Load -> onLoad()
        is Action.SetUser -> onSetUser(action.user)
    }

    fun onScreenView() {
        manager.enqueue(
            CheckoutScreenViewTotal(CheckoutScreenViewTotal.ScreenId.dynamicPlansCurrentSubscription)
        )
    }

    private fun onLoad() = viewModelScope.launch {
        mutableLoadCount.emit(mutableLoadCount.value + 1)
    }

    private fun onSetUser(user: DynamicUser) = viewModelScope.launch {
        observeUserId.setUser(user)
    }
}
