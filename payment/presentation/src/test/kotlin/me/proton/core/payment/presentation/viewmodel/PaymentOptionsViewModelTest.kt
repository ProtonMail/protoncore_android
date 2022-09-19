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

package me.proton.core.payment.presentation.viewmodel

import android.content.Context
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.usecase.GetCountry
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.Details
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithExistingPaymentMethod
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithGoogleIAP
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewCreditCard
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewPayPal
import me.proton.core.payment.domain.usecase.GetAvailablePaymentMethods
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.plan.domain.entity.PLAN_PRODUCT
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentOptionsViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val validateSubscription = mockk<ValidateSubscriptionPlan>(relaxed = true)
    private val getCountryCode = mockk<GetCountry>(relaxed = true)
    private val getAvailablePaymentMethods = mockk<GetAvailablePaymentMethods>(relaxed = true)
    private val getAvailablePaymentProviders = mockk<GetAvailablePaymentProviders>()
    private val getCurrentSubscription = mockk<GetCurrentSubscription>(relaxed = true)
    private val createPaymentToken = mockk<CreatePaymentTokenWithNewCreditCard>()
    private val createPaymentTokenWithExistingPayMethod = mockk<CreatePaymentTokenWithExistingPaymentMethod>()
    private val createPaymentTokenWithNewPayPal = mockk<CreatePaymentTokenWithNewPayPal>()
    private val createPaymentTokenWithGoogleIAP = mockk<CreatePaymentTokenWithGoogleIAP>()
    private val performSubscribe = mockk<PerformSubscribe>()
    private val humanVerificationManager = mockk<HumanVerificationManager>(relaxed = true)
    private val clientIdProvider = mockk<ClientIdProvider>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    // endregion

    // region test data
    private val testCCCountry = "test-country"
    private val testUserId = UserId("test-user-id")
    private val testCurrency = Currency.CHF
    private val testSubscriptionCycle = SubscriptionCycle.YEARLY
    private val testReadOnlyCard = Card.CardReadOnly(
        brand = "visa", last4 = "1234", expirationMonth = "01",
        expirationYear = "2021", name = "Test", country = "Test Country", zip = "123"
    )
    private val testPaymentMethodsList = listOf(
        PaymentMethod(
            "1",
            PaymentMethodType.CARD,
            Details.CardDetails(testReadOnlyCard)
        ),
        PaymentMethod(
            "2",
            PaymentMethodType.PAYPAL,
            Details.PayPalDetails(
                billingAgreementId = "3",
                payer = "test payer"
            )
        )
    )
    private val testSubscribedPlan = Plan(
        "test-subscribed-plan-id", 1, 12, "test-subscribed-plan-name", "test-plan", "EUR",
        2, 1, 1, 1, 2, 1, 1, 1, 1,
        1, 1, 1, true
    )
    private val testSubscription = Subscription(
        id = "test-subscription-id",
        invoiceId = "test-invoice-id",
        cycle = 12,
        periodStart = 1,
        periodEnd = 2,
        couponCode = null,
        currency = "EUR",
        amount = 5,
        plans = listOf(testSubscribedPlan),
        external = SubscriptionManagement.PROTON_MANAGED,
        customerId = null
    )

    // endregion
    private lateinit var viewModel: PaymentOptionsViewModel
    private val subscriptionStatus = SubscriptionStatus(
        1, 1, 1, 0, null, 0, Currency.EUR, SubscriptionCycle.YEARLY, null
    )

    @Before
    fun beforeEveryTest() {
        coEvery { getCountryCode.invoke(any()) } returns Country(testCCCountry, "test-code-1")

        coEvery { validateSubscription.invoke(any(), any(), any(), any(), any()) } returns subscriptionStatus

        every { context.getString(any()) } returns "test-string"
        coEvery { getCurrentSubscription.invoke(testUserId) } returns testSubscription
        coEvery { getAvailablePaymentProviders.invoke() } returns PaymentProvider.values().toSet()
        viewModel =
            PaymentOptionsViewModel(
                context,
                getAvailablePaymentMethods,
                getAvailablePaymentProviders,
                getCurrentSubscription,
                validateSubscription,
                createPaymentToken,
                createPaymentTokenWithNewPayPal,
                createPaymentTokenWithExistingPayMethod,
                createPaymentTokenWithGoogleIAP,
                performSubscribe,
                getCountryCode,
                humanVerificationManager,
                clientIdProvider
            )
    }

    @Test
    fun `available payment methods success handled correctly`() = coroutinesTest {
        // GIVEN
        every { context.getString(any()) } returns "google"
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns testPaymentMethodsList
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            assertIs<PaymentOptionsViewModel.State.Idle>(awaitItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(awaitItem())
            val paymentMethodsStatus = awaitItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertEquals(3, paymentMethodsStatus.availablePaymentMethods.size)
            assertEquals("google", paymentMethodsStatus.availablePaymentMethods[2].id)
        }
    }

    @Test
    fun `available payment methods no IAP provider success handled correctly`() = coroutinesTest {
        // GIVEN
        every { context.getString(any()) } returns "google"
        coEvery { getAvailablePaymentProviders.invoke(refresh = true) } returns setOf(PaymentProvider.CardPayment)
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns testPaymentMethodsList
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            assertIs<PaymentOptionsViewModel.State.Idle>(awaitItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(awaitItem())
            val paymentMethodsStatus = awaitItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertEquals(3, paymentMethodsStatus.availablePaymentMethods.size)
            assertEquals("google", paymentMethodsStatus.availablePaymentMethods[2].id)
        }
    }

    @Test
    fun `no available payment methods and no providers success handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders.invoke() } returns emptySet()
        viewModel =
            PaymentOptionsViewModel(
                context,
                getAvailablePaymentMethods,
                getAvailablePaymentProviders,
                getCurrentSubscription,
                validateSubscription,
                createPaymentToken,
                createPaymentTokenWithNewPayPal,
                createPaymentTokenWithExistingPayMethod,
                createPaymentTokenWithGoogleIAP,
                performSubscribe,
                getCountryCode,
                humanVerificationManager,
                clientIdProvider
            )
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns emptyList()
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            coVerify(exactly = 1) { getCurrentSubscription.invoke(any()) }
            assertIs<PaymentOptionsViewModel.State.Idle>(awaitItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(awaitItem())
            val paymentMethodsStatus = awaitItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertTrue(paymentMethodsStatus.availablePaymentMethods.isEmpty())
        }
    }

    @Test
    fun `no available payment methods and card provider success handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders.invoke() } returns setOf(PaymentProvider.CardPayment)
        viewModel =
            PaymentOptionsViewModel(
                context,
                getAvailablePaymentMethods,
                getAvailablePaymentProviders,
                getCurrentSubscription,
                validateSubscription,
                createPaymentToken,
                createPaymentTokenWithNewPayPal,
                createPaymentTokenWithExistingPayMethod,
                createPaymentTokenWithGoogleIAP,
                performSubscribe,
                getCountryCode,
                humanVerificationManager,
                clientIdProvider
            )
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns emptyList()
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            coVerify(exactly = 1) { getCurrentSubscription.invoke(any()) }
            assertIs<PaymentOptionsViewModel.State.Idle>(awaitItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(awaitItem())
            val paymentMethodsStatus = awaitItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertTrue(paymentMethodsStatus.availablePaymentMethods.isEmpty())
        }
    }

    @Test
    fun `no available payment methods and card and IAP provider success handled correctly`() = coroutinesTest {
        // GIVEN
        every { context.getString(any()) } returns "google"
        coEvery { getAvailablePaymentProviders.invoke() } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase
        )
        viewModel =
            PaymentOptionsViewModel(
                context,
                getAvailablePaymentMethods,
                getAvailablePaymentProviders,
                getCurrentSubscription,
                validateSubscription,
                createPaymentToken,
                createPaymentTokenWithNewPayPal,
                createPaymentTokenWithExistingPayMethod,
                createPaymentTokenWithGoogleIAP,
                performSubscribe,
                getCountryCode,
                humanVerificationManager,
                clientIdProvider
            )
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns emptyList()
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            coVerify(exactly = 1) { getCurrentSubscription.invoke(any()) }
            assertIs<PaymentOptionsViewModel.State.Idle>(awaitItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(awaitItem())
            val paymentMethodsStatus = awaitItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertEquals(1, paymentMethodsStatus.availablePaymentMethods.size)
            assertEquals("google", paymentMethodsStatus.availablePaymentMethods[0].id)
        }
    }

    @Test
    fun `available payment methods error handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            assertIs<PaymentOptionsViewModel.State.Idle>(awaitItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(awaitItem())
            val paymentMethodsStatus = awaitItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Error.General)
            assertEquals("proton error", paymentMethodsStatus.error.getUserMessage(mockk()))
        }
    }

    @Test
    fun `validate plan pass the call to billing validate plan`() = coroutinesTest {
        // GIVEN
        val testPlanId = "test-plan-id"
        val testPlanServices = 1
        val testPlanType = PLAN_PRODUCT
        // WHEN
        viewModel.validatePlan(
            testUserId,
            testPlanId,
            testPlanServices,
            testPlanType,
            null,
            testCurrency,
            testSubscriptionCycle
        )
        // THEN
        verify(exactly = 1) {
            viewModel.validatePlan(
                testUserId,
                listOf("test-plan-id"),
                null,
                testCurrency,
                testSubscriptionCycle
            )
        }
    }

    @Test
    fun `on3dsapproved pass the call to billing on3dsapproved`() = coroutinesTest {
        // GIVEN
        val testPlanId = "test-plan-id"
        val testPlanServices = 1
        val testPlanType = PLAN_PRODUCT
        val testAmount = 5L
        val testToken = ProtonPaymentToken("test-token")
        // WHEN
        viewModel.onThreeDSTokenApproved(
            testUserId,
            testPlanId,
            testPlanServices,
            testPlanType,
            null,
            testAmount,
            testCurrency,
            testSubscriptionCycle,
            testToken,
            SubscriptionManagement.PROTON_MANAGED
        )
        // THEN
        verify(exactly = 1) {
            viewModel.onThreeDSTokenApproved(
                testUserId,
                listOf("test-plan-id"),
                null,
                testAmount,
                testCurrency,
                testSubscriptionCycle,
                testToken,
                SubscriptionManagement.PROTON_MANAGED
            )
        }
    }
}
