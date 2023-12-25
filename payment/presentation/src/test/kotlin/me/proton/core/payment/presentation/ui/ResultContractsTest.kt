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

package me.proton.core.payment.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.entity.PaymentOptionsResult
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalInput
import me.proton.core.payment.presentation.entity.PaymentTokenApprovalResult
import me.proton.core.payment.presentation.entity.PaymentVendorDetails
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.plan.domain.entity.SubscriptionManagement
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultContractsTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    private lateinit var tokenApproval: StartPaymentTokenApproval
    private lateinit var paymentOptions: StartPaymentOptions
    private lateinit var billing: StartBilling

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        tokenApproval = StartPaymentTokenApproval
        paymentOptions = StartPaymentOptions
        billing = StartBilling
    }

    @Test
    fun `startPaymentTokenApproval null user`() {
        val input = PaymentTokenApprovalInput(
            userId = null,
            paymentToken = "test-payment-token",
            returnHost = "test-return-host",
            approvalUrl = "test-approval-url",
            amount = 1
        )
        val result = tokenApproval.createIntent(context, input)
        val extras = result.extras?.get(PaymentTokenApprovalActivity.ARG_INPUT) as PaymentTokenApprovalInput
        assertNotNull(extras)
        assertEquals("test-payment-token", extras.paymentToken)
        assertNull(extras.userId)
    }

    @Test
    fun `startPaymentTokenApproval non null user`() {
        val input = PaymentTokenApprovalInput(
            userId = "test-user-id",
            paymentToken = "test-payment-token",
            returnHost = "test-return-host",
            approvalUrl = "test-approval-url",
            amount = 1
        )
        val result = tokenApproval.createIntent(context, input)
        val extras = result.extras?.get(PaymentTokenApprovalActivity.ARG_INPUT) as PaymentTokenApprovalInput
        assertNotNull(extras)
        assertEquals("test-payment-token", extras.paymentToken)
        assertNotNull(extras.userId)
    }

    @Test
    fun `startPaymentTokenApproval parse result nok`() {
        val result = tokenApproval.parseResult(Activity.RESULT_CANCELED, null)
        assertNull(result)
    }

    @Test
    fun `startPaymentTokenApproval parse result ok`() {
        val approvalResult = PaymentTokenApprovalResult(
            approved = true,
            amount = 1,
            token = "test-token"
        )
        val intent = Intent().also {
            it.putExtra(PaymentTokenApprovalActivity.ARG_RESULT, approvalResult)
        }
        val result = tokenApproval.parseResult(Activity.RESULT_OK, intent)
        assertNotNull(result)
        assertEquals(1, result!!.amount)
        assertEquals("test-token", result!!.token)
        assertTrue(result!!.approved)
    }

    @Test
    fun `startPaymentTokenApproval parse result ok intent null`() {
        val result = tokenApproval.parseResult(Activity.RESULT_OK, null)
        assertNull(result)
    }

    @Test
    fun `startPaymentOptions`() {
        val input = PaymentOptionsInput(
            userId = "test-user-id",
            plan = PlanShortDetails(name = "plan-paid",
                displayName = "Plan Paid",
                SubscriptionCycle.MONTHLY,
                amount = 10,
                services = 1,
                type = 1,
                vendors = mapOf(
                    AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
                ))
        )
        val result = paymentOptions.createIntent(context, input)
        val extras = result.extras?.get(PaymentOptionsActivity.ARG_INPUT) as PaymentOptionsInput
        assertNotNull(extras)
        assertNotNull(extras.plan)
        assertNotNull(extras.userId)
    }

    @Test
    fun `startPaymentOptions parse result nok`() {
        val result = paymentOptions.parseResult(Activity.RESULT_CANCELED, null)
        assertNull(result)
    }

    @Test
    fun `startPaymentOptions parse result ok intent null`() {
        val result = paymentOptions.parseResult(Activity.RESULT_OK, null)
        assertNull(result)
    }

    @Test
    fun `startPaymentOptions parse result ok`() {
        val optionsResult = PaymentOptionsResult(
            result = true
        )
        val intent = Intent().also {
            it.putExtra(PaymentsActivity.ARG_RESULT, optionsResult)
        }
        val result = paymentOptions.parseResult(Activity.RESULT_OK, intent)
        assertNotNull(result)
        assertTrue(result.result)
    }

    @Test
    fun `startBilling`() {
        val input = BillingInput(
            null, emptyList(), PlanShortDetails(name = "plan-paid",
                displayName = "Plan Paid",
                SubscriptionCycle.MONTHLY,
                amount = 10,
                services = 1,
                type = 1,
                vendors = mapOf(
                    AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
                )), null, null
        )
        val result = billing.createIntent(context, input)
        val extras = result.extras?.get(BillingActivity.ARG_BILLING_INPUT) as BillingInput
        assertNotNull(extras)
        assertNotNull(extras.plan)
        assertNull(extras.userId)
    }

    @Test
    fun `startBilling parse result nok`() {
        val result = billing.parseResult(Activity.RESULT_CANCELED, null)
        assertNull(result)
    }

    @Test
    fun `startBilling parse result ok intent null`() {
        val result = billing.parseResult(Activity.RESULT_OK, null)
        assertNull(result)
    }

    @Test
    fun `startBilling parse result ok`() {
        val billingResult = BillingResult(
            paySuccess = true,
            token = "test-token",
            subscriptionCreated = true,
            amount = 1,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        val intent = Intent().also {
            it.putExtra(PaymentsActivity.ARG_RESULT, billingResult)
        }
        val result = billing.parseResult(Activity.RESULT_OK, intent)
        assertNotNull(result)
        assertTrue(result.paySuccess)
    }
}