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

package me.proton.core.payment.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runCurrent
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.usecase.GetCountry
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.presentation.ActivePaymentProviderImpl
import me.proton.core.payment.presentation.R
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.usecase.PerformSubscribe
import me.proton.core.plan.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.UserManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillingViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private val validateSubscription = mockk<ValidateSubscriptionPlan>()
    private val createPaymentToken = mockk<CreatePaymentToken>()
    private val performSubscribe = mockk<PerformSubscribe>()
    private val getCountryCode = mockk<GetCountry>()
    private val humanVerificationManager = mockk<HumanVerificationManager>(relaxed = true)
    private val clientIdProvider = mockk<ClientIdProvider>(relaxed = true)
    private val getAvailablePaymentProviders = mockk<GetAvailablePaymentProviders>(relaxed = true)
    private val activePaymentProvider = mockk<me.proton.core.payment.presentation.ActivePaymentProvider>(relaxed = true)
    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
    private val userManager = mockk<UserManager>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPlanIds = listOf("test-plan-id")
    private val testCurrency = Currency.CHF
    private val testSubscriptionCycle = SubscriptionCycle.YEARLY
    private val testCCNumber = "123456789"
    private val testCCCvc = "123"
    private val testCCExpMonth = "05"
    private val testCCExpYear = "25"
    private val testCCName = "test-name"
    private val testCCCountry = "test-country"
    private val testCCZip = "test-zip"
    private val testCard = Card.CardWithPaymentDetails(
        testCCNumber, testCCCvc, testCCExpMonth, testCCExpYear, testCCName, testCCCountry, testCCZip
    )
    // endregion

    private lateinit var billingViewModel: BillingViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getCountryCode.invoke(any()) } returns Country(testCCCountry, "test-code-1")

        billingViewModel = BillingViewModel(
            activePaymentProvider,
            validateSubscription,
            createPaymentToken,
            performSubscribe,
            getCountryCode,
            humanVerificationManager,
            clientIdProvider,
            observabilityManager,
            userManager
        )
    }

    @Test
    fun `upgrade subscription with new credit card success handled properly`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        val expectedCard = PaymentType.CreditCard(testCard.copy(expirationYear = "2025"))
        coEvery {
            validateSubscription.invoke(
                testUserId,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentToken.invoke(
                testUserId,
                2,
                testCurrency,
                expectedCard
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            testUserId,
            testPlanIds,
            null,
            testCurrency,
            testSubscriptionCycle,
            paymentType,
            SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, expectedCard) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `upgrade subscription with Google IAP success handled properly`() = coroutinesTest {
        // GIVEN
        val productId = "test-product-it"
        val purchaseToken = GooglePurchaseToken("test-purchase-token")
        val orderId = "test-order-id"
        val packageName = "test-package-name"
        val paymentType = PaymentType.GoogleIAP(productId, purchaseToken, orderId, packageName, "customer-id")
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentToken.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                paymentType = paymentType
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE, null, ProtonPaymentToken("test-token"), null
        )

        coEvery {
            performSubscribe.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                cycle = testSubscriptionCycle,
                planNames = testPlanIds,
                codes = null,
                paymentToken = ProtonPaymentToken("test-token"),
                subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
            )
        } returns mockk()

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.SubscriptionPlanValidated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.SubscriptionCreated>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            testUserId,
            testPlanIds,
            null,
            testCurrency,
            testSubscriptionCycle,
            paymentType,
            SubscriptionManagement.GOOGLE_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
        coVerify(exactly = 1) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `sign up subscription success handled properly`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        val expectedCard = PaymentType.CreditCard(testCard.copy(expirationYear = "2025"))
        coEvery {
            validateSubscription.invoke(
                userId = null,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus
        coEvery {
            createPaymentToken.invoke(
                userId = null,
                amount = 2,
                currency = testCurrency,
                paymentType = expectedCard
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            null, testPlanIds, null, testCurrency, testSubscriptionCycle,
            paymentType, SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, expectedCard) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `sign up subscription Google IAP success handled properly`() = coroutinesTest {
        // GIVEN
        val productId = "test-product-it"
        val purchaseToken = GooglePurchaseToken("test-purchase-token")
        val orderId = "test-order-id"
        val packageName = "test-package-name"
        val paymentType = PaymentType.GoogleIAP(productId, purchaseToken, orderId, packageName, "customer-id")
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        coEvery {
            validateSubscription.invoke(
                userId = null,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus
        coEvery {
            createPaymentToken.invoke(
                userId = null,
                amount = 2,
                currency = testCurrency,
                paymentType = paymentType
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE, null, ProtonPaymentToken("test-token"), null
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.SignUpTokenReady>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            null, testPlanIds, null, testCurrency, testSubscriptionCycle,
            paymentType, SubscriptionManagement.GOOGLE_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, paymentType) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `upgrade subscription with existing payment method success handled properly`() = coroutinesTest {
        // GIVEN
        val testPaymentMethodId = "test-payment-method-id"
        val paymentType = PaymentType.PaymentMethod(testPaymentMethodId)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )
        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentToken.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                paymentType = paymentType
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            testUserId,
            testPlanIds,
            null,
            testCurrency,
            testSubscriptionCycle,
            paymentType,
            SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) {
            createPaymentToken.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                paymentType = paymentType
            )
        }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `sign up with existing payment method error handled properly`() = coroutinesTest {
        // GIVEN
        val testPaymentMethodId = "test-payment-method-id"
        val paymentType = PaymentType.PaymentMethod(testPaymentMethodId)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        coEvery {
            validateSubscription.invoke(
                userId = null,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentToken.invoke(
                userId = null,
                amount = 2,
                currency = testCurrency,
                paymentType = paymentType
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Error.SignUpWithPaymentMethodUnsupported>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            null, testPlanIds, null, testCurrency, testSubscriptionCycle,
            paymentType, SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 0) { createPaymentToken.invoke(any(), any(), any(), any()) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `upgrade subscription with existing payment method amount due zero handled properly`() = coroutinesTest {
        // GIVEN
        val testPaymentMethodId = "test-payment-method-id"
        val paymentType = PaymentType.PaymentMethod(testPaymentMethodId)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 0,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 5,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        coEvery {
            performSubscribe.invoke(
                userId = testUserId,
                amount = 0,
                currency = testCurrency,
                cycle = testSubscriptionCycle,
                planNames = testPlanIds,
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        } returns mockk()

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.SubscriptionCreated>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            userId = testUserId,
            planNames = testPlanIds,
            codes = null,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            paymentType = paymentType,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 0) { createPaymentToken.invoke(any(), any(), any(), any()) }
        coVerify(exactly = 1) {
            performSubscribe.invoke(
                userId = testUserId,
                amount = 0,
                currency = testCurrency,
                cycle = testSubscriptionCycle,
                planNames = testPlanIds,
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
    }

    @Test
    fun `upgrade subscription chargeable token handled properly`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        val expectedCard = PaymentType.CreditCard(testCard.copy(expirationYear = "2025"))
        coEvery {
            createPaymentToken.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                paymentType = expectedCard
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        coEvery {
            performSubscribe.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                cycle = testSubscriptionCycle,
                planNames = testPlanIds,
                codes = null,
                paymentToken = ProtonPaymentToken("test-token"),
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        } returns mockk()

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.SubscriptionCreated>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            userId = testUserId,
            planNames = testPlanIds,
            codes = null,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            paymentType = paymentType,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, expectedCard) }
        coVerify(exactly = 1) {
            performSubscribe.invoke(
                testUserId, 2, testCurrency, testSubscriptionCycle,
                testPlanIds, null, ProtonPaymentToken("test-token"), SubscriptionManagement.PROTON_MANAGED
            )
        }
    }

    @Test
    fun `upgrade subscription plan error handled properly`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)

        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Error.General)
            assertEquals("proton error", subscriptionPlanStatus.error.getUserMessage(mockk()))
        }

        // WHEN
        billingViewModel.subscribe(
            userId = testUserId,
            planNames = testPlanIds,
            codes = null,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            paymentType = paymentType,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 0) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `sign up and 3DS token approved`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        val expectedCard = PaymentType.CreditCard(testCard.copy(expirationYear = "2025"))
        coEvery {
            validateSubscription.invoke(
                userId = null,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus
        coEvery {
            createPaymentToken.invoke(
                userId = null,
                amount = 2,
                currency = testCurrency,
                paymentType = expectedCard
            )
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.SignUpTokenReady>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            userId = null,
            planNames = testPlanIds,
            codes = null,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            paymentType = paymentType,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        billingViewModel.onThreeDSTokenApproved(
            userId = null,
            planIds = testPlanIds,
            codes = null,
            amount = 2,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            token = ProtonPaymentToken("test-token"),
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, expectedCard) }
        coVerify(exactly = 1) { humanVerificationManager.addDetails(any()) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `upgrade subscription and 3DS token approved`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        val expectedCard = PaymentType.CreditCard(testCard.copy(expirationYear = "2025"))
        coEvery {
            validateSubscription.invoke(testUserId, null, testPlanIds, testCurrency, testSubscriptionCycle)
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentToken.invoke(testUserId, 2, testCurrency, expectedCard)
        } returns PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )

        coEvery {
            performSubscribe.invoke(
                userId = testUserId,
                amount = 2,
                currency = testCurrency,
                cycle = testSubscriptionCycle,
                planNames = testPlanIds,
                codes = null,
                paymentToken = ProtonPaymentToken("test-token"),
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        } returns mockk()

        val job = flowTest(billingViewModel.subscriptionResult) {
            // THEN
            assertIs<BillingCommonViewModel.State.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.State.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingCommonViewModel.State.Success.TokenCreated>(awaitItem())
            assertIs<BillingCommonViewModel.State.Incomplete.TokenApprovalNeeded>(awaitItem())
            assertIs<BillingCommonViewModel.State.Success.SubscriptionCreated>(awaitItem())
        }

        // WHEN
        billingViewModel.subscribe(
            userId = testUserId,
            planNames = testPlanIds,
            codes = null,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            paymentType = paymentType,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        billingViewModel.onThreeDSTokenApproved(
            userId = testUserId,
            planIds = testPlanIds,
            codes = null,
            amount = 2,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            token = ProtonPaymentToken("test-token"),
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        job.join()

        // THEN
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, expectedCard) }
        coVerify(exactly = 1) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `validate plan success handled correctly`() = coroutinesTest {
        // GIVEN
        val testSubscriptionPlanStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = testCurrency,
            cycle = testSubscriptionCycle,
            gift = null
        )

        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        val job = flowTest(billingViewModel.plansValidationState) {
            // THEN
            assertIs<BillingCommonViewModel.PlansValidationState.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.PlansValidationState.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.PlansValidationState.Success)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscription)
        }

        // WHEN
        billingViewModel.validatePlan(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle)
        job.join()
    }

    @Test
    fun `validate plan error handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            validateSubscription.invoke(
                userId = testUserId,
                codes = null,
                plans = testPlanIds,
                currency = testCurrency,
                cycle = testSubscriptionCycle
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123, message = "http error",
                proton = ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )

        val job = flowTest(billingViewModel.plansValidationState) {
            // THEN
            assertIs<BillingCommonViewModel.PlansValidationState.Idle>(awaitItem())
            assertIs<BillingCommonViewModel.PlansValidationState.Processing>(awaitItem())
            val subscriptionPlanStatus = awaitItem()
            assertTrue(subscriptionPlanStatus is BillingCommonViewModel.PlansValidationState.Error.Message)
            assertEquals("proton error", subscriptionPlanStatus.message)
        }

        // WHEN
        billingViewModel.validatePlan(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle)
        job.join()
    }

    @Test
    fun `observing payment returns proton and GIAP`() = coroutinesTest {
        coEvery { getAvailablePaymentProviders.invoke(refresh = true) } returns setOf(
            PaymentProvider.CardPayment, PaymentProvider.GoogleInAppPurchase, PaymentProvider.PayPal
        )
        coEvery { activePaymentProvider.getActivePaymentProvider() } returns PaymentProvider.GoogleInAppPurchase
        billingViewModel = BillingViewModel(
            activePaymentProvider,
            validateSubscription,
            createPaymentToken,
            performSubscribe,
            getCountryCode,
            humanVerificationManager,
            clientIdProvider,
            observabilityManager,
            userManager
        )

        runCurrent()

        billingViewModel.state.test {
            val state = awaitItem()
            assertTrue(state is BillingViewModel.State.PaymentProvidersSuccess, "Actual state is $state")
            assertEquals(state.activeProvider, PaymentProvider.GoogleInAppPurchase)
        }
    }

    @Test
    fun `observing payment returns none`() = coroutinesTest {
        coEvery { getAvailablePaymentProviders.invoke(refresh = true) } returns emptySet()

        coEvery { activePaymentProvider.getActivePaymentProvider() } returns null
        billingViewModel = BillingViewModel(
            activePaymentProvider,
            validateSubscription,
            createPaymentToken,
            performSubscribe,
            getCountryCode,
            humanVerificationManager,
            clientIdProvider,
            observabilityManager,
            userManager
        )

        runCurrent()

        billingViewModel.state.test {
            val state = awaitItem()
            assertTrue(state is BillingViewModel.State.PaymentProvidersEmpty)
        }
    }

    @Test
    fun `switching payment providers works correctly`() = coroutinesTest {
        coEvery { getAvailablePaymentProviders.invoke(refresh = true) } returns setOf(
            PaymentProvider.CardPayment, PaymentProvider.GoogleInAppPurchase, PaymentProvider.PayPal
        )
        val activePaymentProvider =
            ActivePaymentProviderImpl(getAvailablePaymentProviders)
        billingViewModel = BillingViewModel(
            activePaymentProvider,
            validateSubscription,
            createPaymentToken,
            performSubscribe,
            getCountryCode,
            humanVerificationManager,
            clientIdProvider,
            observabilityManager,
            userManager
        )

        runCurrent()

        billingViewModel.state.test {
            val state = awaitItem()
            assertTrue(state is BillingViewModel.State.PaymentProvidersSuccess)

            assertEquals(state.activeProvider, PaymentProvider.CardPayment)
            assertEquals(state.nextPaymentProviderTextResource, R.string.payment_use_google_pay_instead)

            billingViewModel.switchNextPaymentProvider()
            val stateAfterSwitch = awaitItem()
            assertTrue(stateAfterSwitch is BillingViewModel.State.PaymentProvidersSuccess)

            assertEquals(stateAfterSwitch.activeProvider, PaymentProvider.GoogleInAppPurchase)
            assertEquals(stateAfterSwitch.nextPaymentProviderTextResource, R.string.payment_use_credit_card_instead)
        }
    }
}
