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
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetCurrentSubscriptionTest {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testSubscription = Subscription(
        id = "test-subscription-id",
        invoiceId = "test-invoice-id",
        cycle = 12,
        periodStart = 1,
        periodEnd = 2,
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
    // endregion

    private lateinit var useCase: GetCurrentSubscription

    @Before
    fun beforeEveryTest() {
        useCase = GetCurrentSubscription(repository)
    }

    @Test
    fun `get subscription returns success`() = runTest {
        // GIVEN
        coEvery { repository.getSubscription(testUserId) } returns testSubscription
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(testSubscription, result)
        assertNotNull(result)
        assertEquals(5, result.amount)
    }

    @Test
    fun `get subscription returns error`() = runTest {
        // GIVEN
        coEvery { repository.getSubscription(testUserId) } throws ApiException(
            ApiResult.Error.Connection(
                false,
                RuntimeException("Test error")
            )
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(testUserId)
        }
        // THEN
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }

    @Test
    fun `get subscription returns no active subscription`() = runTest {
        // GIVEN
        coEvery { repository.getSubscription(testUserId) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = ResponseCodes.PAYMENTS_SUBSCRIPTION_NOT_EXISTS,
                    error = "no active subscription"
                )
            )
        )
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertNull(result)
    }
}
