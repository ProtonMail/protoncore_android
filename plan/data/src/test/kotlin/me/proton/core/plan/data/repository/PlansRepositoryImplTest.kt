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

package me.proton.core.plan.data.repository

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.payment.domain.IsPaymentsV5Enabled
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.test.FakeIsPaymentsV5Enabled
import me.proton.core.plan.data.api.PlansApi
import me.proton.core.plan.data.entity.dynamicSubscription
import me.proton.core.plan.data.usecase.GetSessionUserIdForPaymentApi
import me.proton.core.plan.domain.PlanIconsEndpointProvider
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.test.kotlin.TestDispatcherProvider
import me.proton.core.test.kotlin.runTestWithResultContext
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlansRepositoryImplTest {
    // region mocks
    private val endpointProvider = mockk<PlanIconsEndpointProvider> {
        every { get() } returns "endpoint"
    }
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: PlansRepositoryImpl

    @MockK(relaxed = true)
    private lateinit var apiManager: ApiManager<PlansApi>

    @MockK
    private lateinit var getSessionUserIdForPaymentApi: GetSessionUserIdForPaymentApi

    @MockK
    private lateinit var userManager: UserManager
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    // endregion

    private lateinit var paymentsV5Enabled: IsPaymentsV5Enabled
    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        // GIVEN
        coEvery { getSessionUserIdForPaymentApi(any()) } answers { firstArg() }
        coEvery { sessionProvider.getSessionId(UserId(testUserId)) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        paymentsV5Enabled = FakeIsPaymentsV5Enabled(true)
        every {
            apiFactory.create(
                interfaceClass = PlansApi::class
            )
        } returns apiManager
        every {
            apiFactory.create(
                SessionId(testSessionId),
                interfaceClass = PlansApi::class
            )
        } returns apiManager
        coEvery { userManager.getUser(any(), any()) } returns mockk {
            every { type } returns Type.Proton
        }
        repository = PlansRepositoryImpl(
            apiProvider,
            endpointProvider,
            getSessionUserIdForPaymentApi,
            userManager,
            paymentsV5Enabled
        )
    }

    @Test
    fun `get dynamic plans`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plan = DynamicPlan(
            name = "name",
            order = 0,
            state = DynamicPlanState.Available,
            title = "title",
            type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary)
        )
        val plans = listOf(plan)
        coEvery { apiManager.invoke<DynamicPlans>(any()) } returns ApiResult.Success(DynamicPlans(null, plans))

        // WHEN
        val result = repository.getDynamicPlans(sessionUserId = null, AppStore.GooglePlay).plans

        // THEN
        assertContentEquals(plans, result)
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
    fun `fetch subscription for credential-less user`() = runTestWithResultContext(dispatcherProvider.Main) {
        // GIVEN
        coEvery { userManager.getUser(any(), any()) } returns mockk {
            every { type } returns Type.CredentialLess
        }

        // WHEN
        val result = repository.getDynamicSubscriptions(SessionUserId(testUserId))

        // THEN
        assertEquals(1, result.size)
        assertEquals(
            DynamicSubscription(name = null, title = "", description = ""),
            result.first()
        )
        coVerify(exactly = 0) { apiManager.invoke<PlansApi>(any()) }
    }
}
