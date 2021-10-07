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

package me.proton.core.payment.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentBody
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PerformSubscribeTest {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPlanName = "test-plan-name"
    private val testPaymentToken = "test-payment-token"
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
        plans = listOf(mockk())
    )
    // endregion

    private lateinit var useCase: PerformSubscribe

    @Before
    fun beforeEveryTest() {
        useCase = PerformSubscribe(repository)
        coEvery {
            repository.createOrUpdateSubscription(
                testUserId,
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
                paymentToken = null
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
                paymentToken = null
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
                paymentToken = null
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
            paymentToken = testPaymentToken
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                amount = 1,
                currency = Currency.CHF,
                payment = PaymentBody.TokenPaymentBody(testPaymentToken),
                codes = null,
                plans = listOf(testPlanName).map { it to 1 }.toMap(),
                cycle = SubscriptionCycle.YEARLY
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
            paymentToken = null
        )
        coVerify(exactly = 1) {
            repository.createOrUpdateSubscription(
                sessionUserId = testUserId,
                amount = 0,
                currency = Currency.CHF,
                payment = null,
                codes = null,
                plans = listOf(testPlanName).map { it to 1 }.toMap(),
                cycle = SubscriptionCycle.YEARLY
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
                paymentToken = testPaymentToken
            )
        }
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }
}
