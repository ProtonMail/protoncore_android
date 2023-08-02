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

package me.proton.core.payment.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.payment.domain.entity.dynamicSubscription
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

class GetDynamicSubscriptionTest {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testSubscription = dynamicSubscription
    // endregion

    private lateinit var useCase: GetDynamicSubscription

    @Before
    fun beforeEveryTest() {
        useCase = GetDynamicSubscription(repository)
    }

    @Test
    fun `get subscription returns success, enqueue getsubscription observability success`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscription(testUserId) } returns testSubscription
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(testSubscription, result)
        assertNotNull(result)
        assertNotNull(result)
        assertEquals(0, result.amount)
    }

    @Test
    fun `get subscription returns error`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscription(testUserId) } throws ApiException(
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
    fun `get dynamic subscription returns no active subscription`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscription(testUserId) } throws ApiException(
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
        assertFailsWith<ApiException> { useCase.invoke(testUserId) }
    }
}
