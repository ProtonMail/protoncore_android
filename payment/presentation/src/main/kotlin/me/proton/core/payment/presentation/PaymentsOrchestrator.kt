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

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.ui.StartBilling
import me.proton.core.payment.presentation.ui.StartPaymentOptions
import javax.inject.Inject

@Suppress("UseIfInsteadOfWhen")
class PaymentsOrchestrator @Inject constructor() {

    // region result launchers
    private var billingLauncher: ActivityResultLauncher<BillingInput>? = null
    private var paymentOptionsLauncher: ActivityResultLauncher<PaymentOptionsInput>? = null
    // endregion

    private var onPaymentResultListener: (result: BillingResult?) -> Unit = {}

    private fun <T> checkRegistered(launcher: ActivityResultLauncher<T>?) =
        checkNotNull(launcher) { "You must call PaymentsOrchestrator.register(context) before starting workflow!" }

    // region public api
    fun register(caller: ActivityResultCaller) {
        billingLauncher = registerBillingResult(caller)
        paymentOptionsLauncher = registerPaymentOptionsResult(caller)
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
        when (userId) {
            // Directly start the billing screen.
            null -> checkRegistered(billingLauncher).launch(
                BillingInput(null, emptyList(), selectedPlan, codes, null)
            )
            // Start the payment options chooser screen.
            else -> checkRegistered(paymentOptionsLauncher).launch(
                PaymentOptionsInput(userId.id, selectedPlan, codes)
            )
        }
    }
    // endregion

    private fun registerBillingResult(
        context: ActivityResultCaller
    ): ActivityResultLauncher<BillingInput> =
        context.registerForActivityResult(
            StartBilling()
        ) {
            // for Sign Up the token should be used as a human verification header
            onPaymentResultListener(it)
        }

    private fun registerPaymentOptionsResult(
        caller: ActivityResultCaller
    ): ActivityResultLauncher<PaymentOptionsInput> =
        caller.registerForActivityResult(
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
