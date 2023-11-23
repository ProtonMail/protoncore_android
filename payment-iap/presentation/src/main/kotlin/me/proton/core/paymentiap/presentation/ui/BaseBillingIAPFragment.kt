/*
 * Copyright (c) 2023 Proton AG
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
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.paymentiap.domain.entity.GoogleProductPrice
import me.proton.core.paymentiap.presentation.LogTag.DEFAULT
import me.proton.core.paymentiap.presentation.R
import me.proton.core.paymentiap.presentation.viewmodel.BillingIAPViewModel
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive

/**
 * Fragment that handles Billing In App Purchases from Google.
 */
public abstract class BaseBillingIAPFragment(
    @LayoutRes contentLayout: Int
) : ProtonFragment(contentLayout) {

    private val billingIAPViewModel by viewModels<BillingIAPViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        billingIAPViewModel.billingIAPState
            .onEach {
                when (it) {
                    is BillingIAPViewModel.State.Initializing,
                    is BillingIAPViewModel.State.PurchaseStarted,
                    is BillingIAPViewModel.State.Success.PurchaseFlowLaunched,
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
                    is BillingIAPViewModel.State.Success.GoogleProductsDetails -> {
                        onPricesAvailable(it.details)
                    }
                    is BillingIAPViewModel.State.Success.PurchaseSuccess -> {
                        onPurchaseSuccess(it.productId, it.purchaseToken, it.orderID, it.customerId, it.billingInput)
                    }
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.Message -> {
                        onError(it.error ?: R.string.payments_iap_general_error)
                    }
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.UserCancelled -> {
                        onUserCanceled()
                    }
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.ItemAlreadyOwned -> {
                        CoreLogger.i(DEFAULT, getString(R.string.payments_iap_error_already_owned))
                        onError(R.string.payments_iap_error_already_owned)
                    }
                    is BillingIAPViewModel.State.Error.ProductPurchaseError.IncorrectCustomerId -> {
                        CoreLogger.i(DEFAULT, "Customer ID is incorrect.")
                        onError(R.string.payments_iap_general_error)
                    }
                }.exhaustive
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        addOnBackPressedCallback {
            requireActivity().finish()
        }
    }

    protected fun queryGooglePlans(productIds: List<ProductId>) {
        billingIAPViewModel.queryProductDetails(productIds)
    }

    protected fun pay(input: BillingInput) {
        billingIAPViewModel.makePurchase(requireActivity(), input)
    }

    protected abstract fun onPricesAvailable(
        details: Map<ProductId, GoogleProductPrice>
    )

    protected abstract fun onPurchaseSuccess(
        productId: String,
        purchaseToken: GooglePurchaseToken,
        orderId: String,
        customerId: String,
        billingInput: BillingInput
    )

    protected abstract fun onError(@StringRes errorRes: Int)
    protected abstract fun onUserCanceled()

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
