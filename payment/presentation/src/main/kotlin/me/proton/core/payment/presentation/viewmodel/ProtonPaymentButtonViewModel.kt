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

package me.proton.core.payment.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.yield
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingLaunchBillingTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingProductQueryTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingQuerySubscriptionsTotal
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.extension.getCreatePaymentTokenObservabilityData
import me.proton.core.payment.domain.extension.getValidatePlanObservabilityData
import me.proton.core.payment.domain.usecase.ConvertToObservabilityGiapStatus
import me.proton.core.payment.domain.usecase.GetPreferredPaymentProvider
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PaymentProvider.CardPayment
import me.proton.core.payment.domain.usecase.PaymentProvider.GoogleInAppPurchase
import me.proton.core.payment.presentation.LogTag
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent.Error
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.SubscriptionManagement.GOOGLE_MANAGED
import me.proton.core.plan.domain.usecase.GetDynamicSubscription
import me.proton.core.plan.domain.usecase.PerformGiapPurchase
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.ResultCollector
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@HiltViewModel
internal class ProtonPaymentButtonViewModel @Inject constructor(
    private val activityProvider: ActivityProvider,
    private val convertToObservabilityGiapStatus: Optional<ConvertToObservabilityGiapStatus>,
    private val getPreferredPaymentProvider: GetPreferredPaymentProvider,
    override val observabilityManager: ObservabilityManager,
    private val performGiapPurchase: Optional<PerformGiapPurchase<Activity>>,
    private val getCurrentSubscription: GetDynamicSubscription
) : ProtonViewModel(), ObservabilityContext {
    private val attachedButtonIds = mutableSetOf<Int>()
    private val buttonStates = mutableMapOf<Int, MutableStateFlow<ButtonState>>()
    private val paymentEvents = mutableMapOf<Int, MutableSharedFlow<ProtonPaymentEvent>>()

    fun onButtonAttached(buttonId: Int) {
        require(!attachedButtonIds.contains(buttonId)) {
            "Cannot attach two buttons with the same ID ($buttonId)."
        }
        attachedButtonIds.add(buttonId)
    }

    fun onButtonDetached(buttonId: Int) {
        attachedButtonIds.remove(buttonId)
    }

    @Suppress("LongParameterList")
    fun onPayClicked(
        buttonId: Int,
        currency: String,
        cycle: Int,
        paymentProvider: PaymentProvider?,
        plan: DynamicPlan,
        userId: UserId?
    ) = viewModelScope.launchWithResultContext {
        val resolvedPaymentProvider = paymentProvider ?: getPreferredPaymentProvider(userId)
        onResultEnqueueObservabilityEvents(resolvedPaymentProvider)

        emitButtonState(ButtonState.Loading, forButton = buttonId)
        emitButtonState(ButtonState.Disabled, exceptButton = buttonId)
        yield() // needed for testing; otherwise button state will be swallowed

        val lastEvent = flow {
            emit(ProtonPaymentEvent.Loading)

            if (userId != null) {
                val subscription = getCurrentSubscription(userId)
                if (subscription == null) {
                    emit(Error.Generic(Exception("Could not get current subscription.")))
                    return@flow
                } else if (subscription.external == GOOGLE_MANAGED && subscription.deeplink != null) {
                    emit(Error.SubscriptionManagedByOtherApp(userId, subscription.deeplink!!))
                    return@flow
                }
            }

            when (resolvedPaymentProvider) {
                CardPayment ->
                    emit(ProtonPaymentEvent.StartRegularBillingFlow(plan, cycle, currency))

                GoogleInAppPurchase -> {
                    val activity = requireNotNull(activityProvider.lastResumed) {
                        "Could not get last activity."
                    }
                    val purchaseResult = performGiapPurchase.getOrNull()?.invoke(
                        activity,
                        cycle,
                        plan,
                        userId
                    )
                    val event = onPurchaseResult(cycle, currency, plan, purchaseResult)
                    emit(event)
                }

                else -> emit(Error.UnsupportedPaymentProvider)
            }
        }.catch {
            CoreLogger.e(LogTag.UNKNOWN_ERROR, it)
            emit(Error.Generic(it))
        }.onEach {
            emitPaymentEvent(it, buttonId)
        }.lastOrNull()

        if (lastEvent !is ProtonPaymentEvent.GiapSuccess) {
            emitButtonState(ButtonState.Idle)
        }
    }

    fun buttonStates(buttonId: Int): StateFlow<ButtonState> =
        getButtonStateFlow(buttonId).asStateFlow()

    fun paymentEvents(buttonId: Int): SharedFlow<ProtonPaymentEvent> =
        getPaymentEventFlow(buttonId).asSharedFlow()

    /** Emits a [state] for the button with the given [id][forButton]. */
    private fun emitButtonState(state: ButtonState, forButton: Int) {
        buttonStates.forEach { (id, mutableStateFlow) ->
            if (id == forButton) {
                mutableStateFlow.value = state
            }
        }
    }

    /** Emits a [state] for all buttons, except for the button with [id][exceptButton]. */
    private fun emitButtonState(state: ButtonState, exceptButton: Int? = null) {
        buttonStates.forEach { (id, mutableStateFlow) ->
            if (id != exceptButton) {
                mutableStateFlow.value = state
            }
        }
    }

    private suspend fun emitPaymentEvent(event: ProtonPaymentEvent, forButton: Int) {
        getPaymentEventFlow(forButton).emit(event)
    }

    private fun getButtonStateFlow(buttonId: Int): MutableStateFlow<ButtonState> =
        buttonStates.getOrPut(buttonId) { MutableStateFlow(initialButtonState) }

    private fun getPaymentEventFlow(buttonId: Int): MutableSharedFlow<ProtonPaymentEvent> =
        paymentEvents.getOrPut(buttonId) { MutableSharedFlow(extraBufferCapacity = RESULT_EVENT_BUFFER) }

    private fun onPurchaseResult(
        cycle: Int,
        originalCurrency: String,
        plan: DynamicPlan,
        result: PerformGiapPurchase.Result?
    ): ProtonPaymentEvent = when (result) {
        is PerformGiapPurchase.Result.Error.EmptyCustomerId -> Error.EmptyCustomerId
        is PerformGiapPurchase.Result.Error.GiapUnredeemed -> Error.GiapUnredeemed(
            cycle = result.cycle,
            googleProductId = result.googleProductId,
            googlePurchase = result.googlePurchase,
            originalCurrency = originalCurrency,
            plan = result.plan
        )

        is PerformGiapPurchase.Result.Error.GoogleProductDetailsNotFound -> Error.GoogleProductDetailsNotFound
        is PerformGiapPurchase.Result.Error.PurchaseNotFound -> Error.PurchaseNotFound
        is PerformGiapPurchase.Result.Error.RecoverableBillingError -> Error.RecoverableBillingError
        is PerformGiapPurchase.Result.Error.UnrecoverableBillingError -> Error.UnrecoverableBillingError
        is PerformGiapPurchase.Result.Error.UserCancelled -> Error.UserCancelled
        is PerformGiapPurchase.Result.GiapSuccess -> ProtonPaymentEvent.GiapSuccess(
            plan = plan,
            purchase = result.purchase,
            cycle = cycle
        )

        null -> error("Missing payment-iap module.")
    }

    private suspend fun ResultCollector<*>.onResultEnqueueObservabilityEvents(paymentProvider: PaymentProvider?) {
        convertToObservabilityGiapStatus.getOrNull()?.let { converter ->
            onResultEnqueueObservability("getProductsDetails") {
                CheckoutGiapBillingProductQueryTotal(converter(this))
            }
            onResultEnqueueObservability("querySubscriptionPurchases") {
                CheckoutGiapBillingQuerySubscriptionsTotal(converter(this))
            }
            onResultEnqueueObservability("launchBillingFlow") {
                CheckoutGiapBillingLaunchBillingTotal(converter(this))
            }
        }

        onResultEnqueueObservability("validateSubscription") {
            getValidatePlanObservabilityData(paymentProvider)
        }
        onResultEnqueueObservability("createPaymentToken") {
            getCreatePaymentTokenObservabilityData(paymentProvider)
        }
    }

    sealed class ButtonState {
        object Idle : ButtonState()
        object Disabled : ButtonState()
        object Loading : ButtonState()
    }

    companion object {
        private const val RESULT_EVENT_BUFFER = 8
        val initialButtonState = ButtonState.Idle
    }
}

public sealed class ProtonPaymentEvent {
    public object Loading : ProtonPaymentEvent()

    public sealed class Error : ProtonPaymentEvent() {
        public object EmptyCustomerId : Error()
        public data class Generic(public val throwable: Throwable) : Error()
        public data class GiapUnredeemed(
            public val cycle: Int,
            public val googleProductId: ProductId,
            public val googlePurchase: GooglePurchase,
            public val originalCurrency: String,
            public val plan: DynamicPlan,
        ) : Error()

        public class SubscriptionManagedByOtherApp(
            public val userId: UserId,
            public val deeplink: String
        ) : Error()

        public object GoogleProductDetailsNotFound : Error()
        public object PurchaseNotFound : Error()
        public object RecoverableBillingError : Error()
        public object UnrecoverableBillingError : Error()
        public object UnsupportedPaymentProvider : Error()
        public object UserCancelled : Error()
    }

    public data class StartRegularBillingFlow(
        public val plan: DynamicPlan,
        public val cycle: Int,
        public val currency: String
    ) : ProtonPaymentEvent()

    public data class GiapSuccess(
        public val plan: DynamicPlan,
        public val purchase: GooglePurchase,
        public val cycle: Int
    ) : ProtonPaymentEvent()
}
