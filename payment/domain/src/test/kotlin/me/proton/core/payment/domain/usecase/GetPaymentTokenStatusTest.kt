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
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException
import java.lang.RuntimeException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GetPaymentTokenStatusTest {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPaymentToken = "test-payment-token"
    private val testDefaultPaymentTokenStatusResult = PaymentToken.PaymentTokenStatusResult(PaymentTokenStatus.PENDING)
    // endregion
    private lateinit var useCase: GetPaymentTokenStatus

    @Before
    fun beforeEveryTest() {
        useCase = GetPaymentTokenStatus(repository)
        coEvery {
            repository.getPaymentTokenStatus(testUserId, testPaymentToken)
        } returns testDefaultPaymentTokenStatusResult
    }

    @Test
    fun `payment token status for upgrade returns success chargeable`() = runBlockingTest {
        val result = useCase.invoke(testUserId, testPaymentToken)
        assertNotNull(result)
        assertEquals(PaymentTokenStatus.PENDING, result.status)
    }

    @Test
    fun `payment token status for sign up returns success chargeable`() = runBlockingTest {
        coEvery {
            repository.getPaymentTokenStatus(null, testPaymentToken)
        } returns testDefaultPaymentTokenStatusResult
        val result = useCase.invoke(null, testPaymentToken)
        assertNotNull(result)
        assertEquals(PaymentTokenStatus.PENDING, result.status)
    }

    @Test
    fun `payment token status empty token handled correctly`() = runBlockingTest {
        coEvery {
            repository.getPaymentTokenStatus(null, "")
        } returns testDefaultPaymentTokenStatusResult
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(null, "")
        }
    }

    @Test
    fun `payment token status error handled correctly`() = runBlockingTest {
        coEvery {
            repository.getPaymentTokenStatus(null, testPaymentToken)
        } throws ApiException(ApiResult.Error.Connection(false, RuntimeException("Test error")))
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(null, testPaymentToken)
        }
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }
}
