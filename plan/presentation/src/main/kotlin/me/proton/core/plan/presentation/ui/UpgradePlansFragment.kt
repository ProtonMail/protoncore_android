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
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentPlansUpgradeBinding
import me.proton.core.plan.presentation.entity.PlanCurrency
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.viewmodel.BasePlansViewModel
import me.proton.core.plan.presentation.viewmodel.UpgradePlansViewModel
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@AndroidEntryPoint
class UpgradePlansFragment : BasePlansFragment(R.layout.fragment_plans_upgrade) {

    @Inject lateinit var product: Product

    private val upgradePlanViewModel by viewModels<UpgradePlansViewModel>()
    private val binding by viewBinding(FragmentPlansUpgradeBinding::bind)

    private val input: PlanInput by lazy {
        requireArguments().get(ARG_INPUT) as PlanInput
    }

    private val userId: UserId? by lazy {
        input.user
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upgradePlanViewModel.register(this)
        activity?.addOnBackPressedCallback { setResult() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (upgradePlanViewModel.supportedPaidPlanNames.isNotEmpty()) {
            binding.apply {
                toolbar.setNavigationOnClickListener {
                    setResult()
                }
                toolbar.title = getString(R.string.plans_subscription)
                toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
                plansView.setProduct(product)
            }

            upgradePlanViewModel.subscribedPlansState.onEach {
                when (it) {
                    is UpgradePlansViewModel.SubscribedPlansState.Error ->
                        onError(it.error.getUserMessage(resources))
                    is UpgradePlansViewModel.SubscribedPlansState.Idle -> Unit
                    is UpgradePlansViewModel.SubscribedPlansState.Processing -> showLoading(true)
                    is UpgradePlansViewModel.SubscribedPlansState.Success.SubscribedPlans -> {
                        val subscribedPlans = it.subscribedPlans
                        binding.currentPlan.apply {
                            visibility = VISIBLE
                            cycle = PlanCycle.YEARLY
                            currency = PlanCurrency.CHF
                            planDetailsListItem = subscribedPlans[0]
                        }
                        Unit
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

            upgradePlanViewModel.availablePlansState.onEach {
                when (it) {
                    is BasePlansViewModel.PlanState.Error -> onError(it.error.getUserMessage(resources))
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
                                    upgradePlanViewModel.startBillingForPaidPlan(userId, selectedPlan, cycle)
                                }
                            }
                            plansTitle.visibility = if (it.plans.isNotEmpty()) VISIBLE else GONE
                            plansView.plans = it.plans
                        }
                    }
                    is BasePlansViewModel.PlanState.Success.PaidPlanPayment -> {
                        setResult(it.selectedPlan, it.billing)
                        upgradePlanViewModel.getCurrentSubscribedPlans(input.user!!)
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

            upgradePlanViewModel.getCurrentSubscribedPlans(input.user!!)
        } else {
            // means clients does not support any paid plans, so we close this and proceed directly to free plan signup
            setResult(SelectedPlan.free(getString(R.string.plans_free_name)))
        }
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) VISIBLE else View.GONE
    }

    private fun onError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.plans_fetching_general_error))
    }

    companion object {
        operator fun invoke(input: PlanInput) = UpgradePlansFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
