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
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.plan.domain.usecase.GetPlans
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanUpgradeToPaidTest {
    // region mocks
    private val getPlans: GetPlans = mockk()
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders = mockk()
    // endregion

    // region test data
    val testPlan = Plan(
        id = "plan-id-1",
        type = 1,
        cycle = 1,
        name = "Plan 1",
        title = "Plan Title 1",
        currency = "CHF",
        amount = 10,
        maxDomains = 1,
        maxAddresses = 1,
        maxCalendars = 1,
        maxSpace = 1,
        maxMembers = 1,
        maxVPN = 1,
        services = 0,
        features = 1,
        quantity = 1,
        maxTier = 1,
        enabled = true,
        pricing = PlanPricing(
            1, 10, 20
        )
    )
    // endregion

    private lateinit var useCase: CanUpgradeToPaid

    @Before
    fun beforeEveryTest() {
        useCase = CanUpgradeToPaid(
            supportPaidPlans = true,
            getPlans = getPlans,
            getAvailablePaymentProviders = getAvailablePaymentProviders
        )
    }

    @Test
    fun `can upgrade returns false when support paid is false`() = runTest {
        // GIVEN
        useCase = CanUpgradeToPaid(
            supportPaidPlans = false,
            getPlans = getPlans,
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
        coEvery { getPlans(any()) } returns listOf(testPlan)
        // WHEN
        val result = useCase()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false when only PayPal payment provider is available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns setOf(PaymentProvider.PayPal)
        coEvery { getPlans(any()) } returns listOf(testPlan)
        // WHEN
        val result = useCase()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false when no paid plans available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase
        )
        coEvery { getPlans(any()) } returns emptyList()
        // WHEN
        val result = useCase()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns true when paid plans available and payment providers available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase
        )
        coEvery { getPlans(any()) } returns listOf(testPlan)
        // WHEN
        val result = useCase()
        // THEN
        assertTrue(result)
    }
}