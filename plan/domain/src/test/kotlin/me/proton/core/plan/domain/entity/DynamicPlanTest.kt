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

package me.proton.core.plan.domain.entity

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.type.IntEnum
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicPlanTest {

    private fun instanceFor(cycle: Int, vararg currency: String) = DynamicPlanInstance(
        cycle = cycle,
        description = "description-$cycle-$currency",
        periodEnd = Instant.now(),
        price = currency.map {
            DynamicPlanPrice(
                id = "id-$cycle-$currency",
                currency = it,
                current = 100
            )
        }.associateBy { it.currency },
        vendors = mapOf(
            AppStore.GooglePlay to DynamicPlanVendor(
                productId = "googlemail_plus_${cycle}_renewing",
                customerId = "cus_google_fAx9TIdL63UmeYDmUo3l"
            )
        )
    )

    private val planFree = DynamicPlan(
        name = "free",
        order = 0,
        state = DynamicPlanState.Available,
        title = "Free",
        type = null,
        instances = emptyMap(), // No instances.
    )

    private val plan1 = DynamicPlan(
        name = "test1",
        order = 0,
        state = DynamicPlanState.Available,
        title = "title",
        type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary),
        instances = mapOf(
            12 to instanceFor(12, "CHF", "USD", "EUR"),
            24 to instanceFor(24, "CHF")
        ),
    )

    private val plan2 = DynamicPlan(
        name = "test2",
        order = 0,
        state = DynamicPlanState.Available,
        title = "title2",
        type = IntEnum(DynamicPlanType.Secondary.code, DynamicPlanType.Secondary),
        instances = mapOf(
            12 to instanceFor(12, "CHF"),
            24 to instanceFor(24, "CHF", "USD", "EUR")
        ),
    )

    @Test
    fun filterByAlwaysReturnPlanFree() {
        // Given
        val plans = listOf(planFree, plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(12, "CHF")

        // Then
        assertEquals(3, filteredPlans.size)
    }

    @Test
    fun filterBy24CycleAndCHFCurrency() {
        // Given
        val plans = listOf(planFree, plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(24, "CHF")

        // Then
        assertEquals(3, filteredPlans.size)
    }

    @Test
    fun filterBy12CycleAndEURCurrency() {
        // Given
        val plans = listOf(planFree, plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(12, "EUR")

        // Then
        assertEquals(2, filteredPlans.size)
        assertEquals("free", filteredPlans[0].name)
        assertEquals("test1", filteredPlans[1].name)
    }

    @Test
    fun filterBy24CycleAndUSDCurrency() {
        // Given
        val plans = listOf(planFree, plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(24, "USD")

        // Then
        assertEquals(2, filteredPlans.size)
        assertEquals("free", filteredPlans[0].name)
        assertEquals("test2", filteredPlans[1].name)
    }
}
