/*
 * Copyright (c) 2023 Proton Technologies AG
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

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicPlanSelectionBinding
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.State
import me.proton.core.plan.presentation.viewmodel.DynamicUser
import me.proton.core.plan.presentation.viewmodel.filterByCycle
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.onItemSelected
import me.proton.core.presentation.utils.viewBinding
import javax.inject.Inject

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class DynamicPlanSelectionFragment : ProtonFragment(R.layout.fragment_dynamic_plan_selection) {

    @Inject
    lateinit var paymentsOrchestrator: PaymentsOrchestrator

    private val binding by viewBinding(FragmentDynamicPlanSelectionBinding::bind)
    private val viewModel by viewModels<DynamicPlanSelectionViewModel>()

    private val planList by lazy { binding.plans.getFragment<DynamicPlanListFragment>() }
    private val currencySpinner by lazy { binding.currencySpinner.apply { adapter = currencyAdapter } }
    private val currencyAdapter by lazy { ArrayAdapter<String>(requireContext(), R.layout.plan_spinner_item) }

    private var onPlanBilled: ((SelectedPlan, BillingResult) -> Unit)? = null
    private var onPlanFree: ((SelectedPlan) -> Unit)? = null

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
                is State.Idle -> onIdle(it.currencies)
                is State.Free -> onFree(it.selectedPlan)
                is State.Billing -> onBilling(it.selectedPlan)
                is State.Billed -> onBilled(it.selectedPlan, it.billingResult)
            }
        }.launchInViewLifecycleScope()

        planList.setOnPlanList { onPlanList(it) }
        planList.setOnPlanSelected { viewModel.perform(Action.SelectPlan(it)) }

        paymentsOrchestrator.onPaymentResult { result ->
            when (result) {
                null -> viewModel.perform(Action.SetBillingCanceled)
                else -> viewModel.perform(Action.SetBillingResult(result))
            }
        }
    }

    private fun onPlanList(plans: List<DynamicPlan>) {
        val isEmpty = plans.isEmpty()
        binding.listEmpty.isVisible = isEmpty
        binding.listInfo.isVisible = !isEmpty
        currencySpinner.isVisible = !isEmpty
    }

    private fun onIdle(currencies: List<String>) {
        if (currencyAdapter.isEmpty) {
            currencyAdapter.addAll(currencies)
            currencySpinner.setSelection(0)
            currencySpinner.onItemSelected { planList.setCurrency(requireNotNull(currencyAdapter.getItem(it))) }
        }
    }

    private fun onFree(selectedPlan: SelectedPlan) {
        onPlanFree?.invoke(selectedPlan)
    }

    private fun onBilling(selectedPlan: SelectedPlan) {
        paymentsOrchestrator.startBillingWorkFlow(
            userId = planList.getUser().userId,
            selectedPlan = PlanShortDetails(
                name = selectedPlan.planName,
                displayName = selectedPlan.planDisplayName,
                subscriptionCycle = SubscriptionCycle.YEARLY, // Only support 12 months.
                currency = selectedPlan.currency.toSubscriptionCurrency(),
                services = selectedPlan.services,
                type = selectedPlan.type,
                vendors = selectedPlan.vendorNames.filterByCycle(PlanCycle.YEARLY)
            ),
            codes = null
        )
    }

    private fun onBilled(selectedPlan: SelectedPlan, result: BillingResult) {
        onPlanBilled?.invoke(selectedPlan, result)
    }
}
