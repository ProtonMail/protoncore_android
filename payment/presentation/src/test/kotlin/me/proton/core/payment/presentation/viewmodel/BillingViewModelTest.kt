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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Country
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithExistingPaymentMethod
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewCreditCard
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewPayPal
import me.proton.core.payment.domain.usecase.GetCountries
import me.proton.core.payment.domain.usecase.GetCountryCode
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.payment.domain.usecase.ValidateSubscriptionPlan
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BillingViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val validateSubscription = mockk<ValidateSubscriptionPlan>()
    private val createPaymentToken = mockk<CreatePaymentTokenWithNewCreditCard>()
    private val createPaymentTokenWithExistingPaymentMethod = mockk<CreatePaymentTokenWithExistingPaymentMethod>()
    private val createPaymentTokenWithNewPayPal = mockk<CreatePaymentTokenWithNewPayPal>()
    private val performSubscribe = mockk<PerformSubscribe>()
    private val getCountries = mockk<GetCountries>()
    private val getCountryCode = mockk<GetCountryCode>()
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

    private lateinit var viewModel: BillingViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getCountries.invoke() } returns
            listOf(Country("test-country-1", "test-code-1"), Country("test-country-2", "test-code-2"))
        every { getCountryCode.invoke(any()) } returns testCCCountry

        viewModel = BillingViewModel(
            validateSubscription,
            createPaymentToken,
            createPaymentTokenWithNewPayPal,
            createPaymentTokenWithExistingPaymentMethod,
            performSubscribe,
            getCountries,
            getCountryCode
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

        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

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
                paymentType
            )
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", "test-token", "test-return-host"
        )

        // WHEN
        viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)

        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 4) { observer(capture(arguments)) }
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.TokenCreated>(arguments[2])
        assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(arguments[3])
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
        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)
        coEvery {
            validateSubscription.invoke(
                null,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus
        coEvery {
            createPaymentToken.invoke(
                null,
                2,
                testCurrency,
                paymentType
            )
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", "test-token", "test-return-host"
        )
        // WHEN
        viewModel.subscribe(null, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 4) { observer(capture(arguments)) }
        coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, paymentType) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.TokenCreated>(arguments[2])
        assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(arguments[3])
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
        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

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
            createPaymentTokenWithExistingPaymentMethod.invoke(
                testUserId,
                2,
                testCurrency,
                testPaymentMethodId
            )
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", "test-token", "test-return-host"
        )
        // WHEN
        viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 4) { observer(capture(arguments)) }
        coVerify(exactly = 1) {
            createPaymentTokenWithExistingPaymentMethod.invoke(
                testUserId,
                2,
                testCurrency,
                testPaymentMethodId
            )
        }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.TokenCreated>(arguments[2])
        assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(arguments[3])
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
        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

        coEvery {
            validateSubscription.invoke(
                null,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentTokenWithExistingPaymentMethod.invoke(
                null,
                2,
                testCurrency,
                testPaymentMethodId
            )
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", "test-token", "test-return-host"
        )
        // WHEN
        viewModel.subscribe(null, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 3) { observer(capture(arguments)) }
        coVerify(exactly = 0) {
            createPaymentTokenWithExistingPaymentMethod.invoke(any(), any(), any(), any())
        }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Error.SignUpWithPaymentMethodUnsupported>(arguments[2])
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
        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

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
            performSubscribe.invoke(
                testUserId, 0, testCurrency, testSubscriptionCycle, testPlanIds, null, null
            )
        } returns mockk()
        // WHEN
        viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 3) { observer(capture(arguments)) }
        coVerify(exactly = 0) {
            createPaymentTokenWithExistingPaymentMethod.invoke(any(), any(), any(), any())
        }
        coVerify(exactly = 1) {
            performSubscribe.invoke(
                testUserId,
                0,
                testCurrency,
                testSubscriptionCycle,
                testPlanIds,
                null,
                null
            )
        }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.SubscriptionCreated>(arguments[2])
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
        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

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
                paymentType
            )
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE, "test-approval-url", "test-token", "test-return-host"
        )

        coEvery {
            performSubscribe.invoke(
                testUserId, 2, testCurrency, testSubscriptionCycle, testPlanIds, null, "test-token"
            )
        } returns mockk()
        // WHEN
        viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 4) { observer(capture(arguments)) }
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
        coVerify(exactly = 1) {
            performSubscribe.invoke(
                testUserId, 2, testCurrency, testSubscriptionCycle,
                testPlanIds, null, "test-token"
            )
        }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.TokenCreated>(arguments[2])
        assertIs<BillingViewModel.State.Success.SubscriptionCreated>(arguments[3])
    }

    @Test
    fun `upgrade subscription plan error handled properly`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)
        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

        coEvery {
            validateSubscription.invoke(
                testUserId,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
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
        // WHEN
        viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)

        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        coVerify(exactly = 0) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Error.Message)
        assertEquals("proton error", subscriptionPlanStatus.message)
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

        val subscriptionObserver = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(subscriptionObserver)

        coEvery {
            validateSubscription.invoke(
                null,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus
        coEvery {
            createPaymentToken.invoke(
                null,
                2,
                testCurrency,
                paymentType
            )
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", "test-token", "test-return-host"
        )
        // WHEN
        viewModel.subscribe(null, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        viewModel.onThreeDSTokenApproved(
            null, testPlanIds, null, 2, testCurrency, testSubscriptionCycle, "test-token"
        )

        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 5) { subscriptionObserver(capture(arguments)) }
        coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, paymentType) }
        coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.TokenCreated>(arguments[2])
        assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(arguments[3])
        assertIs<BillingViewModel.State.Success.SignUpTokenReady>(arguments[4])
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

        val observer = mockk<(BillingViewModel.State) -> Unit>(relaxed = true)
        viewModel.subscriptionResult.observeDataForever(observer)

        coEvery {
            validateSubscription.invoke(testUserId, null, testPlanIds, testCurrency, testSubscriptionCycle)
        } returns testSubscriptionPlanStatus

        coEvery {
            createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType)
        } returns PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", "test-token", "test-return-host"
        )

        coEvery {
            performSubscribe.invoke(
                testUserId, 2, testCurrency, testSubscriptionCycle, testPlanIds, null, "test-token"
            )
        } returns mockk()

        // WHEN
        viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
        viewModel.onThreeDSTokenApproved(
            testUserId,
            testPlanIds,
            null,
            2,
            testCurrency,
            testSubscriptionCycle,
            "test-token"
        )
        // THEN
        val arguments = mutableListOf<BillingViewModel.State>()
        verify(exactly = 5) { observer(capture(arguments)) }
        coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
        coVerify(exactly = 1) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
        assertIs<BillingViewModel.State.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
        assertIs<BillingViewModel.State.Success.TokenCreated>(arguments[2])
        assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(arguments[3])
        assertIs<BillingViewModel.State.Success.SubscriptionCreated>(arguments[4])
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

        val observer = mockk<(BillingViewModel.PlansValidationState) -> Unit>(relaxed = true)
        viewModel.plansValidationState.observeDataForever(observer)

        coEvery {
            validateSubscription.invoke(
                testUserId,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        // WHEN
        viewModel.validatePlan(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle)
        // THEN
        val arguments = mutableListOf<BillingViewModel.PlansValidationState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<BillingViewModel.PlansValidationState.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.PlansValidationState.Success)
        assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscription)
    }

    @Test
    fun `validate plan error handled correctly`() = coroutinesTest {
        // GIVEN
        val observer = mockk<(BillingViewModel.PlansValidationState) -> Unit>(relaxed = true)
        viewModel.plansValidationState.observeDataForever(observer)

        coEvery {
            validateSubscription.invoke(
                testUserId,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
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

        // WHEN
        viewModel.validatePlan(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle)
        // THEN
        val arguments = mutableListOf<BillingViewModel.PlansValidationState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<BillingViewModel.PlansValidationState.Processing>(arguments[0])
        val subscriptionPlanStatus = arguments[1]
        assertTrue(subscriptionPlanStatus is BillingViewModel.PlansValidationState.Error.Message)
        assertEquals("proton error", subscriptionPlanStatus.message)
    }

    @Test
    fun `countries success`() = coroutinesTest {
        val testCountries = listOf(Country("test-country-1", "test-code-1"))

        coEvery { getCountries.invoke() } returns testCountries

        viewModel = BillingViewModel(
            validateSubscription,
            createPaymentToken,
            createPaymentTokenWithNewPayPal,
            createPaymentTokenWithExistingPaymentMethod,
            performSubscribe,
            getCountries,
            getCountryCode
        )

        val observer = mockk<(List<Country>) -> Unit>(relaxed = true)
        viewModel.countriesResult.observeDataForever(observer)

        val arguments = mutableListOf<List<Country>>()
        verify(exactly = 1) { observer(capture(arguments)) }
        val countriesResult = arguments[0]
        assertEquals(testCountries, countriesResult)
        assertEquals(1, countriesResult.size)
    }

    @Test
    fun `countries error`() = coroutinesTest {
        coEvery { getCountries.invoke() } throws RuntimeException("test-exception")

        viewModel = BillingViewModel(
            validateSubscription,
            createPaymentToken,
            createPaymentTokenWithNewPayPal,
            createPaymentTokenWithExistingPaymentMethod,
            performSubscribe,
            getCountries,
            getCountryCode
        )

        val observer = mockk<(List<Country>) -> Unit>(relaxed = true)
        viewModel.countriesResult.observeDataForever(observer)

        val arguments = mutableListOf<List<Country>>()
        verify(exactly = 1) { observer(capture(arguments)) }

    }
}
