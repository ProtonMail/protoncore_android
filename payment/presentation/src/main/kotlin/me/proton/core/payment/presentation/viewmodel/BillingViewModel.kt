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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Country
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithExistingPaymentMethod
import me.proton.core.payment.domain.usecase.GetCountries
import me.proton.core.payment.domain.usecase.GetCountryCode
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.exhaustive
import studio.forface.viewstatestore.ViewState
import studio.forface.viewstatestore.ViewStateStore
import studio.forface.viewstatestore.ViewStateStoreScope
import javax.inject.Inject

/**
 * ViewModel to serve the billing activity or fragment.
 * It's responsibility is to provide payments functionality.
 */
class BillingViewModel @ViewModelInject @Inject constructor(
    private val validatePlanSubscription: ValidateSubscriptionPlan,
    private val createPaymentToken: CreatePaymentToken,
    private val createPaymentTokenWithExistingPaymentMethod: CreatePaymentTokenWithExistingPaymentMethod,
    private val performSubscribe: PerformSubscribe,
    private val getCountries: GetCountries,
    private val getCountryCode: GetCountryCode
) : ProtonViewModel(), ViewStateStoreScope {

    val countriesResult = ViewStateStore<List<Country>>(ViewState.Loading).lock
    val subscriptionResult = ViewStateStore<State>().lock
    val plansValidationState = ViewStateStore<PlansValidationState>().lock

    init {
        getBillingCountries()
    }

    /**
     * Represents main subscription state sealed class with all possible outcomes as subclasses needed to inform the
     * call site for what is going on in the process, as well as the outcome of it.
     */
    sealed class State {
        object Processing : State()

        sealed class Success : State() {
            data class SubscriptionPlanValidated(val subscriptionStatus: SubscriptionStatus) : Success()
            data class SubscriptionCreated(val subscriptionStatus: Subscription, val paymentToken: String?) : Success()
            data class TokenCreated(val paymentToken: PaymentToken.CreatePaymentTokenResult) : Success()
            data class SignUpTokenReady(val paymentToken: String) : Success()
        }

        sealed class Incomplete : State() {
            data class TokenApprovalNeeded(val paymentToken: PaymentToken.CreatePaymentTokenResult, val amount: Long) :
                Incomplete()
        }

        sealed class Error : State() {
            data class Message(val message: String?) : Error()
            object SignUpWithPaymentMethodUnsupported : Error()
        }
    }

    sealed class PlansValidationState {
        object Processing : PlansValidationState()
        data class Success(val subscription: SubscriptionStatus) : PlansValidationState()

        sealed class Error : PlansValidationState() {
            data class Message(val message: String?) : Error()
        }
    }

    private fun getBillingCountries() {
        val countries = runCatching {
            getCountries()
        }
        countriesResult.postData(countries.getOrDefault(emptyList()), true)
    }

    /**
     * Creates new subscription with a completely new credit card that user will enter details for.
     * If you want to pay for a subscription during sign up, please use:
     *  - [PaymentType.CreditCard] this is only supported.
     * If you want to pay with existing payment method id (previously saved), please use:
     *  - [PaymentType.PaymentMethod]
     *  Otherwise if you want to pay for an upgrade when the user is logged in, please use:
     *  - [PaymentType.CreditCard] or [PaymentType.PayPal]
     */
    fun subscribe(
        sessionId: SessionId?,
        planIds: List<String>,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle,
        paymentType: PaymentType
    ) = flow {
        emit(State.Processing)
        val signUp = sessionId == null

        val subscription = validatePlanSubscription(sessionId, codes, planIds, currency, cycle)
        emit(State.Success.SubscriptionPlanValidated(subscription))

        if (!signUp && subscription.amountDue == 0L) {
            // directly create the subscription
            val subscriptionResult =
                performSubscribe(sessionId!!, 0, currency, cycle, planIds, codes, paymentToken = null)
            emit(State.Success.SubscriptionCreated(subscriptionResult, null))
        } else {
            if (signUp && paymentType is PaymentType.PaymentMethod) {
                emit(State.Error.SignUpWithPaymentMethodUnsupported)
                return@flow
            }
            // region create payment token with the amountDue (will most probably lead to a token approval screen).
            val paymentTokenResult = when (paymentType) {
                is PaymentType.PaymentMethod -> {
                    createPaymentTokenWithExistingPaymentMethod(
                        sessionId = sessionId,
                        amount = subscription.amountDue,
                        currency = currency,
                        paymentMethodId = paymentType.paymentMethodId
                    )
                }
                else -> {
                    val paymentTypeRefined = if (paymentType is PaymentType.CreditCard) {
                        val countryCode = getCountryCode(paymentType.card.country)
                        paymentType.copy(card = (paymentType.card as Card.CardWithPaymentDetails).copy(country = countryCode))
                    } else paymentType
                    createPaymentToken(sessionId, subscription.amountDue, currency, paymentTypeRefined)
                }
            }.exhaustive
            emit(State.Success.TokenCreated(paymentTokenResult))

            if (paymentTokenResult.status == PaymentTokenStatus.CHARGEABLE) {
                val amount = subscription.amountDue
                val token = paymentTokenResult.token
                emit(onTokenApproved(sessionId, planIds, codes, amount, currency, cycle, token))
            } else {
                // should show 3DS approval URL WebView
                emit(State.Incomplete.TokenApprovalNeeded(paymentTokenResult, subscription.amountDue))
            }
            // endregion
        }
    }.catch {
        subscriptionResult.post(State.Error.Message(it.message))
    }.onEach {
        subscriptionResult.post(it)
    }.launchIn(viewModelScope)

    /**
     * Completes the subscription after payment is 3DS approved in a WebView.
     * Should be called only for 3DS credit cards and only after the token has been approved.
     */
    fun onThreeDSTokenApproved(
        sessionId: SessionId?,
        planIds: List<String>,
        codes: List<String>? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        token: String
    ) = flow {
        emit(onTokenApproved(sessionId, planIds, codes, amount, currency, cycle, token))
    }.catch {
        subscriptionResult.post(State.Error.Message(it.message))
    }.onEach {
        subscriptionResult.post(it)
    }.launchIn(viewModelScope)

    /**
     * Validates the subscription plan with the API. It should return more info for later payment processing, such as
     * the real amount (amountDue) which is the plan amount minus any coupons or credits (already on the account).
     */
    fun validatePlan(
        sessionId: SessionId?,
        planIds: List<String>,
        codes: List<String>? = null,
        currency: Currency,
        cycle: SubscriptionCycle
    ) = flow {
        emit(PlansValidationState.Processing)
        emit(PlansValidationState.Success(validatePlanSubscription(sessionId, codes, planIds, currency, cycle)))
    }.catch { error ->
        plansValidationState.post(PlansValidationState.Error.Message(error.message))
    }.onEach { subscriptionState ->
        this.plansValidationState.post(subscriptionState)
    }.launchIn(viewModelScope)

    private suspend fun onTokenApproved(
        sessionId: SessionId?,
        planIds: List<String>,
        codes: List<String>? = null,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        token: String
    ): State =
        if (sessionId == null) {
            // subscription should be created by the sign up module. return payment info (needed for Human Ver headers).
            State.Success.SignUpTokenReady(token)
        } else {
            State.Success.SubscriptionCreated(
                performSubscribe(sessionId, amount, currency, cycle, planIds, codes, token),
                token
            )
        }
}
