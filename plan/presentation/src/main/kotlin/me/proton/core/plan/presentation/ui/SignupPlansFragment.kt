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
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentPlansBinding
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.viewmodel.BasePlansViewModel
import me.proton.core.plan.presentation.viewmodel.SignupPlansViewModel
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
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
        if (signupPlansViewModel.supportedPaidPlanNames.isNotEmpty()) {
            binding.apply {
                toolbar.setNavigationOnClickListener {
                    setResult()
                }
                input.user?.let {
                    if (input.showCurrent) {
                        plansTitle.visibility = View.GONE
                        toolbar.title = getString(R.string.plans_subscription)
                    } else {
                        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
                        plansTitle.text = getString(R.string.plans_upgrade_plan)
                    }
                }
            }
            signupPlansViewModel.availablePlansState.onEach {
                when (it) {
                    is BasePlansViewModel.PlanState.Error.Message -> onError(it.message)
                    is BasePlansViewModel.PlanState.Idle -> Unit
                    is BasePlansViewModel.PlanState.Processing -> showLoading(true)
                    is BasePlansViewModel.PlanState.Success.Plans -> {
                        showLoading(false)
                        with(binding) {
                            plansView.selectPlanListener = { selectedPlan ->
                                if (selectedPlan.free) {
                                    // proceed with result return
                                    setResult(selectedPlan)
                                } else {
                                    val cycle = when (selectedPlan.cycle) {
                                        PlanCycle.MONTHLY -> SubscriptionCycle.MONTHLY
                                        PlanCycle.YEARLY -> SubscriptionCycle.YEARLY
                                        PlanCycle.TWO_YEARS -> SubscriptionCycle.TWO_YEARS
                                    }.exhaustive
                                    signupPlansViewModel.startBillingForPaidPlan(userId, selectedPlan, cycle)
                                }
                            }
                            plansView.plans = it.plans
                        }
                    }
                    is BasePlansViewModel.PlanState.Success.PaidPlanPayment -> setResult(it.selectedPlan, it.billing)
                }.exhaustive
            }.launchIn(lifecycleScope)

            signupPlansViewModel.getAllPlansForSignup()
        } else {
            // means clients does not support any paid plans, so we close this and proceed directly to free plan signup
            parentFragmentManager.removePlansSignup()
            setResult(SelectedPlan.free(getString(R.string.plans_free_name)))
        }
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun onError(message: String?) {
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
