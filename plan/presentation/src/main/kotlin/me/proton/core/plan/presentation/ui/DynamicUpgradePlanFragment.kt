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

package me.proton.core.plan.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicUpgradePlanBinding
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.State
import me.proton.core.plan.presentation.viewmodel.DynamicUser
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class DynamicUpgradePlanFragment : ProtonFragment(R.layout.fragment_dynamic_upgrade_plan) {

    private val binding by viewBinding(FragmentDynamicUpgradePlanBinding::bind)
    private val viewModel by viewModels<DynamicUpgradePlanViewModel>()

    private val subscription by lazy { binding.subscription.getFragment<DynamicSubscriptionFragment>() }
    private val planSelection by lazy { binding.planSelection.getFragment<DynamicPlanSelectionFragment>() }

    private var onBackClicked: (() -> Unit)? = null
    private var onPlanBilled: ((SelectedPlan, BillingResult) -> Unit)? = null

    private val unredeemedPurchaseLauncher = registerForActivityResult(StartUnredeemedPurchase()) { result ->
        if (result?.redeemed == true) {
            viewModel.perform(Action.Load)
            subscription.reload()
        }
    }

    fun setUser(user: DynamicUser) {
        planSelection.setUser(user)
        subscription.setUser(user)
        viewModel.perform(Action.SetUser(user))
    }

    fun setShowSubscription(isVisible: Boolean) = with(binding) {
        subscription.isVisible = isVisible
        toolbar.title = when (isVisible) {
            true -> getString(R.string.plans_subscription)
            false -> getString(R.string.plans_upgrade_your_plan)
        }
        title.isVisible = isVisible
    }

    fun setOnPlanBilled(onPlanBilled: (SelectedPlan, BillingResult) -> Unit) {
        this.onPlanBilled = onPlanBilled
    }

    fun setOnBackClicked(onBackClicked: () -> Unit) {
        this.onBackClicked = onBackClicked
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.onEach {
            when (it) {
                is State.Idle -> onIdle()
                is State.Loading -> onLoading()
                is State.Error -> onError(it.error)
                is State.UnredeemedPurchase -> onUnredeemedPurchase()
                is State.UpgradeNotAvailable -> onUpgradeNotAvailable()
            }
        }.launchInViewLifecycleScope()

        binding.toolbar.setNavigationOnClickListener { onBackClicked?.invoke() }
        binding.retry.onClick { viewModel.perform(Action.Load) }

        planSelection.setOnPlanFree { throw IllegalStateException("Cannot upgrade to Free plan.") }
        planSelection.setOnPlanBilled { plan, result -> onPlanBilled?.invoke(plan, result) }
    }

    private fun onIdle() {
        showLoading(false)
    }

    private fun onLoading() {
        showLoading(true)
    }

    private fun onUnredeemedPurchase() {
        showLoading(false)
        unredeemedPurchaseLauncher.launch(Unit)
    }

    private fun onUpgradeNotAvailable() {
        showLoading(false)
        binding.upgradeLayout.isVisible = false
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
