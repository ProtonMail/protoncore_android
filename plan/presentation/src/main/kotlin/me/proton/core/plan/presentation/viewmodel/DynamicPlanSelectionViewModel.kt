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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.UserManager
import org.jetbrains.annotations.VisibleForTesting
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
internal class DynamicPlanSelectionViewModel @Inject constructor(
    override val manager: ObservabilityManager,
    private val userManager: UserManager,
    private val accountManager: AccountManager,
) : ProtonViewModel(), ObservabilityContext {

    sealed class State {
        object Loading : State()
        data class Idle(val currencies: List<String>) : State()
        data class Free(val selectedPlan: SelectedPlan) : State()
        data class Billing(val selectedPlan: SelectedPlan) : State()
        data class Billed(val selectedPlan: SelectedPlan, val billingResult: BillingResult) : State()
    }

    sealed class Action {
        object Load : Action()
        data class SetUser(val user: DynamicUser) : Action()
        data class SelectPlan(val selectedPlan: SelectedPlan) : Action()
        data class SetBillingResult(val result: BillingResult) : Action()
        object SetBillingCanceled : Action()
    }

    @VisibleForTesting
    internal val localCurrency = Currency.getInstance(Locale.getDefault()).currencyCode

    @VisibleForTesting
    internal val defaultCurrency = availableCurrencies.firstOrNull { it == localCurrency } ?: fallbackCurrency

    private val mutableLoadCount = MutableStateFlow(1)
    private val mutableUser = MutableStateFlow<DynamicUser>(DynamicUser.Unspecified)
    private val mutableSelectedPlan = MutableStateFlow<SelectedPlan?>(null)
    private val mutablePaymentResult = MutableStateFlow<BillingResult?>(null)

    val state = observeCurrencies().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    private fun observeCurrencies() = mutableLoadCount
        .flatMapLatest { observeUserId().distinctUntilChanged() }
        .flatMapLatest { observeUserCurrency(it).distinctUntilChanged() }
        .flatMapLatest { getCurrencies(it) }
        .flatMapLatest { observeState(it) }

    private fun observeUserId(): Flow<UserId?> = mutableUser.flatMapLatest { user ->
        when (user) {
            is DynamicUser.Unspecified -> emptyFlow()
            is DynamicUser.None -> flowOf(null)
            is DynamicUser.Primary -> accountManager.getPrimaryUserId()
            is DynamicUser.ByUserId -> accountManager.getAccount(user.userId).mapLatest { it?.userId }
        }
    }

    private fun observeUserCurrency(userId: UserId?): Flow<String> = when (userId) {
        null -> flowOf(defaultCurrency)
        else -> userManager.observeUser(userId).mapLatest { user -> user?.currency ?: defaultCurrency }
    }

    private fun getCurrencies(userCurrency: String) = flowOf(
        listOf(userCurrency) + (availableCurrencies - userCurrency)
    )

    private fun observeState(currencies: List<String>) = combine(
        mutableSelectedPlan,
        mutablePaymentResult,
    ) { plan, result ->
        when {
            plan == null -> State.Idle(currencies)
            result != null -> when {
                result.paySuccess -> State.Billed(plan, result)
                else -> State.Idle(currencies)
            }

            plan.free -> State.Free(plan)
            else -> State.Billing(plan)
        }
    }

    fun perform(action: Action) = when (action) {
        is Action.Load -> onLoad()
        is Action.SetUser -> onSetUser(action.user)
        is Action.SelectPlan -> onSelectPlan(action.selectedPlan)
        is Action.SetBillingResult -> onSetPaymentResult(action.result)
        is Action.SetBillingCanceled -> onSetBillingCanceled()
    }

    private fun onLoad() = viewModelScope.launch {
        mutableLoadCount.emit(mutableLoadCount.value + 1)
    }

    private fun onSetUser(user: DynamicUser) = viewModelScope.launch {
        mutableUser.emit(user)
    }

    private fun onSelectPlan(selectedPlan: SelectedPlan) = viewModelScope.launch {
        mutableSelectedPlan.emit(selectedPlan)
    }

    private fun onSetPaymentResult(result: BillingResult) = viewModelScope.launch {
        mutablePaymentResult.emit(result)
    }

    private fun onSetBillingCanceled() = viewModelScope.launch {
        mutableSelectedPlan.emit(null)
        mutablePaymentResult.emit(null)
    }

    companion object {
        // ISO 4217 3-letter codes.
        private const val fallbackCurrency = "USD"
        val availableCurrencies = listOf("CHF", "EUR", fallbackCurrency)
    }
}
