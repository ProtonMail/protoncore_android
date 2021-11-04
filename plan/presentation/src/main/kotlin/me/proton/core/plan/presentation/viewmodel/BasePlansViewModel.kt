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

import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanDetailsListItem
import me.proton.core.plan.presentation.entity.PlanPricing
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.presentation.viewmodel.ProtonViewModel

internal abstract class BasePlansViewModel(
    private val paymentsOrchestrator: PaymentsOrchestrator
) : ProtonViewModel() {

    protected val _availablePlansState = MutableStateFlow<PlanState>(PlanState.Idle)

    val availablePlansState = _availablePlansState.asStateFlow()

    sealed class PlanState {
        object Idle : PlanState()
        object Processing : PlanState()
        sealed class Success : PlanState() {
            data class Plans(
                val plans: List<PlanDetailsListItem>
            ) : Success()

            data class PaidPlanPayment(val selectedPlan: SelectedPlan, val billing: BillingResult) : Success()
        }

        sealed class Error : PlanState() {
            data class Message(val message: String?) : Error()
        }
    }

    fun register(context: Fragment) {
        paymentsOrchestrator.register(context)
    }

    protected fun createFreePlan(currentlySubscribed: Boolean, selectable: Boolean): PlanDetailsListItem {
        return PlanDetailsListItem.FreePlanDetailsListItem(
            name = SelectedPlan.FREE_PLAN_ID,
            displayName = SelectedPlan.FREE_PLAN_ID,
            currentlySubscribed = currentlySubscribed,
            selectable = selectable
        )
    }

    protected fun Plan.toPaidPlanDetailsItem(
        subscribedPlans: List<PlanDetailsListItem>?,
        upgrade: Boolean
    ) =
        PlanDetailsListItem.PaidPlanDetailsListItem(
            name = name,
            displayName = title,
            price = PlanPricing.fromPlan(this),
            selectable = if (upgrade) true else subscribedPlans.isNullOrEmpty(),
            currentlySubscribed = subscribedPlans?.find { currentPlan ->
                currentPlan.name == id
            } != null,
            upgrade = upgrade,
            renewalDate = null,
            storage = maxSpace,
            members = maxMembers,
            addresses = maxAddresses,
            calendars = maxCalendars,
            domains = maxDomains,
            connections = maxVPN,
            currency = PlanCurrency.valueOf(currency)
        )

    fun startBillingForPaidPlan(userId: UserId?, selectedPlan: SelectedPlan, cycle: SubscriptionCycle) {
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
            startBillingWorkFlow(
                userId = userId,
                selectedPlan = PlanShortDetails(
                    name = selectedPlan.planName,
                    displayName = selectedPlan.planDisplayName,
                    subscriptionCycle = cycle,
                    currency = selectedPlan.currency.toSubscriptionCurrency()
                ),
                codes = null
            )
        }
    }
}
