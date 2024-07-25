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

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.allViews
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State.RESUMED
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicUpgradePlanBinding
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.usecase.StorageUsageState
import me.proton.core.plan.presentation.view.DynamicPlanCardView
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.Action.Load
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.Action.SetPlanList
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.Action.SetUser
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.State
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class DynamicUpgradePlanFragment : ProtonFragment(R.layout.fragment_dynamic_upgrade_plan) {

    private val binding by viewBinding(FragmentDynamicUpgradePlanBinding::bind)
    private val viewModel by viewModels<DynamicUpgradePlanViewModel>()

    private val subscriptionFragment by lazy { binding.subscription.getFragment<DynamicSubscriptionFragment>() }
    private val planSelectionFragment by lazy { binding.planSelection.getFragment<DynamicPlanSelectionFragment>() }

    private var onBackClicked: (() -> Unit)? = null
    private var onPlanBilled: ((SelectedPlan, BillingResult) -> Unit)? = null

    private val unredeemedPurchaseLauncher =
        registerForActivityResult(StartUnredeemedPurchase) { result ->
            if (result?.redeemed == true) {
                viewModel.perform(Load)
                subscriptionFragment.reload()
            }
        }

    fun setUser(user: DynamicUser) {
        planSelectionFragment.setUser(user)
        subscriptionFragment.setUser(user)
        viewModel.perform(SetUser(user))
    }

    fun setShowSubscription(isVisible: Boolean) = with(binding) {
        toolbar.title = when (isVisible) {
            true -> getString(R.string.plans_subscription)
            false -> getString(R.string.plans_upgrade_your_plan)
        }
        subscription.isVisible = isVisible
        title.isVisible = isVisible
        if (!isVisible) {
            upgradeLayout.isVisible = true
        }
    }

    fun setOnPlanBilled(onPlanBilled: (SelectedPlan, BillingResult) -> Unit) {
        this.onPlanBilled = onPlanBilled
    }

    fun setOnBackClicked(onBackClicked: () -> Unit) {
        this.onBackClicked = onBackClicked
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { onBackClicked?.invoke() }
        binding.retry.onClick { viewModel.perform(Load) }

        planSelectionFragment.setOnPlanFree { throw IllegalStateException("Cannot upgrade to Free plan.") }
        planSelectionFragment.setOnPlanBilled { plan, result -> onPlanBilled?.invoke(plan, result) }
        launchInViewLifecycleScope(RESUMED) {
            planSelectionFragment.onError().filterNotNull().collect { onError(it, showChildLayout = true) }
        }
        launchInViewLifecycleScope(RESUMED) {
            planSelectionFragment.getPlanList().filterNotNull().collect { viewModel.perform(SetPlanList(it)) }
        }

        viewModel.state.onEach {
            when (it) {
                is State.Loading -> onLoading()
                is State.Error -> onError(it.error, showChildLayout = false)
                is State.UnredeemedPurchase -> onUnredeemedPurchase()
                is State.UpgradeAvailable -> onUpgradeAvailable(it.storageUsageState)
                is State.UpgradeNotAvailable -> onUpgradeNotAvailable(it.storageUsageState)
            }
        }.launchInViewLifecycleScope()

        launchOnScreenView { viewModel.onScreenView() }
    }

    private fun onLoading() {
        showLoading(true)
    }

    private fun onUnredeemedPurchase() {
        unredeemedPurchaseLauncher.launch(Unit)
        showLoading(false)
    }

    private fun onUpgradeAvailable(storageUsageState: StorageUsageState?) = with(binding) {
        showLoading(false)
        updateStorageUsageState(storageUsageState)
        storageFullView.onUpgradeAvailable { scrollToFirstAvailablePlanForUpgrade() }
        upgradeLayout.isVisible = true
    }

    private fun onUpgradeNotAvailable(storageUsageState: StorageUsageState?) = with(binding) {
        showLoading(false)
        updateStorageUsageState(storageUsageState)
        storageFullView.onUpgradeUnavailable()
        upgradeLayout.isVisible = subscription.isGone or false
    }

    private fun onError(error: Throwable?, showChildLayout: Boolean) = with(binding) {
        showLoading(false)
        val message = error?.getUserMessage(resources)
        showError(message ?: getString(R.string.presentation_error_general), showChildLayout)
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.isVisible = loading
        planSelection.isVisible = !loading
        errorLayout.isVisible = false
    }

    private fun showError(message: String, showChildLayout: Boolean) = with(binding) {
        errorLayout.isVisible = !showChildLayout
        error.text = message
        upgradeLayout.isVisible = showChildLayout
    }

    private fun updateStorageUsageState(state: StorageUsageState?) {
        binding.storageFullView.isVisible = state != null
        when (state) {
            null -> Unit
            is StorageUsageState.Full -> binding.storageFullView.setStorageFull(state.product)
            is StorageUsageState.NearlyFull -> binding.storageFullView.setStorageNearlyFull(state.product)
        }
    }

    private fun scrollToFirstAvailablePlanForUpgrade() {
        val firstPlanView = binding.root
            .findViewById<ViewGroup>(R.id.plan_selection)
            .allViews
            .filterIsInstance<DynamicPlanCardView>()
            .firstOrNull() ?: return
        firstPlanView.isCollapsed = false

        // Delay scrolling, until the `firstPlanView` is expanded:
        binding.root.post {
            // Get the position of `firstPlanView` relative to its parent:
            val planViewRect = Rect(
                firstPlanView.left,
                firstPlanView.top,
                firstPlanView.right,
                firstPlanView.bottom
            )

            // Adjust the `planViewRect` to the coordinates of `scrollContent`:
            val planViewParent = firstPlanView.parent as? View ?: return@post
            binding.scrollContent.offsetDescendantRectToMyCoords(planViewParent, planViewRect)

            binding.scrollContent.smoothScrollTo(0, planViewRect.top)
        }
    }
}
