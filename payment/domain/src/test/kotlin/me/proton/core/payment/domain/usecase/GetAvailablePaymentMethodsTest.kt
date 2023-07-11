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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsGetPaymentMethodsTotal
import me.proton.core.observability.domain.metrics.common.HttpApiStatus
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.payment.domain.entity.Card
import me.proton.core.payment.domain.entity.Details
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentMethodType
import me.proton.core.payment.domain.repository.PaymentsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetAvailablePaymentMethodsTest {

    private val testUserId = UserId("test-user-id")
    private val testReadOnlyCard = Card.CardReadOnly(
        brand = "visa", last4 = "1234", expirationMonth = "01",
        expirationYear = "2021", name = "Test", country = "Test Country", zip = "123"
    )
    private val testDefaultPaymentMethods = listOf(
        PaymentMethod(
            id = "1",
            type = PaymentMethodType.CARD,
            details = Details.CardDetails(testReadOnlyCard)
        ),
        PaymentMethod(
            id = "2",
            type = PaymentMethodType.PAYPAL,
            details = Details.PayPalDetails(
                billingAgreementId = "3",
                payer = "test payer"
            )
        )
    )

    private val repository = mockk<PaymentsRepository>(relaxed = true) {
        coEvery { getAvailablePaymentMethods(testUserId) } returns testDefaultPaymentMethods
    }

    private lateinit var useCase: GetAvailablePaymentMethods

    @Before
    fun beforeEveryTest() {
        useCase = GetAvailablePaymentMethods(repository)
    }

    @Test
    fun `get payment methods returns non empty list success`() = runTest {
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(PaymentMethodType.CARD, result[0].type)
        assertEquals(PaymentMethodType.PAYPAL, result[1].type)
    }

    @Test
    fun `get payment methods returns empty list success`() = runTest {
        coEvery { repository.getAvailablePaymentMethods(testUserId) } returns emptyList()
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertNotNull(result)
        assertEquals(0, result.size)
    }
}
