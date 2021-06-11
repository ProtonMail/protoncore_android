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

package me.proton.core.payment.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.ui.StartBilling
import me.proton.core.payment.presentation.ui.StartPaymentOptions
import javax.inject.Inject

class PaymentsOrchestrator @Inject constructor() {

    // region result launchers
    private var billingLauncher: ActivityResultLauncher<BillingInput>? = null
    private var paymentOptionsLauncher: ActivityResultLauncher<PaymentOptionsInput>? = null
    // endregion

    private var onPaymentResultListener: (result: BillingResult?) -> Unit = {}

    // region public api
    fun register(context: ComponentActivity) {
        billingLauncher = registerBillingResult(context)
        paymentOptionsLauncher = registerPaymentOptionsResult(context)
    }

    fun setOnPaymentResult(block: (result: BillingResult?) -> Unit) {
        onPaymentResultListener = block
    }

    /**
     * Starts the billing workflow.
     * @param userId set the user id of the user doing the payment (upgrade), or pass null for new user
     * creation (sign up).
     * @param selectedPlan the selected plan ID
     * @param codes the coupon codes (if any)
     */
    fun startBillingWorkFlow(
        userId: UserId? = null,
        selectedPlan: PlanShortDetails,
        codes: List<String>? = null
    ) {
        userId?.let {
            // start the payment options chooser screen
            paymentOptionsLauncher?.launch(
                PaymentOptionsInput(it.id, selectedPlan, codes)
            ) ?: throw IllegalStateException("You must call register(context) before any start workflow function!")
        } ?: run {
            // directly start the billing screen
            billingLauncher?.launch(
                BillingInput(null, emptyList(), selectedPlan, codes, null)
            ) ?: throw IllegalStateException("You must call register before any start workflow function!")
        }
    }
    // endregion

    private fun registerBillingResult(
        context: ComponentActivity
    ): ActivityResultLauncher<BillingInput> =
        context.registerForActivityResult(
            StartBilling()
        ) {
            // for Sign Up the token should be used as a human verification header
            onPaymentResultListener(it)
        }

    private fun registerPaymentOptionsResult(
        context: ComponentActivity
    ): ActivityResultLauncher<PaymentOptionsInput> =
        context.registerForActivityResult(
            StartPaymentOptions()
        ) {
            onPaymentResultListener(it?.billing)
        }
}

fun PaymentsOrchestrator.onPaymentResult(
    block: (result: BillingResult?) -> Unit
): PaymentsOrchestrator {
    setOnPaymentResult { block(it) }
    return this
}
