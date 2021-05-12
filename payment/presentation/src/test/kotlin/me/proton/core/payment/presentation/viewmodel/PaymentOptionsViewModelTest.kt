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
import io.mockk.spyk
import io.mockk.verify
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.usecase.GetCountry
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.Details
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.Plan
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithExistingPaymentMethod
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewCreditCard
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewPayPal
import me.proton.core.payment.domain.usecase.GetAvailablePaymentMethods
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentOptionsViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val validateSubscription = mockk<ValidateSubscriptionPlan>()
    private val createPaymentToken = mockk<CreatePaymentTokenWithNewCreditCard>()
    private val createPaymentTokenWithExistingPaymentMethod = mockk<CreatePaymentTokenWithExistingPaymentMethod>()
    private val createPaymentTokenWithNewPayPal = mockk<CreatePaymentTokenWithNewPayPal>()
    private val performSubscribe = mockk<PerformSubscribe>()
    private val getCountryCode = mockk<GetCountry>()
    private val getAvailablePaymentMethods = mockk<GetAvailablePaymentMethods>(relaxed = true)
    private val getCurrentSubscription = mockk<GetCurrentSubscription>(relaxed = true)
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
    private val testSubscription = Subscription(
        id = "test-subscription-id",
        invoiceId = "test-invoice-id",
        cycle = 12,
        periodStart = 1,
        periodEnd = 2,
        couponCode = null,
        currency = "EUR",
        amount = 5,
        plans = listOf(
            Plan(
                "test-plan-id", 1, 12, "test-plan", "test-plan", "EUR",
                2, 1, 1, 1, 1, 1, 1,
                1, 1, 1
            )
        )
    )
    // endregion
    private lateinit var billingViewModel: BillingViewModel
    private lateinit var viewModel: PaymentOptionsViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getCountryCode.invoke(any()) } returns Country(testCCCountry, "test-code-1")

        billingViewModel = BillingViewModel(
            validateSubscription,
            createPaymentToken,
            createPaymentTokenWithNewPayPal,
            createPaymentTokenWithExistingPaymentMethod,
            performSubscribe,
            getCountryCode
        )

        every { context.getString(any()) } returns "test-string"
        coEvery { getCurrentSubscription.invoke(testUserId) } returns testSubscription
        viewModel =
            PaymentOptionsViewModel(
                context,
                getAvailablePaymentMethods,
                getCurrentSubscription,
                validateSubscription,
                createPaymentToken,
                createPaymentTokenWithNewPayPal,
                createPaymentTokenWithExistingPaymentMethod,
                performSubscribe,
                getCountryCode
            )
    }

    @Test
    fun `available payment methods success handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns listOf(
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
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            assertIs<PaymentOptionsViewModel.State.Idle>(expectItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(expectItem())
            val paymentMethodsStatus = expectItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertEquals(2, paymentMethodsStatus.availablePaymentMethods.size)
        }
    }

    @Test
    fun `no available payment methods success handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { getAvailablePaymentMethods.invoke(testUserId) } returns emptyList()
        viewModel.availablePaymentMethodsState.test {
            // WHEN
            viewModel.getAvailablePaymentMethods(testUserId)
            // THEN
            coVerify(exactly = 1) { getCurrentSubscription.invoke(any()) }
            assertIs<PaymentOptionsViewModel.State.Idle>(expectItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(expectItem())
            val paymentMethodsStatus = expectItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Success.PaymentMethodsSuccess)
            assertTrue(paymentMethodsStatus.availablePaymentMethods.isEmpty())
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
            assertIs<PaymentOptionsViewModel.State.Idle>(expectItem())
            assertIs<PaymentOptionsViewModel.State.Processing>(expectItem())
            val paymentMethodsStatus = expectItem()
            assertTrue(paymentMethodsStatus is PaymentOptionsViewModel.State.Error.Message)
            assertEquals("proton error", paymentMethodsStatus.message)
        }
    }

    @Test
    fun `on subscribe checks existing plans`() = coroutinesTest {
        // GIVEN
        val testPlanId = "test-plan-id"
        val paymentType = PaymentType.PaymentMethod("test-payment-method-id")
        val viewModelSpy = spyk(viewModel, recordPrivateCalls = true)
        viewModelSpy.currentPlans = mutableListOf("current-plan-1")
        // WHEN
        viewModelSpy.subscribe(testUserId, testPlanId, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        verify(exactly = 1) {
            billingViewModel.subscribe(
                testUserId,
                listOf("current-plan-1", testPlanId),
                any(),
                testCurrency,
                testSubscriptionCycle,
                paymentType
            )
        }
    }

    @Test
    fun `subscribe pass the call to billing subscribe`() = coroutinesTest {
        // GIVEN
        val testPlanId = "test-plan-id"
        val paymentType = PaymentType.PaymentMethod("test-payment-method-id")
        // WHEN
        viewModel.subscribe(testUserId, testPlanId, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        verify(exactly = 1) {
            billingViewModel.subscribe(
                testUserId,
                listOf("test-plan-id"),
                null,
                testCurrency,
                testSubscriptionCycle,
                paymentType
            )
        }
    }

    @Test
    fun `validate plan pass the call to billing validate plan`() = coroutinesTest {
        // GIVEN
        val testPlanId = "test-plan-id"
        // WHEN
        viewModel.validatePlan(testUserId, testPlanId, null, testCurrency, testSubscriptionCycle)
        // THEN
        verify(exactly = 1) {
            billingViewModel.validatePlan(
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
        val testAmount = 5L
        val testToken = "test-token"
        // WHEN
        viewModel.onThreeDSTokenApproved(
            testUserId,
            testPlanId,
            null,
            testAmount,
            testCurrency,
            testSubscriptionCycle,
            testToken
        )
        // THEN
        verify(exactly = 1) {
            billingViewModel.onThreeDSTokenApproved(
                testUserId,
                listOf("test-plan-id"),
                null,
                testAmount,
                testCurrency,
                testSubscriptionCycle,
                testToken
            )
        }
    }
}
