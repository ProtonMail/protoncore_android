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

package me.proton.core.plan.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanUpgradeFromMobileTest {

    private val getAvailablePaymentProviders: GetAvailablePaymentProviders = mockk()

    private lateinit var useCase: CanUpgradeFromMobile

    @Before
    fun beforeEveryTest() {
        useCase = CanUpgradeFromMobile(
            supportPaidPlans = true,
            getAvailablePaymentProviders = getAvailablePaymentProviders
        )
    }

    @Test
    fun `can upgrade returns false when support paid is false`() = runTest {
        // GIVEN
        useCase = CanUpgradeFromMobile(
            supportPaidPlans = false,
            getAvailablePaymentProviders = getAvailablePaymentProviders
        )
        // WHEN
        val result = useCase()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false when no payment providers available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns emptySet()
        // WHEN
        val result = useCase()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false when only PayPal payment provider is available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns setOf(PaymentProvider.PayPal)
        // WHEN
        val result = useCase()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns true when payment providers available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase
        )
        // WHEN
        val result = useCase()
        // THEN
        assertTrue(result)
    }
}
