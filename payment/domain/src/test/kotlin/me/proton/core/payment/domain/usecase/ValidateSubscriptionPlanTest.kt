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
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ValidateSubscriptionPlanTest {

    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = SessionId("test-session-id")
    private val testAmount = 5L
    private val testAmountDue = 3L
    private val testCredit = 2L
    private val testPlanId = "test-planId-1"

    private val defaultSubscriptionStatus = SubscriptionStatus(
        amount = testAmount,
        amountDue = testAmountDue,
        proration = 0,
        couponDiscount = 0,
        coupon = null,
        credit = testCredit,
        currency = Currency.CHF,
        cycle = SubscriptionCycle.YEARLY,
        gift = null
    )
    // endregion

    private lateinit var useCase: ValidateSubscriptionPlan

    @Before
    fun beforeEveryTest() {
        useCase = ValidateSubscriptionPlan(repository)
        coEvery { repository.validateSubscription(any(), any(), any(), any(), any()) } returns defaultSubscriptionStatus
    }

    @Test
    fun `upgrade plan success test`() = runBlockingTest {
        val result = useCase.invoke(
            sessionId = testSessionId,
            codes = null,
            planIds = listOf(testPlanId),
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY
        )
        coVerify(exactly = 1) {
            repository.validateSubscription(
                testSessionId,
                null,
                listOf(testPlanId),
                Currency.CHF,
                SubscriptionCycle.YEARLY
            )
        }
        assertNotNull(result)
        assertEquals(testAmountDue, result.amountDue)
    }

    @Test
    fun `sign up payment success test`() = runBlockingTest {
        val session = null
        val result = useCase.invoke(
            sessionId = session,
            codes = null,
            planIds = listOf(testPlanId),
            currency = Currency.CHF,
            cycle = SubscriptionCycle.YEARLY
        )
        coVerify(exactly = 1) {
            repository.validateSubscription(
                session,
                null,
                listOf(testPlanId),
                Currency.CHF,
                SubscriptionCycle.YEARLY
            )
        }
        assertNotNull(result)
        assertEquals(testAmountDue, result.amountDue)
    }

    @Test
    fun `sign up payment no plans error handled correctly`() = runBlockingTest {
        val session = null
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(
                sessionId = session,
                codes = null,
                planIds = listOf(),
                currency = Currency.CHF,
                cycle = SubscriptionCycle.YEARLY
            )
        }
    }
}
