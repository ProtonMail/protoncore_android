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

package me.proton.core.payment.presentation

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.PaymentOptionsInput
import me.proton.core.payment.presentation.entity.PaymentVendorDetails
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.ui.StartBilling
import me.proton.core.payment.presentation.ui.StartPaymentOptions
import org.junit.After
import org.junit.Before
import org.junit.Test

class PaymentsOrchestratorTest {

    private val userId = UserId("test")

    private val billingLauncher = mockk<ActivityResultLauncher<BillingInput>>(relaxed = true)
    private val paymentOptionsLauncher = mockk<ActivityResultLauncher<PaymentOptionsInput>>(relaxed = true)
    private val caller = mockk<ActivityResultCaller>(relaxed = true) {
        every { registerForActivityResult(StartBilling, any()) } returns billingLauncher
        every { registerForActivityResult(StartPaymentOptions, any()) } returns paymentOptionsLauncher
    }

    private lateinit var orchestrator: PaymentsOrchestrator

    @Before
    fun beforeEveryTest() {
        orchestrator = PaymentsOrchestrator()
    }

    @Test
    fun registerLaunchers() = runTest {
        // WHEN
        orchestrator.register(caller)
        // THEN
        verify { caller.registerForActivityResult(any<StartBilling>(), any()) }
        verify { caller.registerForActivityResult(any<StartPaymentOptions>(), any()) }
    }

    @Test
    fun unregisterLaunchers() = runTest {
        // WHEN
        orchestrator.register(caller)
        orchestrator.unregister()
        // THEN
        verify { billingLauncher.unregister() }
        verify { paymentOptionsLauncher.unregister() }
    }

    @Test
    fun `startBillingWorkFlow user null`() = runTest {
        // GIVEN
        val planDetails = PlanShortDetails(
            name = "plan-paid",
            displayName = "Plan Paid",
            SubscriptionCycle.MONTHLY,
            amount = 10,
            services = 1,
            type = 1,
            vendors = mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
            )
        )
        orchestrator.register(caller)
        // WHEN
        orchestrator.startBillingWorkFlow(selectedPlan = planDetails)
        // Then
        verify(exactly = 0) { paymentOptionsLauncher.launch(any()) }
        verify(exactly = 1) { billingLauncher.launch(BillingInput(null, emptyList(), planDetails, null, null)) }
    }

    @Test
    fun `startBillingWorkFlow user non null`() = runTest {
        // GIVEN
        val planDetails = PlanShortDetails(
            name = "plan-paid",
            displayName = "Plan Paid",
            SubscriptionCycle.MONTHLY,
            amount = 10,
            services = 1,
            type = 1,
            vendors = mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("cus-id", "vendor-paid-plan")
            )
        )
        orchestrator.register(caller)
        // WHEN
        orchestrator.startBillingWorkFlow(userId = userId, selectedPlan = planDetails)
        // Then
        verify(exactly = 0) { billingLauncher.launch(any()) }
        verify(exactly = 1) { paymentOptionsLauncher.launch(PaymentOptionsInput(userId.id, planDetails, null)) }
    }
}
