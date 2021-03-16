/*
 * Copyright (c) 2020 Proton Technologies AG
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

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.Details
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.GetAvailablePaymentMethods
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.entity.PaymentOptionUIModel
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.exhaustive

/**
 * ViewModel that returns the available (existing) payment methods (options) that are saved by the user previously.
 * This one should not be used during sign up. In that case, only a new Credit Card input is available.
 */
internal class PaymentOptionsViewModel @ViewModelInject constructor(
    private val availablePaymentMethods: GetAvailablePaymentMethods,
    private val getCurrentSubscription: GetCurrentSubscription,
    @ApplicationContext private val context: Context,
    private val billingViewModel: BillingViewModel
) : ProtonViewModel() {

    // it should be private, but because of a bug in Mockk it was not able to mock a spy. and testing it is important!
    internal var currentPlans = mutableListOf<String>()

    private val _availablePaymentMethodsState = MutableStateFlow<State>(State.Idle)
    val availablePaymentMethodsState = _availablePaymentMethodsState.asStateFlow()

    val subscriptionResult = billingViewModel.subscriptionResult
    val plansValidationState = billingViewModel.plansValidationState

    /**
     * Represents state sealed class with all possible outcomes as subclasses, needed to inform the call site for what
     * is going on in the process, as well as the outcome of it.
     */
    sealed class State {
        object Idle : State()
        object Processing : State()
        sealed class Success : State() {
            data class PaymentMethodsSuccess(val availablePaymentMethods: List<PaymentOptionUIModel>) : Success()
        }

        sealed class Error : State() {
            data class SubscriptionInRecoverableError(val message: String?) : Error()
            data class Message(val message: String?) : Error()
        }
    }

    /**
     * Returns the existing available (saved) payment methods for a user.
     */
    fun getAvailablePaymentMethods(userId: UserId) = flow {
        emit(State.Processing)
        try {
            val currentSubscription = getCurrentSubscription(userId)
            currentSubscription.plans.forEach { plan ->
                currentPlans.add(plan.id)
            }
        } catch (apiException: ApiException) {
            if (NO_ACTIVE_SUBSCRIPTION != (apiException.error as ApiResult.Error.Http).proton?.code) {
                emit(State.Error.SubscriptionInRecoverableError(apiException.message))
            }
        }

        val paymentMethods = availablePaymentMethods(userId).map {
            when (it.type) {
                PaymentMethodType.CARD -> {
                    val card = (it.details as Details.CardDetails).cardDetails
                    PaymentOptionUIModel(
                        it.id,
                        it.type.ordinal,
                        context.getString(
                            R.string.payment_cc_list_item,
                            card.brand,
                            card.last4,
                            card.expirationMonth,
                            card.expirationYear
                        ),
                        card.name
                    )
                }
                PaymentMethodType.PAYPAL -> {
                    val payPalDetails = it.details as Details.PayPalDetails
                    PaymentOptionUIModel(
                        it.id,
                        it.type.ordinal,
                        context.getString(R.string.payment_paypal_list_item),
                        payPalDetails.payer
                    )
                }
            }.exhaustive
        }
        emit(State.Success.PaymentMethodsSuccess(paymentMethods))
    }.catch { error ->
        _availablePaymentMethodsState.tryEmit(State.Error.Message(error.message))
    }.onEach { methods ->
        _availablePaymentMethodsState.tryEmit(methods)
    }.launchIn(viewModelScope)

    fun subscribe(
        userId: UserId?,
        planId: String,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle,
        paymentType: PaymentType
    ) = billingViewModel.subscribe(
        userId, currentPlans.plus(planId), codes, currency, cycle, paymentType
    )

    fun onThreeDSTokenApproved(
        userId: UserId?,
        planId: String,
        codes: List<String>? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        token: String
    ) = billingViewModel.onThreeDSTokenApproved(
        userId, currentPlans.plus(planId), codes, amount, currency, cycle, token
    )

    fun validatePlan(
        userId: UserId?,
        planId: String,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle
    ) = billingViewModel.validatePlan(userId, currentPlans.plus(planId).distinct(), codes, currency, cycle)

    companion object {
        const val NO_ACTIVE_SUBSCRIPTION = 22110
    }
}
