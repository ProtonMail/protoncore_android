/*
 * Copyright (c) 2022 Proton Technologies AG
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.plan.domain.SupportUpgradePaidPlans
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.usecase.CheckUnredeemedGooglePurchase
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class DynamicUpgradePlanViewModel @Inject constructor(
    override val manager: ObservabilityManager,
    private val accountManager: AccountManager,
    private val checkUnredeemedGooglePurchase: CheckUnredeemedGooglePurchase,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    @SupportUpgradePaidPlans private val supportPaidPlans: Boolean,
) : ProtonViewModel(), ObservabilityContext {

    sealed class State {
        object Idle : State()
        object Loading : State()
        object UpgradeNotAvailable : State()
        data class UnredeemedPurchase(val purchase: UnredeemedGooglePurchase) : State()
        data class Error(val error: Throwable) : State()
    }

    sealed class Action {
        object Load : Action()
        data class SetUser(val user: DynamicUser) : Action()
    }

    private val mutableLoadCount = MutableStateFlow(1)
    private val mutableUser = MutableStateFlow<DynamicUser>(DynamicUser.Primary)

    val state: StateFlow<State> = observeState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    private fun observeState() = mutableLoadCount
        .flatMapLatest { observeUserId().filterNotNull().distinctUntilChanged() }
        .flatMapLatest { loadPaymentProviders(it) }

    private fun observeUserId(): Flow<UserId?> = mutableUser.flatMapLatest { user ->
        when (user) {
            is DynamicUser.Unspecified -> emptyFlow()
            is DynamicUser.None -> flowOf(null)
            is DynamicUser.Primary -> accountManager.getPrimaryUserId()
            is DynamicUser.ByUserId -> accountManager.getAccount(user.userId).mapLatest { it?.userId }
        }
    }

    private suspend fun loadPaymentProviders(userId: UserId) = flow {
        emit(State.Loading)
        when {
            !supportPaidPlans -> emit(State.UpgradeNotAvailable)
            getAvailablePaymentProviders.invoke(userId).isEmpty() -> emit(State.UpgradeNotAvailable)
            else -> emitAll(loadUnredeemedPurchase(userId))
        }
    }.catch { emit(State.Error(it)) }

    private suspend fun loadUnredeemedPurchase(userId: UserId) = flow {
        emit(State.Loading)
        when (val unredeemedPurchase = checkUnredeemedGooglePurchase.invoke(userId)) {
            null -> emit(State.Idle)
            else -> emit(State.UnredeemedPurchase(unredeemedPurchase))
        }
    }.catch { emit(State.Error(it)) }

    fun perform(action: Action) = when (action) {
        is Action.Load -> onLoad()
        is Action.SetUser -> onSetUser(action.user)
    }

    private fun onLoad() = viewModelScope.launch {
        mutableLoadCount.emit(mutableLoadCount.value + 1)
    }

    private fun onSetUser(user: DynamicUser) = viewModelScope.launch {
        mutableUser.emit(user)
    }
}
