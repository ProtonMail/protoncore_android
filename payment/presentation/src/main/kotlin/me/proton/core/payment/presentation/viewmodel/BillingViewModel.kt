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

package me.proton.core.payment.presentation.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val billingCommonViewModel: BillingCommonViewModel,
) : ProtonViewModel() {

    val plansValidationState = billingCommonViewModel.plansValidationState
    val subscriptionResult = billingCommonViewModel.subscriptionResult

    fun subscribe(
        userId: UserId?,
        planNames: List<String>,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle,
        paymentType: PaymentType
    ) = billingCommonViewModel.subscribe(userId, planNames, codes, currency, cycle, paymentType)

    fun validatePlan(
        userId: UserId?,
        plans: List<String>,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle
    ) = billingCommonViewModel.validatePlan(userId, plans, codes, currency, cycle)

    fun onThreeDSTokenApproved(
        userId: UserId?,
        planIds: List<String>,
        codes: List<String>? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        token: String
    ) = billingCommonViewModel.onThreeDSTokenApproved(userId, planIds, codes, amount, currency, cycle, token)
}