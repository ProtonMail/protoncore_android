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

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicPlanTest {

    private fun instanceFor(cycle: Int, vararg currency: String) = DynamicPlanInstance(
        id = "id-$cycle-$currency",
        cycle = cycle,
        description = "description-$cycle-$currency",
        periodEnd = Instant.now(),
        price = currency.map {
            DynamicPlanPrice(
                currency = it,
                current = 100
            )
        }.associateBy { it.currency },
        vendors = emptyMap()
    )

    private val planEmpty = DynamicPlan(
        name = "empty",
        order = 0,
        state = DynamicPlanState.Available,
        title = "title",
        type = null,
        instances = emptyMap(), // No instances.
    )

    private val plan1 = DynamicPlan(
        name = "test1",
        order = 0,
        state = DynamicPlanState.Available,
        title = "title",
        type = null,
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
        type = null,
        instances = mapOf(
            12 to instanceFor(12, "CHF"),
            24 to instanceFor(24, "CHF", "USD", "EUR")
        ),
    )

    @Test
    fun filterByAlwaysReturnPlanEmpty() {
        // Given
        val plans = listOf(planEmpty, plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(12, "CHF")

        // Then
        assertEquals(3, filteredPlans.size)
    }

    @Test
    fun filterBy12CycleAndCHFCurrency() {
        // Given
        val plans = listOf(plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(12, "CHF")

        // Then
        assertEquals(2, filteredPlans.size)
    }

    @Test
    fun filterBy12CycleAndEURCurrency() {
        // Given
        val plans = listOf(plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(12, "EUR")

        // Then
        assertEquals(1, filteredPlans.size)
        assertEquals("test1", filteredPlans[0].name)
    }

    @Test
    fun filterBy24CycleAndUSDCurrency() {
        // Given
        val plans = listOf(plan1, plan2)

        // When
        val filteredPlans = plans.filterBy(24, "USD")

        // Then
        assertEquals(1, filteredPlans.size)
        assertEquals("test2", filteredPlans[0].name)
    }
}
