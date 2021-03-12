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
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CreatePaymentTokenTest {
    // region mocks
    private val repository = mockk<PaymentsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = SessionId("test-session-id")
    private val testAmount = 5L
    private val testCurrency = Currency.CHF
    private val testDefaultCardWithPaymentDetails = Card.CardWithPaymentDetails(
        number = "123456789", cvc = "123",
        expirationMonth = "01", expirationYear = "2021", name = "Test", country = "Test Country", zip = "123"
    )
    private val testPayment = PaymentType.CreditCard(testDefaultCardWithPaymentDetails)
    private val testToken = "test-token"
    private val testApprovalUrl = "test-approval-url"
    private val testReturnHost = "test-return-host"
    private val createTokenResult = PaymentToken.CreatePaymentTokenResult(
        PaymentTokenStatus.PENDING, testApprovalUrl, testToken, testReturnHost
    )
    // endregion

    private lateinit var useCase: CreatePaymentToken

    @Before
    fun beforeEveryTest() {
        useCase = CreatePaymentToken(repository)
        coEvery {
            repository.createPaymentToken(any(), any(), any(), any(), null)
        } returns createTokenResult
    }

    @Test
    fun `create payment token upgrade success response`() = runBlockingTest {
        val result = useCase.invoke(testSessionId, testAmount, testCurrency, testPayment)

        coVerify(exactly = 1) {
            repository.createPaymentToken(
                sessionId = testSessionId,
                amount = testAmount,
                currency = testCurrency,
                paymentType = testPayment,
                paymentMethodId = null
            )
        }

        assertNotNull(result)
        assertEquals(testToken, result.token)
        assertEquals(PaymentTokenStatus.PENDING, result.status)
        assertEquals(testApprovalUrl, result.approvalUrl)
        assertEquals(testReturnHost, result.returnHost)
    }

    @Test
    fun `create payment token sign up success response`() = runBlockingTest {
        val result = useCase.invoke(null, testAmount, testCurrency, testPayment)

        coVerify(exactly = 1) {
            repository.createPaymentToken(
                sessionId = null,
                amount = testAmount,
                currency = testCurrency,
                paymentType = testPayment,
                paymentMethodId = null
            )
        }

        assertNotNull(result)
        assertEquals(testToken, result.token)
        assertEquals(PaymentTokenStatus.PENDING, result.status)
        assertEquals(testApprovalUrl, result.approvalUrl)
        assertEquals(testReturnHost, result.returnHost)
    }

    @Test
    fun `create token returns chargeable`() = runBlockingTest {
        val createTokenChargeableResult = PaymentToken.CreatePaymentTokenResult(
            PaymentTokenStatus.CHARGEABLE, null, testToken, null
        )
        coEvery {
            repository.createPaymentToken(testSessionId, testAmount, testCurrency, any(), null)
        } returns createTokenChargeableResult

        val result = useCase.invoke(testSessionId, testAmount, testCurrency, testPayment)
        assertNotNull(result)
        assertEquals(testToken, result.token)
        assertEquals(PaymentTokenStatus.CHARGEABLE, result.status)
        assertNull(result.approvalUrl)
        assertNull(result.returnHost)
    }

    @Test
    fun `create payment token negative amount response`() = runBlockingTest {
        assertFailsWith(IllegalArgumentException::class) {
            useCase.invoke(testSessionId, -1, testCurrency, testPayment)
        }
    }
}
