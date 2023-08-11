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

package me.proton.core.plan.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.presentation.viewmodel.ProtonViewModel
import org.jetbrains.annotations.VisibleForTesting
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
internal class DynamicPlanSelectionViewModel @Inject constructor(
    override val manager: ObservabilityManager
) : ProtonViewModel(), ObservabilityContext {

    sealed class State {
        data class Idle(val currencies: List<String>) : State()
        data class Free(val selectedPlan: SelectedPlan) : State()
        data class Billing(val selectedPlan: SelectedPlan) : State()
        data class Billed(val selectedPlan: SelectedPlan, val billingResult: BillingResult) : State()
    }

    sealed class Action {
        data class SelectPlan(val selectedPlan: SelectedPlan) : Action()
        data class SetBillingResult(val result: BillingResult) : Action()
        object SetBillingCanceled : Action()
    }

    @VisibleForTesting
    internal val localCurrency = Currency.getInstance(Locale.getDefault()).currencyCode
    @VisibleForTesting
    internal val defaultCurrency = availableCurrencies.firstOrNull { it == localCurrency } ?: fallbackCurrency
    // ISO 4217 3-letter codes.
    private val currencies = listOf(defaultCurrency) + (availableCurrencies - defaultCurrency)

    private val mutableSelectedPlan = MutableStateFlow<SelectedPlan?>(null)
    private val mutableState = MutableStateFlow<State>(State.Idle(currencies))

    val state = mutableState.asStateFlow()

    fun perform(action: Action) = when (action) {
        is Action.SelectPlan -> onSelectPlan(action.selectedPlan)
        is Action.SetBillingResult -> onSetPaymentResult(action.result)
        is Action.SetBillingCanceled -> onSetBillingCanceled()
    }

    private fun onSelectPlan(selectedPlan: SelectedPlan) = viewModelScope.launch {
        mutableSelectedPlan.value = selectedPlan
        when {
            selectedPlan.free -> mutableState.emit(State.Free(selectedPlan))
            else -> mutableState.emit(State.Billing(selectedPlan))
        }
    }

    private fun onSetPaymentResult(result: BillingResult) = viewModelScope.launch {
        when {
            result.paySuccess -> mutableState.emit(State.Billed(requireNotNull(mutableSelectedPlan.value), result))
            else -> onSetBillingCanceled()
        }
    }

    private fun onSetBillingCanceled() = viewModelScope.launch {
        mutableState.emit(State.Idle(currencies))
    }

    companion object {
        // ISO 4217 3-letter codes.
        private const val fallbackCurrency = "USD"
        val availableCurrencies = listOf("CHF", "EUR", fallbackCurrency)
    }
}
