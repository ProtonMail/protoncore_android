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

package me.proton.core.paymentcommon.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.paymentcommon.domain.entity.Currency
import me.proton.core.paymentcommon.domain.entity.PaymentType
import me.proton.core.paymentcommon.domain.entity.SubscriptionCycle
import me.proton.core.paymentcommon.domain.usecase.PaymentProvider
import me.proton.core.paymentcommon.presentation.ActivePaymentProvider
import me.proton.core.paymentcommon.presentation.entity.BillingInput
import me.proton.core.paymentcommon.presentation.entity.PlanShortDetails
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
public class BillingViewModel @Inject constructor(
    private val billingCommonViewModel: BillingCommonViewModel,
    private val activePaymentProvider: ActivePaymentProvider
) : ProtonViewModel() {

    public val plansValidationState: StateFlow<BillingCommonViewModel.PlansValidationState> =
        billingCommonViewModel.plansValidationState
    public val subscriptionResult: StateFlow<BillingCommonViewModel.State> = billingCommonViewModel.subscriptionResult

    private val _paymentProvidersState = MutableStateFlow<PaymentProvidersState>(PaymentProvidersState.Idle)
    public val paymentProvidersResult: StateFlow<PaymentProvidersState> = _paymentProvidersState.asStateFlow()

    private val _userInteractionState = MutableStateFlow<UserInteractionState>(UserInteractionState.Idle)
    public val userInteractionState: StateFlow<UserInteractionState> = _userInteractionState.asStateFlow()

    public sealed class UserInteractionState {
        public object Idle : UserInteractionState()
        public data class OnPay(val input: BillingInput) : UserInteractionState()
        public data class OnLoadingStateChange(val loading: Boolean) : UserInteractionState()
        public data class PlanValidated(val plan: PlanShortDetails) : UserInteractionState()
    }

    public sealed class PaymentProvidersState {
        public object Idle : PaymentProvidersState()
        public object Processing : PaymentProvidersState()

        public object PaymentProvidersEmpty : PaymentProvidersState()

        public data class Success(
            val activeProvider: PaymentProvider,
            val nextPaymentProviderTextResource: Int?
        ) : PaymentProvidersState()

        public sealed class Error : PaymentProvidersState() {
            public data class Message(val error: String?) : Error()
        }
    }

    init {
        viewModelScope.launch { observePaymentProviders() }
    }

    public fun onPay(input: BillingInput) {
        _userInteractionState.tryEmit(UserInteractionState.OnPay(input))
    }

    public fun onLoadingStateChange(loading: Boolean) {
        _userInteractionState.tryEmit(UserInteractionState.OnLoadingStateChange(loading))
    }

    public fun setPlan(plan: PlanShortDetails) {

        _userInteractionState.tryEmit(UserInteractionState.PlanValidated(plan))
    }

    public fun switchNextPaymentProvider() {
        val paymentProvider = activePaymentProvider.switchNextPaymentProvider()
        if (paymentProvider == null) {
            _paymentProvidersState.tryEmit(PaymentProvidersState.PaymentProvidersEmpty)
        } else {
            _paymentProvidersState.tryEmit(
                PaymentProvidersState.Success(
                    paymentProvider,
                    activePaymentProvider.getNextPaymentProviderText()
                )
            )
        }
    }

    /**
     * Observes the most recent payment providers availability.
     */
    private fun observePaymentProviders() = flow {
        emit(PaymentProvidersState.Processing)
        val paymentProvider = activePaymentProvider.getActivePaymentProvider()
        if (paymentProvider == null) {
            emit(PaymentProvidersState.PaymentProvidersEmpty)
        } else {
            emit(PaymentProvidersState.Success(paymentProvider, activePaymentProvider.getNextPaymentProviderText()))
        }
    }.catch { error ->
        _paymentProvidersState.tryEmit(PaymentProvidersState.Error.Message(error.message))
    }.onEach { subscriptionState ->
        _paymentProvidersState.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    public fun subscribe(
        userId: UserId?,
        planNames: List<String>,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle,
        paymentType: PaymentType
    ): Job = billingCommonViewModel.subscribe(userId, planNames, codes, currency, cycle, paymentType)

    public fun validatePlan(
        userId: UserId?,
        plans: List<String>,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle
    ): Job = billingCommonViewModel.validatePlan(userId, plans, codes, currency, cycle)

    public fun onThreeDSTokenApproved(
        userId: UserId?,
        planIds: List<String>,
        codes: List<String>? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        token: String
    ): Job = billingCommonViewModel.onThreeDSTokenApproved(userId, planIds, codes, amount, currency, cycle, token)
}
