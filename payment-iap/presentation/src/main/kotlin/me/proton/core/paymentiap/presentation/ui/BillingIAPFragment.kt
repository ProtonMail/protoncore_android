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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.viewmodel.BillingCommonViewModel.Companion.buildPlansList
import me.proton.core.payment.presentation.viewmodel.BillingViewModel
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

    private var billingInput: BillingInput? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userInteractionState
            .onEach {
                when (it) {
                    is BillingViewModel.UserInteractionState.OnLoadingStateChange -> onLoading(it.loading)
                    is BillingViewModel.UserInteractionState.OnPay -> onPay(it.input)
                    is BillingViewModel.UserInteractionState.PlanValidated -> setPlan(it.plan)
                    else -> {
                        // no operation, not interested in other events
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        billingIAPViewModel.billingIAPState
            .onEach {
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (it) {
                    is BillingIAPViewModel.State.Initializing,
                    is BillingIAPViewModel.State.PurchaseStarted,
                    is BillingIAPViewModel.State.QueryingProductDetails -> {
                        // do nothing currently. maybe spinner?
                    }
                    is BillingIAPViewModel.State.UnredeemedPurchase -> {
                        onUnredeemedPurchase(it.purchase)
                    }
                    is BillingIAPViewModel.State.Error.BillingClientDisconnected,
                    is BillingIAPViewModel.State.Error.BillingClientUnavailable -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_billing_client_unavailable))
                        onError(R.string.payments_iap_error_billing_client_unavailable)
                    }
                    is BillingIAPViewModel.State.Error.ProductDetailsError.Message -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_invalid_google_plan))
                        onError(R.string.payments_iap_invalid_google_plan)
                    }
                    is BillingIAPViewModel.State.Error.ProductDetailsError.ProductMismatch,
                    is BillingIAPViewModel.State.Error.ProductDetailsError.Price -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_google_plan_price))
                        onError(R.string.payments_iap_error_google_plan_price)
                    }
                    is BillingIAPViewModel.State.Error.ProductDetailsError.ResponseCode -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_fetching_google_plan))
                        onError(R.string.payments_iap_error_fetching_google_plan)
                    }
                    is BillingIAPViewModel.State.Success.GoogleProductDetails -> {
                        val currentPlan = binding.selectedPlanDetailsLayout.plan ?: return@onEach
                        binding.selectedPlanDetailsLayout.plan = currentPlan.copy(
                            amount = it.amount,
                            currency = it.currency,
                            formattedPriceAndCurrency = it.formattedPriceAndCurrency
                        )
                        viewModel.setGPayButtonState(true)
                    }
                    is BillingIAPViewModel.State.Success.PurchaseSuccess -> {
                        onPurchaseSuccess(it.productId, it.purchaseToken, it.orderID)
                    }
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.Message -> {
                        onError(it.error ?: R.string.payments_iap_general_error)
                    }
                    is BillingIAPViewModel.State.Success.PurchaseFlowLaunched,
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.UserCancelled -> {
                        onError()
                    }
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.ItemAlreadyOwned -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_already_owned))
                        onError(R.string.payments_iap_error_already_owned)
                    }
                }.exhaustive
            }.launchIn(lifecycleScope)

        activity?.apply {
            onBackPressedDispatcher.addCallback {
                finish()
            }
        }
    }

    private fun onLoading(loading: Boolean) {
        // no operation
    }

    private fun onPay(input: BillingInput?) {
        this.billingInput = input
        billingIAPViewModel.makePurchase(input?.userId, requireActivity())
    }

    private fun onPurchaseSuccess(productId: String, purchaseToken: String, orderId: String) {
        requireNotNull(billingInput)
        billingInput?.let {
            viewModel.subscribe(
                userId = it.user,
                planNames = it.existingPlans.buildPlansList(it.plan.name, it.plan.services, it.plan.type),
                codes = it.codes,
                currency = it.plan.currency,
                cycle = it.plan.subscriptionCycle,
                PaymentType.GoogleIAP(
                    productId = productId,
                    purchaseToken = purchaseToken,
                    orderId = orderId,
                    packageName = requireContext().packageName
                ),
                subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
            )
        }
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
        viewModel.setGPayButtonState(false)
    }

    private fun onError(@StringRes error: Int? = null) {
        if (error != null) {
            binding.root.errorSnack(getString(error))
        }
        viewModel.setPayButtonsState(false)
    }

    private fun onUnredeemedPurchase(purchase: GooglePurchase) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.payments_giap_unredeemed_title)
            .setMessage(R.string.payments_giap_unredeemed_description)
            .setPositiveButton(R.string.payments_giap_unredeemed_confirm) { _, _ ->
                billingIAPViewModel.redeemExistingPurchase(purchase)
            }
            .setNegativeButton(R.string.presentation_alert_cancel) { _, _ -> }
            .show()
    }
}
