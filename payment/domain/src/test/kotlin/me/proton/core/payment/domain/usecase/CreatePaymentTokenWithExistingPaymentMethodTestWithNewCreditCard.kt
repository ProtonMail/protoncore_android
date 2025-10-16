/*
 * Copyright (c) 2025 Proton AG
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
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CreatePaymentTokenWithExistingPaymentMethodTestWithNewCreditCard {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPaymentMethodId = "test-paymentMethodID"
    private val testPaymentType = PaymentType.PaymentMethod(testPaymentMethodId)
    private val testToken = ProtonPaymentToken("test-token")
    private val testApprovalUrl = "test-approval-url"
    private val testReturnHost = "test-return-host"
    private val createTokenResult = PaymentTokenResult.CreatePaymentTokenResult(
        PaymentTokenStatus.PENDING, testApprovalUrl, testToken, testReturnHost
    )
    // endregion

    private lateinit var useCase: CreatePaymentToken

    @Before
    fun beforeEveryTest() {
        useCase = CreatePaymentToken(repository, mockk())
        coEvery {
            repository.createPaymentToken(any(), any())
        } returns createTokenResult
    }

    @Test
    fun `create payment token upgrade success response`() = runTest {
        val result = useCase.invoke(testUserId, testPaymentType)

        coVerify(exactly = 1) {
            repository.createPaymentToken(
                sessionUserId = testUserId,
                paymentType = testPaymentType
            )
        }

        assertNotNull(result)
        assertEquals(testToken, result.token)
        assertEquals(PaymentTokenStatus.PENDING, result.status)
        assertEquals(testApprovalUrl, result.approvalUrl)
        assertEquals(testReturnHost, result.returnHost)
    }

    @Test
    fun `create payment token sign up success response`() = runTest {
        val result = useCase.invoke(null, testPaymentType)

        coVerify(exactly = 1) {
            repository.createPaymentToken(
                sessionUserId = null,
                paymentType = testPaymentType
            )
        }

        assertNotNull(result)
        assertEquals(testToken, result.token)
        assertEquals(PaymentTokenStatus.PENDING, result.status)
        assertEquals(testApprovalUrl, result.approvalUrl)
        assertEquals(testReturnHost, result.returnHost)
    }

    @Test
    fun `create token returns chargeable`() = runTest {
        val createTokenChargeableResult = PaymentTokenResult.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE, null, testToken, null
        )
        coEvery {
            repository.createPaymentToken(
                sessionUserId = testUserId,
                paymentType = testPaymentType
            )
        } returns createTokenChargeableResult

        val result = useCase.invoke(testUserId, testPaymentType)
        assertNotNull(result)
        assertEquals(testToken, result.token)
        assertEquals(PaymentTokenStatus.CHARGEABLE, result.status)
        assertNull(result.approvalUrl)
        assertNull(result.returnHost)
    }
}
