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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.country.domain.usecase.GetCountry
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsGetPaymentMethodsTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsSubscribeTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsValidatePlanTotal
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.Details
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.payment.domain.usecase.GetAvailablePaymentMethods
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.entity.PaymentOptionUIModel
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

/**
 * ViewModel that returns the available (existing) payment methods (options) that are saved by the user previously.
 * This one should not be used during sign up. In that case, only a new Credit Card input is available.
 */
@HiltViewModel
internal class PaymentOptionsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val availablePaymentMethods: GetAvailablePaymentMethods,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getCurrentSubscription: GetCurrentSubscription,
    validatePlanSubscription: ValidateSubscriptionPlan,
    createPaymentToken: CreatePaymentToken,
    performSubscribe: PerformSubscribe,
    getCountry: GetCountry,
    humanVerificationManager: HumanVerificationManager,
    clientIdProvider: ClientIdProvider,
    override val manager: ObservabilityManager
) : BillingCommonViewModel(
    validatePlanSubscription,
    createPaymentToken,
    performSubscribe,
    getCountry,
    humanVerificationManager,
    clientIdProvider,
    manager
) {

    // it should be private, but because of a bug in Mockk it was not able to mock a spy. and testing it is important!
    internal var currentPlans = mutableListOf<Plan>()

    private val _availablePaymentMethodsState = MutableStateFlow<State>(State.Idle)
    val availablePaymentMethodsState = _availablePaymentMethodsState.asStateFlow()

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
            object SubscriptionInRecoverableError : Error()
            data class General(val error: Throwable) : Error()
        }
    }

    /**
     * Returns the existing available (saved) payment methods for a user.
     */
    fun getAvailablePaymentMethods(userId: UserId) = flow {
        emit(State.Processing)
        val currentSubscription = getCurrentSubscription(userId)
        currentSubscription?.let {
            it.plans.forEach { plan ->
                currentPlans.add(plan)
            }
        } ?: run {
            emit(State.Error.SubscriptionInRecoverableError)
        }

        val paymentProviders = getAvailablePaymentProviders()
        val paymentMethods = availablePaymentMethods(userId, metricData = {
            CheckoutPaymentMethodsGetPaymentMethodsTotal(it.toHttpApiStatus())
        }).filter {
            when (it.type) {
                PaymentMethodType.CARD -> PaymentProvider.CardPayment in paymentProviders
                PaymentMethodType.PAYPAL -> PaymentProvider.PayPal in paymentProviders
            }
        }.mapPaymentMethods()
        val giapPaymentMethod = paymentProviders.createGIAPPaymentMethod()
        emit(
            State.Success.PaymentMethodsSuccess(
                if (giapPaymentMethod != null) paymentMethods + giapPaymentMethod
                else paymentMethods
            )
        )
    }.catch { error ->
        _availablePaymentMethodsState.tryEmit(State.Error.General(error))
    }.onEach { methods ->
        _availablePaymentMethodsState.tryEmit(methods)
    }.launchIn(viewModelScope)

    fun subscribe(
        userId: UserId?,
        planName: String,
        planServices: Int,
        planType: Int,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle,
        paymentType: PaymentType,
        subscriptionManagement: SubscriptionManagement,
    ) = super.subscribe(
        userId,
        currentPlans.createSubscriptionPlansList(planName, planServices, planType),
        codes,
        currency,
        cycle,
        paymentType,
        subscriptionManagement,
        subscribeMetricData = { result, _ -> CheckoutPaymentMethodsSubscribeTotal(result.toHttpApiStatus()) },
        validatePlanMetricData = { CheckoutPaymentMethodsValidatePlanTotal(it.toHttpApiStatus()) }
    )

    fun onThreeDSTokenApproved(
        userId: UserId?,
        planName: String,
        planServices: Int,
        planType: Int,
        codes: List<String>? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        token: ProtonPaymentToken,
        external: SubscriptionManagement
    ) = super.onThreeDSTokenApproved(
        userId,
        currentPlans.createSubscriptionPlansList(planName, planServices, planType),
        codes,
        amount,
        currency,
        cycle,
        token,
        external,
        subscribeMetricData = { result, _ ->
            CheckoutPaymentMethodsSubscribeTotal(result.toHttpApiStatus())
        }
    )

    fun validatePlan(
        userId: UserId?,
        planName: String,
        planServices: Int,
        planType: Int,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle
    ) = super.validatePlan(
        userId,
        currentPlans.createSubscriptionPlansList(planName, planServices, planType),
        codes,
        currency,
        cycle,
        validatePlanMetricData = { CheckoutPaymentMethodsValidatePlanTotal(it.toHttpApiStatus()) }
    )

    private fun Set<PaymentProvider>.createGIAPPaymentMethod() =
        if (contains(PaymentProvider.GoogleInAppPurchase)) {
            val providerName = context.getString(R.string.payments_method_google)
            PaymentOptionUIModel.InAppPurchase(
                id = providerName.lowercase(),
                provider = "$providerName*"
            )
        } else null

    private fun List<PaymentMethod>.mapPaymentMethods() =
        map {
            when (it.type) {
                PaymentMethodType.CARD -> {
                    val card = (it.details as Details.CardDetails).cardDetails
                    PaymentOptionUIModel.PaymentMethod(
                        id = it.id,
                        type = it.type.value,
                        title = context.getString(
                            R.string.payment_cc_list_item,
                            card.brand,
                            card.last4,
                            card.expirationMonth,
                            card.expirationYear
                        ),
                        subtitle = card.name
                    )
                }
                PaymentMethodType.PAYPAL -> {
                    val payPalDetails = it.details as Details.PayPalDetails
                    PaymentOptionUIModel.PaymentMethod(
                        id = it.id,
                        type = it.type.value,
                        title = context.getString(R.string.payment_paypal_list_item),
                        subtitle = payPalDetails.payer
                    )
                }
            }.exhaustive
        }
}
