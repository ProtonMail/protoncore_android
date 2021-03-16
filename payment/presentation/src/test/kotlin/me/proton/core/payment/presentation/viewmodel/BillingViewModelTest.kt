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

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.country.domain.entity.Country
import me.proton.core.country.domain.usecase.GetCountry
import me.proton.core.country.domain.usecase.LoadCountries
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithExistingPaymentMethod
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewCreditCard
import me.proton.core.payment.domain.usecase.CreatePaymentTokenWithNewPayPal
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
    private val getCountryCode = mockk<GetCountry>()
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
        coEvery { getCountryCode.invoke(any()) } returns Country(testCCCountry, "test-code-1")

        viewModel = BillingViewModel(
            validateSubscription,
            createPaymentToken,
            createPaymentTokenWithNewPayPal,
            createPaymentTokenWithExistingPaymentMethod,
            performSubscribe,
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

        viewModel.subscriptionResult.test {

            // WHEN
            viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)

            // THEN
            coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
            coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }

            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.TokenCreated>(expectItem())
            assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(expectItem())
        }

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

        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(null, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)

            // THEN
            coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, paymentType) }
            coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.TokenCreated>(expectItem())
            assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(expectItem())
        }
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
        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
            // THEN
            coVerify(exactly = 1) {
                createPaymentTokenWithExistingPaymentMethod.invoke(
                    testUserId,
                    2,
                    testCurrency,
                    testPaymentMethodId
                )
            }
            coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.TokenCreated>(expectItem())
            assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(expectItem())
        }
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

        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(null, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
            // THEN
            coVerify(exactly = 0) {
                createPaymentTokenWithExistingPaymentMethod.invoke(any(), any(), any(), any())
            }
            coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Error.SignUpWithPaymentMethodUnsupported>(expectItem())
        }
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

        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
            // THEN
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
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.SubscriptionCreated>(expectItem())
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
        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
            // THEN
            coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
            coVerify(exactly = 1) {
                performSubscribe.invoke(
                    testUserId, 2, testCurrency, testSubscriptionCycle,
                    testPlanIds, null, "test-token"
                )
            }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.TokenCreated>(expectItem())
            assertIs<BillingViewModel.State.Success.SubscriptionCreated>(expectItem())
        }
    }

    @Test
    fun `upgrade subscription plan error handled properly`() = coroutinesTest {
        // GIVEN
        val paymentType = PaymentType.CreditCard(testCard)

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

        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)

            // THEN
            coVerify(exactly = 0) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
            coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Error.Message)
            assertEquals("proton error", subscriptionPlanStatus.message)
        }
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

        viewModel.subscriptionResult.test {
            // WHEN
            viewModel.subscribe(null, testPlanIds, null, testCurrency, testSubscriptionCycle, paymentType)
            viewModel.onThreeDSTokenApproved(
                null, testPlanIds, null, 2, testCurrency, testSubscriptionCycle, "test-token"
            )

            // THEN
            coVerify(exactly = 1) { createPaymentToken.invoke(null, 2, testCurrency, paymentType) }
            coVerify(exactly = 0) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.TokenCreated>(expectItem())
            assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(expectItem())
            assertIs<BillingViewModel.State.Success.SignUpTokenReady>(expectItem())
        }
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

        viewModel.subscriptionResult.test {
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
            coVerify(exactly = 1) { createPaymentToken.invoke(testUserId, 2, testCurrency, paymentType) }
            coVerify(exactly = 1) { performSubscribe.invoke(any(), any(), any(), any(), any(), any(), any()) }
            assertIs<BillingViewModel.State.Idle>(expectItem())
            assertIs<BillingViewModel.State.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.State.Success.SubscriptionPlanValidated)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscriptionStatus)
            assertIs<BillingViewModel.State.Success.TokenCreated>(expectItem())
            assertIs<BillingViewModel.State.Incomplete.TokenApprovalNeeded>(expectItem())
            assertIs<BillingViewModel.State.Success.SubscriptionCreated>(expectItem())
        }
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
                testUserId,
                null,
                testPlanIds,
                testCurrency,
                testSubscriptionCycle
            )
        } returns testSubscriptionPlanStatus

        viewModel.plansValidationState.test {
            // WHEN
            viewModel.validatePlan(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle)
            // THEN
            assertIs<BillingViewModel.PlansValidationState.Idle>(expectItem())
            assertIs<BillingViewModel.PlansValidationState.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.PlansValidationState.Success)
            assertEquals(testSubscriptionPlanStatus, subscriptionPlanStatus.subscription)
        }
    }

    @Test
    fun `validate plan error handled correctly`() = coroutinesTest {
        // GIVEN
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

        viewModel.plansValidationState.test {
            // WHEN
            viewModel.validatePlan(testUserId, testPlanIds, null, testCurrency, testSubscriptionCycle)
            // THEN
            assertIs<BillingViewModel.PlansValidationState.Idle>(expectItem())
            assertIs<BillingViewModel.PlansValidationState.Processing>(expectItem())
            val subscriptionPlanStatus = expectItem()
            assertTrue(subscriptionPlanStatus is BillingViewModel.PlansValidationState.Error.Message)
            assertEquals("proton error", subscriptionPlanStatus.message)
        }
    }
}
