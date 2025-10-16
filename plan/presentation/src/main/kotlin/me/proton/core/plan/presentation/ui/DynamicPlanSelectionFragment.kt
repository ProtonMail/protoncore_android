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

package me.proton.core.plan.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State.RESUMED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentVendorDetails
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.payment.presentation.viewmodel.ProtonPaymentEvent
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.entity.isFree
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicPlanSelectionBinding
import me.proton.core.plan.presentation.entity.DynamicPlanFilters
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanVendorDetails
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.getSelectedPlan
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.State
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.onItemSelected
import me.proton.core.presentation.utils.viewBinding
import javax.inject.Inject
import kotlin.math.max

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class DynamicPlanSelectionFragment : ProtonFragment(R.layout.fragment_dynamic_plan_selection) {

    @Inject
    lateinit var paymentsOrchestrator: PaymentsOrchestrator
    @Inject
    lateinit var plansOrchestrator: PlansOrchestrator

    private val binding by viewBinding(FragmentDynamicPlanSelectionBinding::bind)
    private val viewModel by viewModels<DynamicPlanSelectionViewModel>()

    private val planList by lazy { binding.plans.getFragment<DynamicPlanListFragment>() }
    private val cycleSpinner by lazy { binding.cycleSpinner.apply { adapter = cycleAdapter } }
    private val cycleAdapter by lazy { CycleAdapter(requireContext(), R.layout.plan_spinner_item) }
    private val currencySpinner by lazy { binding.currencySpinner.apply { adapter = currencyAdapter } }
    private val currencyAdapter by lazy { ArrayAdapter<String>(requireContext(), R.layout.plan_spinner_item) }

    private var onPlanBilled: ((SelectedPlan, BillingResult) -> Unit)? = null
    private var onPlanFree: ((SelectedPlan) -> Unit)? = null

    private var planFilters = MutableStateFlow(DynamicPlanFilters())

    private val allFree: Boolean
        get() = planList.getPlanList().value?.all { it.isFree() } ?: false

    private val hasPlans: Boolean
        get() = planList.getPlanList().value?.isNotEmpty() ?: false

    private val hasPlanFiltersCycles: Boolean
        get() = planFilters.value.cycles.count() > 1

    private val hasPlanFiltersCurrencies: Boolean
        get() = planFilters.value.currencies.count() > 1

    private fun setCycle(index: Int) {
        planList.setCycle(requireNotNull(cycleAdapter.getCycle(index)))
    }

    private fun setCurrency(index: Int) {
        planList.setCurrency(requireNotNull(currencyAdapter.getItem(index)))
    }

    fun onError(): Flow<Throwable?> = planList.onError()

    fun getPlanList(): StateFlow<List<DynamicPlan>?> = planList.getPlanList()

    fun setUser(user: DynamicUser) {
        planList.setUser(user)
        viewModel.perform(Action.SetUser(user))
    }

    fun setOnPlanBilled(onPlanBilled: (SelectedPlan, BillingResult) -> Unit) {
        this.onPlanBilled = onPlanBilled
    }

    fun setOnPlanFree(onPlanFree: (SelectedPlan) -> Unit) {
        this.onPlanFree = onPlanFree
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentsOrchestrator.register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.onEach {
            when (it) {
                is State.Loading -> Unit
                is State.Idle -> onIdle(it.planFilters)
                is State.Free -> onFree(it.selectedPlan)
                is State.Billing -> onBilling(it.selectedPlan)
                is State.Billed -> onBilled(it.selectedPlan, it.billingResult)
            }
        }.launchInViewLifecycleScope()

        planFilters.onEach { onPlanFilters() }.launchInViewLifecycleScope()

        planList.setOnPlanSelected { viewModel.perform(Action.SelectPlan(it)) }
        planList.setOnProtonPaymentResultListener(this::onProtonPaymentResult)
        launchInViewLifecycleScope(RESUMED) {
            planList.getPlanList().filterNotNull().collect { onPlanList() }
        }

        paymentsOrchestrator.onPaymentResult { result ->
            when (result) {
                null -> viewModel.perform(Action.SetBillingCanceled)
                else -> viewModel.perform(Action.SetBillingResult(result))
            }
        }
    }

    private fun onIdle(planFilters: DynamicPlanFilters) {
        this.planFilters.value = planFilters
    }

    private fun onPlanList() {
        binding.listEmpty.isVisible = hasPlans.not()
        setupCycleSpinnerVisibility()
        setupCurrencySpinnerVisibility()
    }

    private fun onPlanFilters() {
        setupCycleSpinner()
        setupCurrencySpinner()
    }

    private fun setupCycleSpinner() {
        val filters = planFilters.value
        cycleAdapter.clear()
        cycleAdapter.addAll(filters.cycles)
        if (filters.cycles.isNotEmpty()) {
            val indexOfCycle = max(0, filters.cycles.indexOf(filters.defaultCycle))
            cycleSpinner.setSelection(indexOfCycle)
            setCycle(indexOfCycle)
        }
        cycleSpinner.onItemSelected { setCycle(it) }
        setupCycleSpinnerVisibility()
    }

    private fun setupCycleSpinnerVisibility() {
        cycleSpinner.isVisible = hasPlans && hasPlanFiltersCycles && !allFree
    }

    private fun setupCurrencySpinner() {
        val filters = planFilters.value
        currencyAdapter.clear()
        currencyAdapter.addAll(filters.currencies)
        if (filters.currencies.isNotEmpty()) {
            currencySpinner.setSelection(0)
            setCurrency(0)
        }
        currencySpinner.onItemSelected { setCurrency(it) }
        setupCurrencySpinnerVisibility()
    }

    private fun setupCurrencySpinnerVisibility() {
        currencySpinner.isVisible = hasPlans && hasPlanFiltersCurrencies && !allFree
    }

    private fun onFree(selectedPlan: SelectedPlan) {
        onPlanFree?.invoke(selectedPlan)
        viewModel.perform(Action.PlanSelectionFinished)
    }

    private fun onBilling(selectedPlan: SelectedPlan) {
        paymentsOrchestrator.startBillingWorkFlow(
            userId = planList.getUser().value.userId,
            selectedPlan = PlanShortDetails(
                name = selectedPlan.planName,
                displayName = selectedPlan.planDisplayName,
                subscriptionCycle = selectedPlan.cycle.toSubscriptionCycle(),
                currency = selectedPlan.currency.toSubscriptionCurrency(),
                services = selectedPlan.services,
                type = selectedPlan.type,
                vendors = selectedPlan.vendorNames.filterByCycle(selectedPlan.cycle)
            ),
            codes = null
        )
    }

    private fun onBilled(selectedPlan: SelectedPlan, result: BillingResult) {
        onPlanBilled?.invoke(selectedPlan, result)
        viewModel.perform(Action.PlanSelectionFinished)
    }

    private fun onProtonPaymentResult(event: ProtonPaymentEvent) {
        when (event) {
            is ProtonPaymentEvent.Loading -> Unit
            is ProtonPaymentEvent.Error.GiapUnredeemed -> onGiapUnredeemed(event)
            is ProtonPaymentEvent.Error.UserCancelled -> viewModel.perform(Action.SetBillingCanceled)
            is ProtonPaymentEvent.Error.GoogleProductDetailsNotFound -> view?.errorSnack(R.string.payments_error_google_prices)
            is ProtonPaymentEvent.Error.SubscriptionManagedByOtherApp ->
                plansOrchestrator.startUpgradeExternalWorkflow(event.userId, event.deeplink)
            is ProtonPaymentEvent.Error -> view?.errorSnack(R.string.payments_general_error)
            is ProtonPaymentEvent.GiapSuccess -> onGiapSuccess(event)
            is ProtonPaymentEvent.StartRegularBillingFlow -> startRegularBillingFlow(
                event.plan,
                event.cycle,
                event.currency
            )
        }
    }

    private fun onGiapSuccess(event: ProtonPaymentEvent.GiapSuccess) {
        viewModel.perform(
            Action.SetGiapBillingResult(
                selectedPlan = event.plan.getSelectedPlan(
                    resources,
                    event.cycle,
                    Currency.CHF.name
                ),
                result = BillingResult(
                    paySuccess = true,
                    token = null,
                    subscriptionCreated = false,
                    amount = 0,
                    currency = Currency.CHF,
                    cycle = SubscriptionCycle.map[event.cycle] ?: SubscriptionCycle.OTHER,
                    subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
                )
            )
        )
    }

    private fun onGiapUnredeemed(event: ProtonPaymentEvent.Error.GiapUnredeemed) {
        startRegularBillingFlow(
            event.plan,
            event.cycle,
            event.originalCurrency
        )
    }

    private fun startRegularBillingFlow(
        plan: DynamicPlan,
        cycle: Int,
        currency: String
    ) {
        val selectedPlan =
            plan.getSelectedPlan(resources = resources, cycle = cycle, currency = currency)
        viewModel.perform(Action.SelectPlan(selectedPlan))
    }
}

@VisibleForTesting
fun Map<AppStore, PlanVendorDetails>.filterByCycle(
    planCycle: PlanCycle
): Map<AppStore, PaymentVendorDetails> {
    return mapNotNull { (appVendor, details: PlanVendorDetails) ->
        details.names[planCycle]?.let { vendorPlanName ->
            appVendor to PaymentVendorDetails(customerId = details.customerId, vendorPlanName)
        }
    }.toMap()
}
