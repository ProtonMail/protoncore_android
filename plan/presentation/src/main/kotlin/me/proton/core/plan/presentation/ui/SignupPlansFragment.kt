/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.plan.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.metrics.CheckoutScreenViewTotal
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentPlansBinding
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.viewmodel.BasePlansViewModel
import me.proton.core.plan.presentation.viewmodel.SignupPlansViewModel
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class SignupPlansFragment : BasePlansFragment(R.layout.fragment_plans) {

    private val signupPlansViewModel by viewModels<SignupPlansViewModel>()

    private val binding by viewBinding(FragmentPlansBinding::bind)

    private val input: PlanInput by lazy {
        requireArguments().get(ARG_INPUT) as PlanInput
    }

    private val userId: UserId? by lazy {
        input.user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signupPlansViewModel.register(this)
        addOnBackPressedCallback { finish() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (signupPlansViewModel.supportPaidPlans) {
            binding.toolbar.setNavigationOnClickListener {
                setResult()
            }
            signupPlansViewModel.availablePlansState
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .distinctUntilChanged()
                .onEach {
                    when (it) {
                        is BasePlansViewModel.PlanState.Error -> onError(it)
                        is BasePlansViewModel.PlanState.Idle -> Unit
                        is BasePlansViewModel.PlanState.Processing -> showLoading(true)
                        is BasePlansViewModel.PlanState.Success.Plans -> {
                            showLoading(false)
                            onSuccess(it.plans)
                        }
                        is BasePlansViewModel.PlanState.Success.PaidPlanPayment ->
                            setResult(it.selectedPlan, it.billing)
                    }.exhaustive
                }.launchIn(viewLifecycleOwner.lifecycleScope)

            signupPlansViewModel.getAllPlansForSignup()

            launchOnScreenView {
                signupPlansViewModel.onScreenView(CheckoutScreenViewTotal.ScreenId.planSelection)
            }
        } else {
            onFreeSelected()
        }
    }

    private fun onFreeSelected() {
        // means clients does not support any paid plans, so we close this and proceed directly to free plan signup
        parentFragmentManager.removePlansSignup()
        setResult(SelectedPlan.free(resources))
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progressParent.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun onSuccess(plans: List<PlanDetailsItem>) {
        with(binding) {
            planSelection.selectPlanListener = { selectedPlan ->
                if (selectedPlan.free) {
                    // proceed with result return
                    setResult(selectedPlan)
                } else {
                    signupPlansViewModel.startBillingForPaidPlan(
                        userId,
                        selectedPlan,
                        selectedPlan.cycle.toSubscriptionCycle()
                    )
                }
            }
            if (plans.isNotEmpty()) {
                planSelection.plans = plans
            } else {
                onFreeSelected()
            }
        }
    }

    private fun onError(errorState: BasePlansViewModel.PlanState.Error) {
        when (errorState) {
            is BasePlansViewModel.PlanState.Error.Exception -> {
                onError(errorState.error.getUserMessage(resources))
                binding.connectivityIssueView.visibility = View.VISIBLE
            }
            is BasePlansViewModel.PlanState.Error.Message -> onError(errorState.message)
        }.exhaustive
    }

    private fun onError(@StringRes message: Int?) {
        if (message != null) {
            onError(getString(message))
        } else {
            binding.root.errorSnack(message = getString(R.string.plans_fetching_general_error))
        }
    }

    private fun onError(message: String?) = with(binding) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.plans_fetching_general_error))
    }

    private fun finish() {
        setResult()
    }

    companion object {
        operator fun invoke(input: PlanInput) = SignupPlansFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
