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
import me.proton.core.payment.domain.usecase.PurchaseEnabled
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.domain.SupportedSignupPaidPlans
import me.proton.core.plan.domain.usecase.GetPlanDefault
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.plan.presentation.entity.SupportedPlan
import javax.inject.Inject

@HiltViewModel
internal class SignupPlansViewModel @Inject constructor(
    private val getPlans: GetPlans,
    private val getPlanDefault: GetPlanDefault,
    @SupportedSignupPaidPlans val supportedPaidPlanNames: List<SupportedPlan>,
    purchaseEnabled: PurchaseEnabled,
    paymentsOrchestrator: PaymentsOrchestrator
) : BasePlansViewModel(purchaseEnabled, paymentsOrchestrator) {

    fun getAllPlansForSignup() = flow {
        emit(PlanState.Processing)
        val plans: MutableList<PlanDetailsItem> = mutableListOf()
        val purchaseStatus = getPurchaseStatus()
        if (purchaseStatus) {
            plans.apply {
                addAll(
                    getPlans(supportedPaidPlans = supportedPaidPlanNames.map { it.name }, userId = null)
                        .map { plan ->
                            plan.toPaidPlanDetailsItem(
                                supportedPaidPlanNames.firstOrNull { it.name == plan.name }?.starred ?: false
                            )
                        }
                )
                add(createFreePlan(getPlanDefault(userId = null)))
            }
        }
        emit(PlanState.Success.Plans(plans = plans, purchaseEnabled = purchaseStatus))
    }.catch { error ->
        state.tryEmit(PlanState.Error(error))
    }.onEach { plans ->
        state.tryEmit(plans)
    }.launchIn(viewModelScope)
}
