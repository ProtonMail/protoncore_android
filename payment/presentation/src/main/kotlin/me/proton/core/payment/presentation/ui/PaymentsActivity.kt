/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.payment.presentation.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.ViewDataBinding
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentOptionsResult
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalInput
import me.proton.core.payment.presentation.entity.PlanDetails
import me.proton.core.presentation.ui.ProtonActivity
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.isNightMode
import me.proton.core.presentation.utils.showToast

abstract class PaymentsActivity<DB : ViewDataBinding> : ProtonActivity<DB>() {

    private var tokenApprovalLauncher: ActivityResultLauncher<PaymentTokenApprovalInput>? = null
    private var newBillingLauncher: ActivityResultLauncher<BillingInput>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tokenApprovalLauncher = registerForPaymentTokenApproval()
        newBillingLauncher = registerForNewBilling()

        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            if (!isNightMode()) View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else 0

        window.decorView.systemUiVisibility = flags
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun registerForPaymentTokenApproval(): ActivityResultLauncher<PaymentTokenApprovalInput> =
        registerForActivityResult(
            StartPaymentTokenApproval()
        ) {
            it?.apply {
                if (approved) {
                    onThreeDSApproved(amount, token)
                } else {
                    showToast(R.string.payments_3ds_not_approved)
                }
            }
        }

    private fun registerForNewBilling(): ActivityResultLauncher<BillingInput> =
        registerForActivityResult(
            StartBilling()
        ) {
            it?.apply {
                if (paySuccess) {
                    onPaymentSuccess(this)
                } else {
                    showToast("Billing Error happened!")
                }
            }
        }

    protected fun onTokenApprovalNeeded(
        userId: String?,
        paymentToken: PaymentToken.CreatePaymentTokenResult,
        amount: Long
    ) {
        tokenApprovalLauncher?.launch(
            PaymentTokenApprovalInput(
                userId, paymentToken.token, paymentToken.returnHost!!, paymentToken.approvalUrl!!, amount
            )
        )
    }

    protected fun startBilling(
        userId: String?,
        currentPlans: List<String>,
        plan: PlanDetails,
        codes: List<String>?
    ) {
        newBillingLauncher?.launch(
            BillingInput(userId, currentPlans, plan, codes, null)
        )
    }

    protected fun onPaymentSuccess(billingResult: BillingResult?) {
        val intent = Intent()
            .putExtra(ARG_RESULT, PaymentOptionsResult(true, billingResult))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    open fun onThreeDSApproved(amount: Long, token: String) {
        // no default operation
    }

    open fun showLoading(loading: Boolean) {
        // no default operation
    }

    open fun onError(message: String? = null) {
        // no default operation
    }

    open fun showError(message: String?) {
        showLoading(false)
        binding.root.errorSnack(message = message ?: getString(R.string.payments_general_error))
    }

    companion object {
        const val ARG_RESULT = "arg.billingResult"
    }
}
