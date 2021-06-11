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
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentPlansBinding
import me.proton.core.plan.presentation.entity.PlanInput
import me.proton.core.plan.presentation.viewmodel.PlansViewModel
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class PlansFragment : ProtonFragment<FragmentPlansBinding>() {

    private val viewModel by viewModels<PlansViewModel>()

    private val input: PlanInput by lazy {
        requireArguments().get(ARG_INPUT) as PlanInput
    }

    private val upgrade: Boolean by lazy {
        input.user != null
    }

    override fun layoutId() = R.layout.fragment_plans

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            closeButton.onClick {
                finish()
            }
            input.user?.let {
                if (input.showCurrent)
                    plansTitle.visibility = View.GONE
                else
                    plansTitle.text = getString(R.string.plans_upgrade_plan)
            }
        }
        viewModel.availablePlansState.onEach {
            when (it) {
                is PlansViewModel.State.Error.Message -> onError(it.message)
                is PlansViewModel.State.Idle -> {
                }
                is PlansViewModel.State.Processing -> showLoading(true)
                is PlansViewModel.State.Success -> {
                    showLoading(false)
                    with(binding) {
                        plansView.selectPlanListener = { selectedPlan ->
                            parentFragmentManager.setFragmentResult(
                                KEY_PLAN_SELECTED, bundleOf(BUNDLE_KEY_PLAN to selectedPlan)
                            )
                            if (!upgrade) {
                                parentFragmentManager.popBackStackImmediate()
                            }
                        }
                        plansView.plans = it.plans

                        with(customizableFeaturesText) {
                            if (it.subscription != null && !it.subscription.subscriptionPlanSupportedFromCore
                            ) {
                                text = getString(R.string.plans_customizable_features_web)
                            }
                            movementMethod = LinkMovementMethod.getInstance()
                            visibility = View.VISIBLE
                        }
                    }
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
        viewModel.getCurrentPlanWithUpgradeOption(userId = input.user, input.showCurrent)
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun onError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.plans_fetching_general_error))
    }

    private fun finish() {
        parentFragmentManager.setFragmentResult(
            KEY_PLAN_SELECTED, bundleOf(BUNDLE_KEY_PLAN to null)
        )
        parentFragmentManager.popBackStackImmediate()
    }

    companion object {
        const val KEY_PLAN_SELECTED = "key.plan_selected"
        const val BUNDLE_KEY_PLAN = "bundle.plan"
        const val ARG_INPUT = "arg.plansInput"

        operator fun invoke(input: PlanInput) = PlansFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
