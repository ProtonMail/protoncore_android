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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.filterBy
import me.proton.core.plan.domain.usecase.GetDynamicPlans
import me.proton.core.plan.presentation.entity.DynamicPlanFilter
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class DynamicPlanListViewModel @Inject constructor(
    override val manager: ObservabilityManager,
    private val userManager: UserManager,
    private val accountManager: AccountManager,
    private val getDynamicPlans: GetDynamicPlans
) : ProtonViewModel(), ObservabilityContext {

    sealed class State {
        object Loading : State()
        data class Success(val plans: List<DynamicPlan>, val filter: DynamicPlanFilter) : State()
        data class Error(val error: Throwable) : State()
    }

    sealed class Action {
        object Load : Action()
        data class SetUserId(val userId: UserId) : Action()
        data class SetCycle(val cycle: Int) : Action()
        data class SetCurrency(val currency: String) : Action()
    }

    private val mutableLoadCount = MutableStateFlow(1)
    private val mutableUserId = MutableStateFlow<UserId?>(null)
    private val mutablePlanFilter = MutableStateFlow(DynamicPlanFilter())

    private val cycleFilter = mutablePlanFilter.mapLatest { it.cycle }.distinctUntilChanged()
    private val currencyFilter = mutablePlanFilter.mapLatest { it.currency }.distinctUntilChanged()

    val state: StateFlow<State> = observeUserDynamicPlans().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    fun getUserId(): UserId? = mutableUserId.value

    private fun observeUserDynamicPlans() = mutableLoadCount
        .flatMapLatest { observeUserId() }
        .flatMapLatest { observeFilter(it) }
        .flatMapLatest { loadDynamicPlans(it) }

    private fun observeUserId(): Flow<UserId?> = mutableUserId.flatMapLatest { userId ->
        when (userId) {
            null -> accountManager.getPrimaryUserId()
            else -> accountManager.getAccount(userId).mapLatest { it?.userId }
        }
    }

    private fun observeFilter(userId: UserId?) = combine(
        cycleFilter,
        observeCurrency(userId)
    ) { cycle, currency ->
        DynamicPlanFilter(userId, cycle, currency)
    }

    private fun observeCurrency(userId: UserId?): Flow<String?> = currencyFilter.flatMapLatest { currency ->
        when (currency) {
            null -> userId?.let { userManager.observeUser(it).mapLatest { user -> user?.currency } } ?: flowOf(null)
            else -> flowOf(currency)
        }
    }

    private suspend fun loadDynamicPlans(filter: DynamicPlanFilter) = flow {
        emit(State.Loading)
        val filteredPlans = getDynamicPlans(filter.userId).filterBy(filter.cycle, filter.currency)
        emit(State.Success(filteredPlans, filter))
    }.catch { emit(State.Error(it)) }

    fun perform(action: Action) = when (action) {
        is Action.Load -> onLoad()
        is Action.SetUserId -> onSetUserId(action.userId)
        is Action.SetCycle -> onSetCycle(action.cycle)
        is Action.SetCurrency -> onSetCurrency(action.currency)
    }

    private fun onLoad() = viewModelScope.launch {
        mutableLoadCount.emit(mutableLoadCount.value + 1)
    }

    private fun onSetUserId(userId: UserId?) = viewModelScope.launch {
        mutableUserId.emit(userId)
    }

    private fun onSetCycle(cycle: Int) = viewModelScope.launch {
        mutablePlanFilter.emit(mutablePlanFilter.value.copy(cycle = cycle))
    }

    private fun onSetCurrency(currency: String?) = viewModelScope.launch {
        mutablePlanFilter.emit(mutablePlanFilter.value.copy(currency = currency))
    }
}
