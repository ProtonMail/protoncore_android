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
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.AppStore
import me.proton.core.paymentcommon.presentation.entity.PlanShortDetails
import me.proton.core.paymentcommon.presentation.viewmodel.BillingViewModel
import me.proton.core.paymentiap.presentation.LogTag.DEFAULT
import me.proton.core.paymentiap.presentation.R
import me.proton.core.paymentiap.presentation.databinding.FragmentBillingIapBinding
import me.proton.core.paymentiap.presentation.entity.GooglePlanShortDetails
import me.proton.core.paymentiap.presentation.viewmodel.BillingIAPViewModel
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive

/**
 * Fragment that handles Billing In App Purchases from Google.
 */
@AndroidEntryPoint
public class BillingIAPFragment : ProtonFragment(R.layout.fragment_billing_iap) {

    private val viewModel: BillingViewModel by viewModels({ requireActivity() })
    private val billingIAPViewModel by viewModels<BillingIAPViewModel>()
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

        billingIAPViewModel.state
            .onEach {
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (it) {
                    is BillingIAPViewModel.State.Error.BillingClientDisconnected,
                    is BillingIAPViewModel.State.Error.BillingClientUnavailable -> {
                        onError(R.string.payments_iap_general_error)
                    }
                    is BillingIAPViewModel.State.GoogleProductDetails -> {
                        val currentPlan = binding.selectedPlanDetailsLayout.plan ?: return@onEach
                        binding.selectedPlanDetailsLayout.plan = currentPlan.copy(
                            amount = it.amount,
                            currency = it.currency,
                            formattedPriceAndCurrency = it.formattedPriceAndCurrency
                        )
                    }
                    is BillingIAPViewModel.State.Error.ProductDetailsError.Message -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_invalid_google_plan))
                        onError(R.string.payments_iap_invalid_google_plan)
                    }
                    is BillingIAPViewModel.State.Error.ProductDetailsError.Price -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_google_plan_price))
                        onError(R.string.payments_iap_error_google_plan_price)
                    }
                    is BillingIAPViewModel.State.Error.ProductDetailsError.ResponseCode -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_fetching_google_plan))
                        onError(R.string.payments_iap_error_fetching_google_plan)
                    }
                    is BillingIAPViewModel.State.Initialized,
                    is BillingIAPViewModel.State.Initializing,
                    is BillingIAPViewModel.State.QueryingProductDetails -> {
                        // do nothing currently. maybe spinner?
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        requireActivity().onBackPressedDispatcher.addCallback {
            requireActivity().finish()
        }
    }

    private fun onLoading(loading: Boolean) {
        // no operation
    }

    private fun onPay() {
        // TODO
    }

    private fun setPlan(plan: PlanShortDetails) {
        // the plan price should come from the Billing Library
        binding.selectedPlanDetailsLayout.plan = GooglePlanShortDetails.fromPlanShortDetails(plan)
        val googlePlanName: String? = plan.vendorNames[AppStore.GooglePlay]
        if (googlePlanName == null) {
            onError(R.string.payments_iap_invalid_google_plan)
        } else {
            lifecycleScope.launch {
                billingIAPViewModel.queryProductDetails(googlePlanName)
            }
        }
    }

    private fun onError(@StringRes error: Int) {
        binding.root.errorSnack(getString(error))
    }
}
