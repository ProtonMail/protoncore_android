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

package me.proton.core.plan.presentation.entity

import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.test.kotlin.assertEquals
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanCycleTest {

    @Test
    fun `test plan pricing by cycle`() {
        val pricing = PlanPricing(
            monthly = 1.0,
            yearly = 12.0,
            twoYearly = 24.0,
            other = null
        )

        var cycle = PlanCycle.MONTHLY
        var price = cycle.getPrice(pricing)
        assertEquals(1.0, price)

        cycle = PlanCycle.YEARLY
        price = cycle.getPrice(pricing)
        assertEquals(12.0, price)

        cycle = PlanCycle.TWO_YEARS
        price = cycle.getPrice(pricing)
        assertEquals(24.0, price)

        cycle = PlanCycle.OTHER
        price = cycle.getPrice(pricing)
        assertNull(price)
    }

    @Test
    fun `test plan pricing to subscription cycle`() {
        var cycle = PlanCycle.MONTHLY
        var subscriptionCycle = cycle.toSubscriptionCycle()
        assertEquals(SubscriptionCycle.MONTHLY, subscriptionCycle)

        cycle = PlanCycle.YEARLY
        subscriptionCycle = cycle.toSubscriptionCycle()
        assertEquals(SubscriptionCycle.YEARLY, subscriptionCycle)

        cycle = PlanCycle.TWO_YEARS
        subscriptionCycle = cycle.toSubscriptionCycle()
        assertEquals(SubscriptionCycle.TWO_YEARS, subscriptionCycle)

        cycle = PlanCycle.OTHER
        subscriptionCycle = cycle.toSubscriptionCycle()
        assertEquals(SubscriptionCycle.OTHER, subscriptionCycle)
    }
}