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

package me.proton.core.payment.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.payment.data.api.PaymentsApi
import me.proton.core.payment.data.entity.dynamicSubscription
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.Details
import me.proton.core.payment.domain.entity.DynamicSubscription
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.plan.domain.PlanIconsEndpointProvider
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.runTestWithResultContext
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PaymentsRepositoryImplTest {

    // region mocks
    private val endpointProvider = mockk<PlanIconsEndpointProvider> {
        every { get() } returns "endpoint"
    }
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiManagerFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val apiManager = mockk<ApiManager<PaymentsApi>>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: PaymentsRepositoryImpl
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    private val testReadOnlyCard = Card.CardReadOnly(
        brand = "visa", last4 = "1234", expirationMonth = "01",
        expirationYear = "2021", name = "Test", country = "Test Country", zip = "123"
    )
    private val testCardWithPaymentDetails = Card.CardWithPaymentDetails(
        number = "123456789", cvc = "123",
        expirationMonth = "01", expirationYear = "2021", name = "Test", country = "Test Country", zip = "123"
    )
    // endregion

    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { sessionProvider.getSessionId(UserId(testUserId)) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiManagerFactory, sessionProvider, dispatcherProvider)
        every {
            apiManagerFactory.create(
                interfaceClass = PaymentsApi::class
            )
        } returns apiManager
        every {
            apiManagerFactory.create(
                SessionId(testSessionId),
                interfaceClass = PaymentsApi::class
            )
        } returns apiManager
        repository = PaymentsRepositoryImpl(apiProvider, endpointProvider)
    }

    @Test
    fun `payment methods return success single card`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val paymentMethods = listOf(
            PaymentMethod(
                "1",
                PaymentMethodType.CARD,
                Details.CardDetails(
                    Card.CardReadOnly(
                        brand = "visa", last4 = "1234", expirationMonth = "01",
                        expirationYear = "2021", name = "Test",
                        country = "Test Country", zip = "123"
                    )
                )
            )
        )
        coEvery { apiManager.invoke<List<PaymentMethod>>(any()) } returns ApiResult.Success(paymentMethods)
        // WHEN
        val paymentMethodsResponse = repository.getAvailablePaymentMethods(sessionUserId = SessionUserId(testUserId))
        // THEN
        assertNotNull(paymentMethodsResponse)
        assertEquals(1, paymentMethodsResponse.size)
        val method1 = paymentMethods[0]
        assertEquals("1", method1.id)
        assertEquals(PaymentMethodType.CARD, method1.type)
        assertIs<Details.CardDetails>(method1.details)
        assertTrue(assertSingleResult("getAvailablePaymentMethods").isSuccess)
    }

    @Test
    fun `payment methods return success card and paypal`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val paymentMethods = listOf(
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
        coEvery { apiManager.invoke<List<PaymentMethod>>(any()) } returns ApiResult.Success(paymentMethods)
        // WHEN
        val paymentMethodsResponse = repository.getAvailablePaymentMethods(sessionUserId = SessionUserId(testUserId))
        // THEN
        assertNotNull(paymentMethodsResponse)
        assertEquals(2, paymentMethodsResponse.size)
        val method1 = paymentMethods[0]
        assertEquals("1", method1.id)
        assertEquals(PaymentMethodType.CARD, method1.type)
        assertIs<Details.CardDetails>(method1.details)
        val method2 = paymentMethods[1]
        assertEquals("2", method2.id)
        assertEquals(PaymentMethodType.PAYPAL, method2.type)
        assertIs<Details.PayPalDetails>(method2.details)
        assertTrue(assertSingleResult("getAvailablePaymentMethods").isSuccess)
    }

    @Test
    fun `payment methods return error`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<List<PaymentMethod>>(any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.getAvailablePaymentMethods(sessionUserId = SessionUserId(testUserId))
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
        assertTrue(assertSingleResult("getAvailablePaymentMethods").isFailure)
    }

    @Test
    fun `payment token status success pending`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val testPaymentToken = ProtonPaymentToken("test-payment-token")
        coEvery {
            apiManager.invoke<PaymentTokenResult.PaymentTokenStatusResult>(any())
        } returns ApiResult.Success(
            PaymentTokenResult.PaymentTokenStatusResult(PaymentTokenStatus.PENDING)
        )
        // WHEN
        val paymentStatusResponse = repository.getPaymentTokenStatus(
            sessionUserId = SessionUserId(testUserId), testPaymentToken
        )
        // THEN
        assertNotNull(paymentStatusResponse)
        assertEquals(PaymentTokenStatus.PENDING, paymentStatusResponse.status)
    }

    @Test
    fun `payment token status success chargeable`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val testPaymentToken = ProtonPaymentToken("test-payment-token")
        coEvery {
            apiManager.invoke<PaymentTokenResult.PaymentTokenStatusResult>(any())
        } returns ApiResult.Success(
            PaymentTokenResult.PaymentTokenStatusResult(PaymentTokenStatus.CHARGEABLE)
        )
        // WHEN
        val paymentStatusResponse = repository.getPaymentTokenStatus(
            sessionUserId = SessionUserId(testUserId), testPaymentToken
        )
        // THEN
        assertNotNull(paymentStatusResponse)
        assertEquals(PaymentTokenStatus.CHARGEABLE, paymentStatusResponse.status)
    }

    @Test
    fun `payment token status error`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val testPaymentToken = ProtonPaymentToken("test-payment-token")
        coEvery {
            apiManager.invoke<PaymentTokenResult.PaymentTokenStatusResult>(any())
        } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.getPaymentTokenStatus(
                sessionUserId = SessionUserId(testUserId), testPaymentToken
            )
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `create payment token logged in new credit card success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val createTokenResult = PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Success(createTokenResult)
        // WHEN
        val createPaymentTokenResult = repository.createPaymentToken(
            sessionUserId = SessionUserId(testUserId),
            amount = 1L,
            currency = Currency.EUR,
            paymentType = PaymentType.CreditCard(testCardWithPaymentDetails)
        )
        // THEN
        assertNotNull(createPaymentTokenResult)
        assertEquals(PaymentTokenStatus.PENDING, createPaymentTokenResult.status)
        assertEquals(ProtonPaymentToken("test-token"), createPaymentTokenResult.token)
        assertTrue(assertSingleResult("createPaymentToken").isSuccess)
    }

    @Test
    fun `create payment token sign up new credit card success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val createTokenResult = PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Success(createTokenResult)
        // WHEN
        val createPaymentTokenResult = repository.createPaymentToken(
            sessionUserId = null,
            amount = 1L,
            currency = Currency.EUR,
            paymentType = PaymentType.CreditCard(testCardWithPaymentDetails)
        )
        // THEN
        assertNotNull(createPaymentTokenResult)
        assertEquals(PaymentTokenStatus.PENDING, createPaymentTokenResult.status)
        assertEquals(ProtonPaymentToken("test-token"), createPaymentTokenResult.token)
        assertTrue(assertSingleResult("createPaymentToken").isSuccess)
    }

    @Test
    fun `create payment token logged in new credit card error`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Error.Http(
                httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
            )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.createPaymentToken(
                sessionUserId = SessionUserId(testUserId),
                amount = 1L,
                currency = Currency.EUR,
                paymentType = PaymentType.CreditCard(testCardWithPaymentDetails)
            )
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
        assertTrue(assertSingleResult("createPaymentToken").isFailure)
    }

    @Test
    fun `create payment token giap success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val createTokenResult = PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Success(createTokenResult)
        // WHEN
        val createPaymentTokenResult = repository.createPaymentToken(
            sessionUserId = SessionUserId(testUserId),
            amount = 1L,
            currency = Currency.EUR,
            paymentType = PaymentType.GoogleIAP(
                productId = "productId",
                purchaseToken = GooglePurchaseToken("token"),
                orderId = "orderId",
                packageName = "packageName",
                customerId = "customerID"
            )
        )
        // THEN
        assertNotNull(createPaymentTokenResult)
        assertEquals(PaymentTokenStatus.PENDING, createPaymentTokenResult.status)
        assertEquals(ProtonPaymentToken("test-token"), createPaymentTokenResult.token)
        assertTrue(assertSingleResult("createPaymentToken").isSuccess)
    }

    @Test
    fun `create payment token giap error`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Error.Http(
                httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
            )
        // WHEN
        assertFailsWith(ApiException::class) {
            repository.createPaymentToken(
                sessionUserId = SessionUserId(testUserId),
                amount = 1L,
                currency = Currency.EUR,
                paymentType = PaymentType.GoogleIAP(
                    productId = "productId",
                    purchaseToken = GooglePurchaseToken("token"),
                    orderId = "orderId",
                    packageName = "packageName",
                    customerId = "customerID"
                )
            )
        }
        // THEN
        assertTrue(assertSingleResult("createPaymentToken").isFailure)
    }

    @Test
    fun `create payment token paypal success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val createTokenResult = PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.PENDING, "test-approval-url", ProtonPaymentToken("test-token"), "test-return-host"
        )
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Success(createTokenResult)
        // WHEN
        val createPaymentTokenResult = repository.createPaymentToken(
            sessionUserId = SessionUserId(testUserId),
            amount = 1L,
            currency = Currency.EUR,
            paymentType = PaymentType.PayPal
        )
        // THEN
        assertNotNull(createPaymentTokenResult)
        assertEquals(PaymentTokenStatus.PENDING, createPaymentTokenResult.status)
        assertEquals(ProtonPaymentToken("test-token"), createPaymentTokenResult.token)
        assertTrue(assertSingleResult("createPaymentToken").isSuccess)
    }

    @Test
    fun `create payment token paypal error`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<PaymentTokenResult.CreatePaymentTokenResult>(any()) } returns
            ApiResult.Error.Http(
                httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
            )
        // WHEN
        assertFailsWith(ApiException::class) {
            repository.createPaymentToken(
                sessionUserId = SessionUserId(testUserId),
                amount = 1L,
                currency = Currency.EUR,
                paymentType = PaymentType.PayPal
            )
        }
        // THEN
        assertTrue(assertSingleResult("createPaymentToken").isFailure)
    }

    @Test
    fun `validate subscription returns success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val subscriptionStatus = SubscriptionStatus(
            amount = 5,
            amountDue = 2,
            proration = 0,
            couponDiscount = 0,
            coupon = null,
            credit = 3,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            gift = null
        )
        coEvery { apiManager.invoke<SubscriptionStatus>(any()) } returns ApiResult.Success(subscriptionStatus)
        // WHEN
        val validationResult = repository.validateSubscription(
            sessionUserId = SessionUserId(testUserId),
            plans = mapOf("test-plan-id" to 1),
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY
        )
        // THEN
        assertNotNull(validationResult)
        assertTrue(assertSingleResult("validateSubscription").isSuccess)
    }

    @Test
    fun `get subscription returns success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<List<DynamicSubscription>>(any()) } returns ApiResult.Success(
            listOf(
                dynamicSubscription
            )
        )
        // WHEN
        val result = repository.getDynamicSubscriptions(SessionUserId(testUserId))
        assertNotNull(result)
        assertTrue(assertSingleResult("getDynamicSubscriptions").isSuccess)
    }

    @Test
    fun `validate subscription returns error`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<SubscriptionStatus>(any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.validateSubscription(
                sessionUserId = SessionUserId(testUserId),
                plans = mapOf("test-plan-id" to 1),
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY
            )
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
        assertTrue(assertSingleResult("validateSubscription").isFailure)
    }

    @Test
    fun `create subscription returns success`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        val subscription = Subscription(
            id = "test-subscription-id",
            invoiceId = "test-invoice-id",
            cycle = 12,
            periodStart = 1L,
            periodEnd = 2L,
            couponCode = null,
            currency = "EUR",
            amount = 5,
            discount = 0,
            renewDiscount = 0,
            renewAmount = 5,
            plans = listOf(mockk()),
            external = SubscriptionManagement.PROTON_MANAGED,
            customerId = null
        )
        coEvery { apiManager.invoke<Subscription>(any()) } returns ApiResult.Success(subscription)
        // WHEN
        val createSubscriptionResult = repository.createOrUpdateSubscription(
            sessionUserId = SessionUserId(testUserId),
            amount = 1,
            currency = Currency.CHF,
            codes = null,
            plans = mapOf("test-plan-id" to 1),
            cycle = SubscriptionCycle.YEARLY,
            payment = PaymentTokenEntity(ProtonPaymentToken("test-token-id")),
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        // THEN
        assertNotNull(createSubscriptionResult)
        assertTrue(assertSingleResult("createOrUpdateSubscription").isSuccess)
    }

    @Test
    fun `create subscription returns error`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<Subscription>(any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.createOrUpdateSubscription(
                sessionUserId = SessionUserId(testUserId),
                amount = 1,
                currency = Currency.CHF,
                codes = null,
                plans = mapOf("test-plan-id" to 1),
                cycle = SubscriptionCycle.YEARLY,
                payment = PaymentTokenEntity(ProtonPaymentToken("test-token-id")),
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
        assertTrue(assertSingleResult("createOrUpdateSubscription").isFailure)
    }

    @Test
    fun `payment status success google play`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<PaymentStatus>(any()) } returns ApiResult.Success(
            PaymentStatus(
                card = true,
                inApp = true,
                paypal = true
            )
        )
        // WHEN
        val paymentStatusResponse = repository.getPaymentStatus(SessionUserId(testUserId), AppStore.GooglePlay)
        // THEN
        assertNotNull(paymentStatusResponse)
        assertTrue(paymentStatusResponse.card == true)
        assertTrue(paymentStatusResponse.paypal == true)
    }

    @Test
    fun `payment status success f-droid`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<PaymentStatus>(any()) } returns ApiResult.Success(
            PaymentStatus(
                card = true,
                inApp = false,
                paypal = true
            )
        )
        // WHEN
        val paymentStatusResponse = repository.getPaymentStatus(SessionUserId(testUserId), AppStore.FDroid)
        // THEN
        assertNotNull(paymentStatusResponse)
        assertTrue(paymentStatusResponse.card == true)
        assertTrue(paymentStatusResponse.paypal == true)
    }
}
