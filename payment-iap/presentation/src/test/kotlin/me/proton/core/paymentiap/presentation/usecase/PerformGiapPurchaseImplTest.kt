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

package me.proton.core.paymentiap.presentation.usecase

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.plan.domain.entity.DynamicPlanPrice
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.plan.domain.entity.DynamicPlanVendor
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.PerformGiapPurchase
import me.proton.core.plan.domain.usecase.PerformSubscribe
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PerformGiapPurchaseImplTest {
    @MockK
    private lateinit var createPaymentTokenForGooglePurchase: CreatePaymentTokenForGooglePurchase

    @MockK
    private lateinit var launchGiapBillingFlow: LaunchGiapBillingFlow<Activity>

    @MockK
    private lateinit var prepareGiapPurchase: PrepareGiapPurchase

    @MockK
    private lateinit var performSubscribe: PerformSubscribe

    private lateinit var tested: PerformGiapPurchaseImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = PerformGiapPurchaseImpl(
            createPaymentTokenForGooglePurchase,
            launchGiapBillingFlow,
            prepareGiapPurchase,
            performSubscribe
        )
    }

    @Test
    fun `billing client error`() = runTest {
        // GIVEN
        val cycle = SubscriptionCycle.YEARLY
        val billingClientError =
            BillingClientError(BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE, null)
        coEvery { prepareGiapPurchase(any(), any()) } throws billingClientError

        // WHEN
        val result = tested(mockk(), cycle.value, mailPlusPlan, userId = null)

        // THEN
        assertEquals(
            PerformGiapPurchase.Result.Error.RecoverableBillingError(billingClientError),
            result
        )
    }

    @Test
    fun `unrecoverable billing client error`() = runTest {
        // GIVEN
        val cycle = SubscriptionCycle.YEARLY
        val billingClientError =
            BillingClientError(BillingClient.BillingResponseCode.DEVELOPER_ERROR, null)
        coEvery { prepareGiapPurchase(any(), any()) } throws billingClientError

        // WHEN
        val result = tested(mockk(), cycle.value, mailPlusPlan, userId = null)

        // THEN
        assertEquals(
            PerformGiapPurchase.Result.Error.UnrecoverableBillingError(billingClientError),
            result
        )
    }

    @Test
    fun `user cancelled`() = runTest {
        // GIVEN
        val cycle = SubscriptionCycle.YEARLY
        val purchase = mockk<Purchase>().wrap()
        val billingClientError =
            BillingClientError(BillingClient.BillingResponseCode.USER_CANCELED, null)

        coEvery { prepareGiapPurchase(any(), any()) } returns
                PrepareGiapPurchase.Result.Success(mockk())
        coEvery { launchGiapBillingFlow(any(), any(), any()) } returns
                LaunchGiapBillingFlow.Result.PurchaseSuccess(purchase)
        coEvery { createPaymentTokenForGooglePurchase(any(), any(), any(), any(), any()) } throws
                billingClientError

        // WHEN
        val result = tested(mockk(), cycle.value, mailPlusPlan, userId = null)

        // THEN
        assertEquals(
            PerformGiapPurchase.Result.Error.UserCancelled,
            result
        )
    }

    @Test
    fun `successful giap for new user`() = runTest {
        // GIVEN
        val amount = 499L
        val currency = Currency.CHF
        val cycle = SubscriptionCycle.YEARLY
        val purchase = mockk<Purchase>().wrap()
        val token = ProtonPaymentToken("payment-token")

        coEvery { prepareGiapPurchase(any(), any()) } returns
                PrepareGiapPurchase.Result.Success(mockk())
        coEvery { launchGiapBillingFlow(any(), any(), any()) } returns
                LaunchGiapBillingFlow.Result.PurchaseSuccess(purchase)
        coEvery { createPaymentTokenForGooglePurchase(any(), any(), any(), any(), any()) } returns
                CreatePaymentTokenForGooglePurchase.Result(
                    amount,
                    cycle,
                    currency,
                    listOf(TEST_PLAN_NAME),
                    token
                )

        // WHEN
        val result = tested(mockk(), cycle.value, mailPlusPlan, userId = null)

        // THEN
        assertEquals(
            PerformGiapPurchase.Result.GiapSuccess(
                purchase,
                amount,
                currency.name,
                subscriptionCreated = false,
                token
            ),
            result
        )
    }

    @Test
    fun `successful giap for existing user`() = runTest {
        // GIVEN
        val amount = 499L
        val currency = Currency.CHF
        val cycle = SubscriptionCycle.YEARLY
        val purchase = mockk<Purchase>().wrap()
        val token = ProtonPaymentToken("payment-token")

        coEvery { prepareGiapPurchase(any(), any()) } returns
                PrepareGiapPurchase.Result.Success(mockk())
        coEvery { launchGiapBillingFlow(any(), any(), any()) } returns
                LaunchGiapBillingFlow.Result.PurchaseSuccess(purchase)
        coEvery { createPaymentTokenForGooglePurchase(any(), any(), any(), any(), any()) } returns
                CreatePaymentTokenForGooglePurchase.Result(
                    amount,
                    cycle,
                    currency,
                    listOf(TEST_PLAN_NAME),
                    token
                )
        coEvery { performSubscribe(any(), any(), any(), any(), any(), any(), any(), any()) } returns
                mockk()

        // WHEN
        val result = tested(mockk(), cycle.value, mailPlusPlan, userId = UserId("user-id"))

        // THEN
        assertEquals(
            PerformGiapPurchase.Result.GiapSuccess(
                purchase,
                amount,
                currency.name,
                subscriptionCreated = true,
                token
            ),
            result
        )
    }
}

private const val TEST_PLAN_NAME = "mail2022"

private val mailPlusPlan = DynamicPlan(
    name = TEST_PLAN_NAME,
    order = 1,
    state = DynamicPlanState.Available,
    title = "Mail Plus",
    type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
    instances = mapOf(
        12 to DynamicPlanInstance(
            cycle = 12,
            description = "Description",
            periodEnd = Instant.MAX,
            price = mapOf("CHF" to DynamicPlanPrice("id", "CHF", 499)),
            vendors = mapOf(AppStore.GooglePlay to DynamicPlanVendor("product-id", "customer-id"))
        )
    )
)
