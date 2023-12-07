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

package me.proton.core.payment.presentation.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.get
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PaymentProvider.CardPayment
import me.proton.core.payment.domain.usecase.PaymentProvider.GoogleInAppPurchase
import me.proton.core.payment.domain.usecase.PaymentProvider.PayPal
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentButtonViewModel
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentButtonViewModel.ButtonState.Disabled
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentButtonViewModel.ButtonState.Idle
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentButtonViewModel.ButtonState.Loading
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.presentation.ui.view.ProtonProgressButton

/**
 * Button for one-click payments.
 *
 * Caveat: For now, only [PaymentProvider.GoogleInAppPurchase] is supported directly.
 * [PaymentProvider.CardPayment] will result in [ProtonPaymentEvent.StartRegularBillingFlow] event.
 * [PaymentProvider.PayPal] is not supported.
 *
 * If you are adding multiple buttons under a single activity/fragment/view,
 * for each button you must set a unique (but not random) [id][setId]
 * (i.e. the [id][getId] should be the same after activity is re-created).
 *
 * For other required parameters, see [ProtonPaymentContract].
 */
public class ProtonPaymentButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : ProtonProgressButton(context, attrs, defStyleAttr), ProtonPaymentContract {
    override var currency: String
        get() = requireNotNull(_currency) { "Missing currency." }
        set(value) {
            _currency = value
        }
    override var cycle: Int
        get() = requireNotNull(_cycle) { "Missing cycle." }
        set(value) {
            _cycle = value
        }
    override var paymentProvider: PaymentProvider?
        get() = _paymentProvider
        set(value) {
            _paymentProvider = value
        }
    override var plan: DynamicPlan
        get() = requireNotNull(_plan) { "Missing plan." }
        set(value) {
            _plan = value
        }
    override var userId: UserId? = null

    private var _currency: String? = null
        set(value) {
            field = value
            adjustText()
        }
    private var _cycle: Int? = null
        set(value) {
            field = value
            adjustText()
        }
    private var _paymentProvider: PaymentProvider? = null
        set(value) {
            field = value
            onPaymentProviderChanged()
        }
    private var _plan: DynamicPlan? = null
        set(value) {
            field = value
            adjustText()
        }
    private val buttonId = lazy { id }
    private var errorEventsObserverJob: Job? = null
    private var eventListener: ProtonPaymentEventListener? = null
    private var stateObserverJob: Job? = null
    private var state: ProtonPaymentButtonViewModel.ButtonState =
        ProtonPaymentButtonViewModel.initialButtonState
        set(value) {
            field = value
            onStateChanged()
        }

    private val lifecycleCoroutineScope: LifecycleCoroutineScope
        get() = findViewTreeLifecycleOwner()!!.lifecycle.coroutineScope

    @VisibleForTesting
    internal var testViewModel: ProtonPaymentButtonViewModel? = null

    /** A view model can be accessed only after [onAttachedToWindow].*/
    private val viewModel: ProtonPaymentButtonViewModel by lazy {
        testViewModel ?: ViewModelProvider(requireNotNull(findViewTreeViewModelStoreOwner()) {
            "Could not find ViewModelStoreOwner."
        }).get()
    }

    init {
        context.withStyledAttributes(
            set = attrs,
            attrs = R.styleable.ProtonPaymentButton,
            defStyleAttr = defStyleAttr
        ) { initializeAttributes() }

        setOnClickListener {
            viewModel.onPayClicked(
                buttonId = buttonId.value,
                currency = currency,
                cycle = cycle,
                paymentProvider = paymentProvider,
                plan = plan,
                userId = userId
            )
        }
    }

    override fun setId(id: Int) {
        if (buttonId.isInitialized()) {
            error("ID cannot be set after `buttonId` has been initialized.")
        }
        super.setId(id)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isInEditMode) return

        viewModel.onButtonAttached(buttonId.value)
        stateObserverJob?.cancel()
        stateObserverJob = viewModel.buttonStates(buttonId.value)
            .onEach { state = it }
            .launchIn(lifecycleCoroutineScope)

        errorEventsObserverJob?.cancel()
        errorEventsObserverJob = viewModel.paymentEvents(buttonId.value)
            .onEach { eventListener?.invoke(it) }
            .launchIn(lifecycleCoroutineScope)
    }

    override fun onDetachedFromWindow() {
        if (isInEditMode) return super.onDetachedFromWindow()

        errorEventsObserverJob?.cancel()
        stateObserverJob?.cancel()
        viewModel.onButtonDetached(buttonId.value)
        super.onDetachedFromWindow()
    }

    override fun setOnEventListener(listener: ProtonPaymentEventListener) {
        eventListener = listener
    }

    private fun TypedArray.initializeAttributes() {
        _paymentProvider = when (getInt(R.styleable.ProtonPaymentButton_paymentProvider, -1)) {
            0 -> CardPayment
            1 -> GoogleInAppPurchase
            2 -> PayPal
            else -> null
        }
    }

    // region State

    private fun onStateChanged() = when (state) {
        Idle -> onIdle()
        Disabled -> onDisabled()
        Loading -> onPaymentInProgress()
    }

    private fun onIdle() {
        isEnabled = true
        setIdle()
        adjustText()
    }

    private fun onDisabled() {
        isEnabled = false
        setIdle()
        adjustText()
    }

    private fun onPaymentInProgress() {
        isEnabled = false
        adjustText()
        setLoading()
    }

    // endregion

    // region Payment provider

    private fun onPaymentProviderChanged() {
        when (_paymentProvider) {
            CardPayment -> setCardPaymentUi()
            GoogleInAppPurchase -> setGoogleInAppPurchaseUi()
            PayPal -> error("PayPal is not supported")
            null -> setNoPaymentProviderUi()
        }
    }

    private fun setCardPaymentUi() {
        adjustText()
    }

    private fun setGoogleInAppPurchaseUi() {
        adjustText()
    }

    private fun setNoPaymentProviderUi() {
        adjustText()
    }

    // endregion

    // region UI

    private fun adjustText() {
        text = when  {
            state == Loading -> context.getString(R.string.payments_paying_in_process)
            _plan != null -> context.getString(R.string.payments_get_plan, plan.title)
            else -> null
        }
    }

    // endregion
}

/**
 * Input parameters:
 * - [currency] - The currency requested for the payment.
 * - [cycle] - The cycle (number of months) of the subscription to be purchased.
 * - [paymentProvider] - Optionally, the payment provider to use. If `null`, it will be determined automatically.
 * - [plan] The plan to be purchased.
 * - [userId] Optional user ID, that will be used to subscribe.
 * To get the result (after the user clicks on the button), call [setOnEventListener].
 */
public interface ProtonPaymentContract {
    public val currency: String
    public val cycle: Int
    public val paymentProvider: PaymentProvider?
    public val plan: DynamicPlan
    public val userId: UserId?

    public fun setOnEventListener(listener: ProtonPaymentEventListener)
}

public fun interface ProtonPaymentEventListener {
    public operator fun invoke(event: ProtonPaymentEvent)
}
