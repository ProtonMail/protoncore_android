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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutScreenViewTotal
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.domain.SupportSignupPaidPlans
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.isFree
import me.proton.core.plan.domain.usecase.GetDynamicPlansAdjustedPrices
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class DynamicSelectPlanViewModel @Inject constructor(
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getDynamicPlans: GetDynamicPlansAdjustedPrices,
    override val observabilityManager: ObservabilityManager,
    @SupportSignupPaidPlans val supportPaidPlans: Boolean
) : ProtonViewModel(), ObservabilityContext {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class FreePlanOnly(val plan: DynamicPlan) : State()
        data class FreePlanSelected(val plan: SelectedPlan) : State()
        data class PaidPlanSelected(val plan: SelectedPlan, val result: BillingResult) : State()
        data class Error(val error: Throwable) : State()
    }

    sealed class Action {
        object Load : Action()
        data class SelectFreePlan(val plan: SelectedPlan) : Action()
        data class SelectPaidPlan(val plan: SelectedPlan, val result: BillingResult) : Action()
    }

    private val mutableLoadCount = MutableStateFlow(1)
    private val mutableSelectedItem = MutableStateFlow<Pair<SelectedPlan?, BillingResult?>>(Pair(null, null))

    val state: StateFlow<State> = observeState().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = State.Loading
    )

    private fun observeState() = mutableLoadCount
        .flatMapLatest { mutableSelectedItem }
        .flatMapLatest { observeState(it) }

    fun perform(action: Action) = when (action) {
        Action.Load -> onLoad()
        is Action.SelectFreePlan -> onSelectFreePlan(action.plan)
        is Action.SelectPaidPlan -> onSelectPaidPlan(action.plan, action.result)
    }

    fun onScreenView() {
        enqueueObservability(CheckoutScreenViewTotal(CheckoutScreenViewTotal.ScreenId.dynamicPlanSelection))
    }

    private fun onLoad() = viewModelScope.launch {
        mutableLoadCount.update { it + 1 }
    }

    private fun onSelectFreePlan(plan: SelectedPlan) = viewModelScope.launch {
        mutableSelectedItem.emit(Pair(plan, null))
    }

    private fun onSelectPaidPlan(
        plan: SelectedPlan,
        result: BillingResult
    ) = viewModelScope.launch {
        mutableSelectedItem.emit(Pair(plan, result))
    }

    private suspend fun observeState(selectedItem: Pair<SelectedPlan?, BillingResult?>) = flow {
        emit(State.Loading)
        val (plan, result) = selectedItem
        when {
            plan != null && result != null -> emit(State.PaidPlanSelected(plan, result))
            plan != null -> emit(State.FreePlanSelected(plan))
            !supportPaidPlans -> emit(State.FreePlanOnly(getFreePlan()))
            getPaymentProvidersForSignup().isEmpty() -> emit(State.FreePlanOnly(getFreePlan()))
            else -> emit(State.Idle)
        }
    }.catch {
        emit(State.Error(it))
    }

    private suspend fun getPaymentProvidersForSignup(): Collection<PaymentProvider> =
        getAvailablePaymentProviders().filter {
            // It's not possible to setup PayPal during signup, from mobile app.
            it != PaymentProvider.PayPal
        }

    private suspend fun getFreePlan(): DynamicPlan =
        requireNotNull(getDynamicPlans(userId = null).plans.firstOrNull { it.isFree() }) {
            "Could not find a free plan."
        }
}
