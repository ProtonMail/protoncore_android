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
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.entity.PaymentOptionsResult
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalInput
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalResult
import me.proton.core.payment.presentation.entity.BillingInput

internal object StartPaymentOptions : ActivityResultContract<PaymentOptionsInput, PaymentOptionsResult?>() {
    override fun createIntent(context: Context, input: PaymentOptionsInput): Intent =

        Intent(context, PaymentOptionsActivity::class.java).apply {
            putExtra(PaymentOptionsActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentOptionsResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(PaymentsActivity.ARG_RESULT)
    }

}

internal object StartPaymentTokenApproval : ActivityResultContract<PaymentTokenApprovalInput, PaymentTokenApprovalResult?>() {
    override fun createIntent(context: Context, input: PaymentTokenApprovalInput): Intent =
        Intent(context, PaymentTokenApprovalActivity::class.java).apply {
            putExtra(PaymentTokenApprovalActivity.ARG_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): PaymentTokenApprovalResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(PaymentTokenApprovalActivity.ARG_RESULT)
    }
}

internal object StartBilling : ActivityResultContract<BillingInput, BillingResult?>() {
    override fun createIntent(context: Context, input: BillingInput): Intent =
        Intent(context, BillingActivity::class.java).apply {
            putExtra(BillingActivity.ARG_BILLING_INPUT, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): BillingResult? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra(BillingActivity.ARG_BILLING_RESULT)
    }
}
