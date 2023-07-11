/*
 * Copyright (c) 2022 Proton Technologies AG
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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.country.domain.usecase.GetCountry
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.payment.presentation.ActivePaymentProvider
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.PlanShortDetails
import javax.inject.Inject

@HiltViewModel
public open class BillingViewModel @Inject constructor(
    private val activePaymentProvider: ActivePaymentProvider,
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
    private val _state = MutableStateFlow<State>(State.Idle)
    public val state: StateFlow<State> = _state.asStateFlow()

    private val _userInteractionState =
        MutableSharedFlow<UserInteractionState>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    public val userInteractionState: SharedFlow<UserInteractionState> = _userInteractionState.asSharedFlow()

    private var selectedPlan: PlanShortDetails? = null

    public sealed class UserInteractionState {
        public object Idle : UserInteractionState()
        public data class OnPay(val input: BillingInput) : UserInteractionState()
        public data class OnLoadingStateChange(val loading: Boolean) : UserInteractionState()
        public data class PlanValidated(val plan: PlanShortDetails) : UserInteractionState()
    }

    public sealed class State {
        public object Idle : State()
        public object Loading : State()
        public object PaymentProvidersEmpty : State()
        public data class PaymentProvidersSuccess(
            val activeProvider: PaymentProvider,
            val nextPaymentProviderTextResource: Int?
        ) : State()

        public sealed class PaymentProvidersError : State() {
            public data class Message(val error: String?) : PaymentProvidersError()
        }

        public sealed class PayButtonsState : State() {
            public object ProtonPayDisabled : PayButtonsState()
            public object GPayDisabled : PayButtonsState()
            public data class ProtonPayEnabled(val text: String) : PayButtonsState()
            public object GPayEnabled : PayButtonsState()
            public object Loading : PayButtonsState()
            public object Idle : PayButtonsState()
        }
    }

    init {
        viewModelScope.launch { observePaymentProviders() }
    }

    public fun onPay(input: BillingInput) {
        _state.tryEmit(State.PayButtonsState.Loading)
        _userInteractionState.tryEmit(UserInteractionState.OnPay(input))
    }

    public fun onLoadingStateChange(loading: Boolean) {
        _userInteractionState.tryEmit(UserInteractionState.OnLoadingStateChange(loading))
    }

    public fun setPlan(plan: PlanShortDetails) {
        selectedPlan = plan
        selectedPlan?.let {
            _userInteractionState.tryEmit(UserInteractionState.PlanValidated(it))
        }
    }

    public fun announcePlan() {
        selectedPlan?.let {
            _userInteractionState.tryEmit(UserInteractionState.PlanValidated(it))
        }
    }

    public fun switchNextPaymentProvider() {
        val paymentProvider = activePaymentProvider.switchNextPaymentProvider()
        if (paymentProvider == null) {
            _state.tryEmit(State.PaymentProvidersEmpty)
        } else {
            _state.tryEmit(
                State.PaymentProvidersSuccess(
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
        emit(State.Loading)
        val paymentProvider = activePaymentProvider.getActivePaymentProvider()
        if (paymentProvider == null) {
            emit(State.PaymentProvidersEmpty)
        } else {
            emit(State.PaymentProvidersSuccess(paymentProvider, activePaymentProvider.getNextPaymentProviderText()))
        }
    }.catch { error ->
        _state.tryEmit(State.PaymentProvidersError.Message(error.message))
    }.onEach { subscriptionState ->
        _state.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    public fun setPayButtonStateEnabled(text: String) {
        _state.tryEmit(State.PayButtonsState.ProtonPayEnabled(text))
    }

    public fun setGPayButtonState(enabled: Boolean) {
        if (!enabled) {
            _state.tryEmit(State.PayButtonsState.GPayDisabled)
        } else {
            _state.tryEmit(State.PayButtonsState.GPayEnabled)
        }
    }

    public fun setPayButtonsState(loading: Boolean) {
        if (loading) {
            _state.tryEmit(State.PayButtonsState.Loading)
        } else {
            _state.tryEmit(State.PayButtonsState.Idle)
        }
    }
}
