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

package me.proton.core.payment.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPreferredPaymentProviderTest {
    @MockK
    private lateinit var getAvailablePaymentProviders: GetAvailablePaymentProviders
    private lateinit var tested: GetPreferredPaymentProvider

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = GetPreferredPaymentProvider(getAvailablePaymentProviders)
    }

    @Test
    fun `no payment providers`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders(any(), any()) } returns emptySet()

        // WHEN
        val result = tested()

        // THEN
        assertNull(result)
    }

    @Test
    fun `unsupported provider`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders(any(), any()) } returns setOf(PaymentProvider.PayPal)

        // WHEN
        val result = tested()

        // THEN
        assertNull(result)
    }

    @Test
    fun `giap provider`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders(any(), any()) } returns setOf(
            PaymentProvider.GoogleInAppPurchase,
            PaymentProvider.PayPal
        )

        // WHEN
        val result = tested()

        // THEN
        assertEquals(PaymentProvider.GoogleInAppPurchase, result)
    }

    @Test
    fun `card provider`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders(any(), any()) } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase,
            PaymentProvider.PayPal
        )

        // WHEN
        val result = tested()

        // THEN
        assertEquals(PaymentProvider.CardPayment, result)
    }
}
