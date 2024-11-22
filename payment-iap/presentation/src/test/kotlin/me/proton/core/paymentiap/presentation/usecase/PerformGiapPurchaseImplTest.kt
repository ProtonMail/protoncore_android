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
import com.android.billingclient.api.AccountIdentifiers
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.presentation.entity.mockPurchase
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.plan.domain.entity.DynamicPlanPrice
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.plan.domain.entity.DynamicPlanVendor
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.PerformGiapPurchase
import me.proton.core.plan.domain.usecase.PerformSubscribe
import me.proton.core.user.domain.UserManager
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

    @MockK(relaxed = true)
    private lateinit var purchaseRepository: PurchaseRepository

    @MockK(relaxed = true)
    private lateinit var sessionProvider: SessionProvider

    @MockK(relaxed = true)
    private lateinit var userManager: UserManager

    private lateinit var tested: PerformGiapPurchaseImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { sessionProvider.getSessionId(any()) } returns SessionId("sessionId")

        tested = PerformGiapPurchaseImpl(
            createPaymentTokenForGooglePurchase,
            launchGiapBillingFlow,
            prepareGiapPurchase,
            purchaseRepository,
            sessionProvider
        )
    }

    @Test
    fun `billing client error`() = runTest {
        // GIVEN
        val cycle = SubscriptionCycle.YEARLY
        val billingClientError =
            BillingClientError(BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE, null)
        coEvery { prepareGiapPurchase(any(), any(), any()) } throws billingClientError

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
        coEvery { prepareGiapPurchase(any(), any(), any()) } throws billingClientError

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
        val purchase = mockPurchase().wrap()
        val billingClientError =
            BillingClientError(BillingClient.BillingResponseCode.USER_CANCELED, null)

        coEvery { prepareGiapPurchase(any(), any(), any()) } returns
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
        val purchase = mockPurchase().wrap()
        val token = ProtonPaymentToken("payment-token")

        coEvery { prepareGiapPurchase(any(), any(), any()) } returns
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
        coVerify { purchaseRepository.upsertPurchase(any()) }
        assertEquals(
            PerformGiapPurchase.Result.GiapSuccess(
                purchase,
                amount,
                currency.name,
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
        val purchase = mockPurchase().wrap()
        val token = ProtonPaymentToken("payment-token")

        coEvery { prepareGiapPurchase(any(), any(), any()) } returns
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
        val result = tested(mockk(), cycle.value, mailPlusPlan, userId = UserId("user-id"))

        // THEN
        coVerify { purchaseRepository.upsertPurchase(any()) }
        assertEquals(
            PerformGiapPurchase.Result.GiapSuccess(
                purchase,
                amount,
                currency.name,
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
