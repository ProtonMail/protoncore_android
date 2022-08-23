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
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.SupportSignupPaidPlans
import me.proton.core.plan.domain.usecase.GetPlanDefault
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import javax.inject.Inject

@HiltViewModel
internal class SignupPlansViewModel @Inject constructor(
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getPlans: GetPlans,
    private val getPlanDefault: GetPlanDefault,
    @SupportSignupPaidPlans val supportPaidPlans: Boolean,
    paymentsOrchestrator: PaymentsOrchestrator
) : BasePlansViewModel(paymentsOrchestrator) {

    fun getAllPlansForSignup() = flow {
        emit(PlanState.Processing)
        val plans: MutableList<PlanDetailsItem> = mutableListOf()
        val paymentProviders = getAvailablePaymentProviders().filter {
            // It's not possible to setup PayPal during signup, from mobile app.
            it != PaymentProvider.PayPal
        }
        val anyPaymentEnabled = paymentProviders.isNotEmpty()
        if (anyPaymentEnabled && supportPaidPlans) {
            plans.apply {
                addAll(
                    getPlans(userId = null)
                        .map { plan -> plan.toPaidPlanDetailsItem(false) }
                )
                add(createFreePlan(getPlanDefault(userId = null)))
            }
        }
        emit(PlanState.Success.Plans(plans = plans, purchaseEnabled = anyPaymentEnabled))
    }.catch { error ->
        state.tryEmit(PlanState.Error.Exception(error))
    }.onEach { plans ->
        state.tryEmit(plans)
    }.launchIn(viewModelScope)
}
