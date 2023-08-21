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

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicSelectPlanBinding
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.getSelectedPlan
import me.proton.core.plan.presentation.viewmodel.DynamicSelectPlanViewModel
import me.proton.core.plan.presentation.viewmodel.DynamicSelectPlanViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicSelectPlanViewModel.State
import me.proton.core.plan.presentation.viewmodel.DynamicUser
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding

/**
 * Fragment for selecting a plan during signup, when no user is available.
 * The result from this fragment is exposed via [BasePlansFragment.setResult].
 */
@AndroidEntryPoint
class DynamicSelectPlanFragment : BasePlansFragment(R.layout.fragment_dynamic_select_plan) {
    private val binding by viewBinding(FragmentDynamicSelectPlanBinding::bind)
    private val planSelection by lazy { binding.planSelection.getFragment<DynamicPlanSelectionFragment>() }
    private val viewModel by viewModels<DynamicSelectPlanViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.retry.onClick { viewModel.perform(Action.Load) }
        binding.toolbar.setNavigationOnClickListener { onBack() }
        addOnBackPressedCallback { onBack() }

        planSelection.setOnPlanFree { viewModel.perform(Action.SelectFreePlan(it)) }
        planSelection.setOnPlanBilled { plan, result ->
            viewModel.perform(Action.SelectPaidPlan(plan, result))
        }

        viewModel.state.onEach(this::handleState).launchIn(viewLifecycleOwner.lifecycleScope)
        launchOnScreenView { viewModel.onScreenView() }
    }

    override fun onResume() {
        super.onResume()
        planSelection.setUser(DynamicUser.None)
    }

    private fun handleState(state: State) = when (state) {
        is State.Idle -> onIdle()
        is State.Loading -> onLoading()
        is State.FreePlanOnly -> onFreePlanOnly(state.plan)
        is State.FreePlanSelected -> onFreePlanSelected(state.plan)
        is State.PaidPlanSelected -> onPaidPlanSelected(state.plan, state.result)
        is State.Error -> onError(state.error)
    }

    private fun onBack() {
        setResult()
    }

    private fun onIdle() {
        showLoading(false)
    }

    private fun onLoading() {
        showLoading(true)
    }

    private fun onFreePlanOnly(plan: DynamicPlan) {
        val selectedPlan = plan.getSelectedPlan(
            resources,
            cycle = PlanCycle.FREE.cycleDurationMonths,
            currency = null
        )
        onFreePlanSelected(selectedPlan)
    }

    private fun onFreePlanSelected(plan: SelectedPlan) {
        setResult(plan)
    }

    private fun onPaidPlanSelected(plan: SelectedPlan, result: BillingResult) {
        setResult(plan, result)
    }

    private fun onError(error: Throwable?) = with(binding) {
        showLoading(false)
        val message = error?.getUserMessage(resources)
        showError(message ?: getString(R.string.presentation_error_general))
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.isVisible = loading
        planSelection.isVisible = !loading
        errorLayout.isVisible = false
    }

    private fun showError(message: String) = with(binding) {
        errorLayout.isVisible = true
        error.text = message
        planSelection.isVisible = false
    }
}
