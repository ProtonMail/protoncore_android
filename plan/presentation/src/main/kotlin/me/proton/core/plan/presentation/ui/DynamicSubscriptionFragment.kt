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
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.DynamicPlanDecoration
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.FragmentDynamicSubscriptionBinding
import me.proton.core.plan.presentation.view.formatRenew
import me.proton.core.plan.presentation.view.toView
import me.proton.core.plan.presentation.viewmodel.DynamicSubscriptionViewModel
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.viewBinding

@AndroidEntryPoint
class DynamicSubscriptionFragment : ProtonFragment(R.layout.fragment_dynamic_subscription) {

    private val binding by viewBinding(FragmentDynamicSubscriptionBinding::bind)
    private val viewModel by viewModels<DynamicSubscriptionViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.state.onEach {
            when (it) {
                is DynamicSubscriptionViewModel.State.Loading -> onLoading()
                is DynamicSubscriptionViewModel.State.Error -> onError(it.error)
                is DynamicSubscriptionViewModel.State.UserNotExist -> onNoPrimaryUser()
                is DynamicSubscriptionViewModel.State.Success -> onSuccess(it.dynamicSubscription)
            }
        }.launchIn(lifecycleScope)
    }

    private fun onLoading() {
        showLoading(true)
    }

    private fun onError(error: Throwable?) = with(binding) {
        showLoading(false)
        val message = error?.getUserMessage(resources)
        root.errorSnack(message ?: getString(R.string.presentation_error_general))
    }

    private fun onNoPrimaryUser() {
        showLoading(true)
    }

    private fun onSuccess(dynamicSubscription: DynamicSubscription) {
        showLoading(false)
        showSubscription(dynamicSubscription)
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        progress.visibility = if (loading) VISIBLE else GONE
    }

    private fun showSubscription(dynamicSubscription: DynamicSubscription) = with(binding.dynamicPlan) {
        title = dynamicSubscription.title
        description = dynamicSubscription.description
        starred = dynamicSubscription.decorations.filterIsInstance<DynamicPlanDecoration.Star>().isNotEmpty()
        val price = dynamicSubscription.renewAmount?.takeIf { it > 0 } ?: dynamicSubscription.amount
        priceText = price.toDouble().formatCentsPriceDefaultLocale(dynamicSubscription.currency)
        priceCycle = dynamicSubscription.cycleDescription
        renewalTextIsVisible = dynamicSubscription.renewAmount != null && dynamicSubscription.renew
        renewalText = formatRenew(context, dynamicSubscription.renew, dynamicSubscription.periodEnd)

        isCollapsable = false
        entitlements.removeAllViews()
        dynamicSubscription.entitlements.forEach { entitlements.addView(it.toView(context)) }
    }
}
