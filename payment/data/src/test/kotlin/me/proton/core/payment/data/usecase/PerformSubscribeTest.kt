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

package me.proton.core.payment.data.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentBody
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.payment.domain.usecase.PerformSubscribe
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PerformSubscribeTest {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    private val humanVerificationManager = mockk<HumanVerificationManager>(relaxed = true)
    private val clientIdProvider = mockk<ClientIdProvider>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPlanName = "test-plan-name"
    private val testPaymentToken = ProtonPaymentToken("test-payment-token")
    private val testSubscriptionId = "test-subscription-id"
    private val testSubscription = Subscription(
        id = testSubscriptionId,
        invoiceId = "test-invoice-id",
        cycle = 12,
        periodStart = 1L,
        periodEnd = 2L,
        couponCode = null,
        currency = "EUR",
        amount = 5L,
        plans = listOf(mockk()),
        external = SubscriptionManagement.PROTON_MANAGED
    )
    // endregion

    private lateinit var useCase: PerformSubscribe

    @Before
    fun beforeEveryTest() {
        useCase = PerformSubscribeImpl(Optional.empty(), repository, humanVerificationManager, clientIdProvider)
        coEvery {
            repository.createOrUpdateSubscription(
                testUserId,
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns testSubscription
    }

    @Test
    fun `payment token not provided with amount bigger than zero is handled correctly`() = runBlockingTest {
        val throwable = assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
        assertNotNull(throwable)
        assertEquals(
            "Payment Token must be supplied when the amount is bigger than zero. Otherwise it should be null.",
            throwable.message
        )
    }

    @Test
    fun `negative amount is handled correctly`() = runBlockingTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                amount = -1,
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
    }

    @Test
    fun `negative amount google managed is handled correctly`() = runBlockingTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                amount = -1,
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
            )
        }
    }

    @Test
    fun `no plans is handled correctly`() = runBlockingTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(),
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
    }

    @Test
    fun `no plans google managed is handled correctly`() = runBlockingTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                userId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(),
                codes = null,
                paymentToken = null,
                subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
            )
        }
    }

    @Test
    fun `happy path is handled correctly`() = runBlockingTest {
        val result = useCase.invoke(
            userId = testUserId,
            amount = 1,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            codes = null,
            paymentToken = testPaymentToken,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                payment = PaymentBody.TokenPaymentBody(testPaymentToken),
                codes = null,
                plans = listOf(testPlanName).map { it to 1 }.toMap(),
                cycle = SubscriptionCycle.YEARLY,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `happy path google managed is handled correctly`() = runBlockingTest {
        val result = useCase.invoke(
            userId = testUserId,
            amount = 1,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            codes = null,
            paymentToken = testPaymentToken,
            subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                payment = PaymentBody.TokenPaymentBody(testPaymentToken),
                codes = null,
                plans = listOf(testPlanName).map { it to 1 }.toMap(),
                cycle = SubscriptionCycle.YEARLY,
                subscriptionManagement = SubscriptionManagement.GOOGLE_MANAGED
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `happy path 0 amount is handled correctly`() = runBlockingTest {
        val result = useCase.invoke(
            userId = testUserId,
            amount = 0,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            codes = null,
            paymentToken = null,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                amount = 0,
                currency = Currency.CHF,
                payment = null,
                codes = null,
                plans = listOf(testPlanName).map { it to 1 }.toMap(),
                cycle = SubscriptionCycle.YEARLY,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
        assertNotNull(result)
    }

    @Test
    fun `repository returns error handled correctly`() = runBlockingTest {
        coEvery {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws ApiException(ApiResult.Error.Connection(false, RuntimeException("Test error")))

        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(
                userId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY,
                planNames = listOf(testPlanName),
                codes = null,
                paymentToken = testPaymentToken,
                subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
            )
        }
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }

    @Test
    fun `payment token is cleared on successful subscription`() = runBlockingTest {
        useCase.invoke(
            userId = testUserId,
            amount = 48,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            codes = null,
            paymentToken = ProtonPaymentToken("token"),
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )

        coVerify(atLeast = 1) { humanVerificationManager.clearDetails(any()) }
    }

    @Test
    fun `null payment token is not cleared on successful subscription`() = runBlockingTest {
        useCase.invoke(
            userId = testUserId,
            amount = 0,
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY,
            planNames = listOf(testPlanName),
            codes = null,
            paymentToken = null,
            subscriptionManagement = SubscriptionManagement.PROTON_MANAGED
        )

        coVerify(exactly = 0) { humanVerificationManager.clearDetails(any()) }
    }
}
