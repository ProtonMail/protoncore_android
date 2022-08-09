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

package me.proton.core.paymentiap.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.paymentcommon.presentation.entity.PlanShortDetails
import me.proton.core.paymentcommon.presentation.viewmodel.BillingViewModel
import me.proton.core.paymentiap.presentation.R
import me.proton.core.paymentiap.presentation.databinding.FragmentBillingIapBinding
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive

/**
 * Fragment that handles Billing In App Purchases from Google.
 */
@AndroidEntryPoint
public class BillingIAPFragment : ProtonFragment(R.layout.fragment_billing_iap) {

    private val viewModel: BillingViewModel by viewModels({ requireActivity() })
    private val binding by viewBinding(FragmentBillingIapBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userInteractionState
            .onEach {
                when (it) {
                    is BillingViewModel.UserInteractionState.OnLoadingStateChange -> onLoading(it.loading)
                    is BillingViewModel.UserInteractionState.OnPay -> onPay()
                    is BillingViewModel.UserInteractionState.PlanValidated -> setPlan(it.plan)
                    else -> {
                        // no operation, not interested in other events
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)
    }

    private fun onLoading(loading: Boolean) {
        // TODO
    }

    private fun onPay() {
        // TODO
    }

    private fun setPlan(plan: PlanShortDetails) {
        // the plan price should come from the Billing Library
        binding.selectedPlanDetailsLayout.plan = plan

        // To obtain plan name for Google Play:
        // val googlePlanName :String? = plan.vendorNames[AppStore.GooglePlay]
        // if (googlePlanName == null) { displayError(...); return }
    }
}
