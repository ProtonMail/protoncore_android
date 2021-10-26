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

package me.proton.core.plan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.plan.domain.SupportedPaidPlans
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.plan.presentation.entity.SelectedPlan
import javax.inject.Inject

@HiltViewModel
internal class SignupPlansViewModel @Inject constructor(
    private val getPlans: GetPlans,
    @SupportedPaidPlans val supportedPaidPlanNames: List<String>,
    private val paymentsOrchestrator: PaymentsOrchestrator
) : BasePlansViewModel(paymentsOrchestrator) {

    fun getAllPlansForSignup() = flow {
        emit(PlanState.Processing)

        val plans: MutableList<PlanDetailsListItem> = mutableListOf()

        plans.apply {
            addAll(getPlans(supportedPaidPlans = supportedPaidPlanNames.map { it }, userId = null)
                .map {
                    it.toPaidPlanDetailsItem(
                        subscribedPlans = null,
                        upgrade = false
                    )
                }
            )
            add(createFreePlanAsCurrent(current = false, selectable = true))
        }
        emit(PlanState.Success.Plans(plans = plans))
    }.catch { error ->
        _availablePlansState.tryEmit(PlanState.Error.Message(error.message))
    }.onEach { plans ->
        _availablePlansState.tryEmit(plans)
    }.launchIn(viewModelScope)

    override fun startBillingForPaidPlan(userId: UserId?, selectedPlan: SelectedPlan, cycle: SubscriptionCycle) {
        with(paymentsOrchestrator) {
            onPaymentResult { result ->
                result.let { billingResult ->
                    if (billingResult?.paySuccess == true) {
                        viewModelScope.launch {
                            _availablePlansState.emit(PlanState.Success.PaidPlanPayment(selectedPlan, billingResult))
                        }
                    }
                }
            }
        }
        super.startBillingForPaidPlan(userId, selectedPlan, cycle)
    }
}
