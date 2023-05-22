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

import me.proton.core.plan.domain.entity.Plan
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PlanPromotionPercentageTest {

    private val testPlan = Plan(
        id = "plan-name-1",
        type = 1,
        cycle = 1,
        name = "plan-name-1",
        title = "Plan Title 1",
        currency = "CHF",
        amount = 10,
        maxDomains = 1,
        maxAddresses = 1,
        maxCalendars = 1,
        maxSpace = 2,
        maxMembers = 1,
        maxVPN = 1,
        services = 0,
        features = 1,
        quantity = 1,
        maxTier = 1,
        enabled = true,
        pricing = me.proton.core.plan.domain.entity.PlanPricing(
            1, 10, 20
        ),
        defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
            2, 20, 40
        )
    )

    @Test
    fun `test plan promotions 50%`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan)
        assertNotNull(promotions)
        assertEquals(50, promotions.monthly)
        assertEquals(50, promotions.yearly)
        assertEquals(50, promotions.twoYearly)
        assertNull(promotions.other)
    }

    @Test
    fun `test plan promotions different percentages`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan.copy(
            pricing = me.proton.core.plan.domain.entity.PlanPricing(
                7, 11, 19
            ),
            defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
                21, 30, 45
            )
        ))
        assertNotNull(promotions)
        assertEquals(67, promotions.monthly)
        assertEquals(63, promotions.yearly)
        assertEquals(58, promotions.twoYearly)
        assertNull(promotions.other)
    }

    @Test
    fun `test plan promotions zero percentages`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan.copy(
            pricing = me.proton.core.plan.domain.entity.PlanPricing(
                7, 11, 19
            ),
            defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
                7, 11, 19
            )
        ))
        assertNotNull(promotions)
        assertEquals(0, promotions.monthly)
        assertEquals(0, promotions.yearly)
        assertEquals(0, promotions.twoYearly)
        assertNull(promotions.other)
    }

    @Test
    fun `test plan promotions default prices lower`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan.copy(
            pricing = me.proton.core.plan.domain.entity.PlanPricing(
                10, 100, 200
            ),
            defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
                9, 90, 190
            )
        ))
        assertNotNull(promotions)
        assertEquals(0, promotions.monthly)
        assertEquals(0, promotions.yearly)
        assertEquals(0, promotions.twoYearly)
        assertNull(promotions.other)
    }

    @Test
    fun `test plan promotions all two yearly plan price null`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan.copy(
            pricing = me.proton.core.plan.domain.entity.PlanPricing(
                10, 100, null
            ),
            defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
                9, 90, null
            )
        ))
        assertNotNull(promotions)
        assertEquals(0, promotions.monthly)
        assertEquals(0, promotions.yearly)
        assertEquals(0, promotions.twoYearly)
        assertNull(promotions.other)
    }

    @Test
    fun `test plan promotions two yearly plan price null default not null`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan.copy(
            pricing = me.proton.core.plan.domain.entity.PlanPricing(
                10, 100, null
            ),
            defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
                9, 90, 200
            )
        ))
        assertNotNull(promotions)
        assertEquals(0, promotions.monthly)
        assertEquals(0, promotions.yearly)
        assertEquals(0, promotions.twoYearly)
        assertNull(promotions.other)
    }

    @Test
    fun `test plan promotions two yearly plan default price null promo not null`() {
        val promotions = PlanPromotionPercentage.fromPlan(testPlan.copy(
            pricing = me.proton.core.plan.domain.entity.PlanPricing(
                10, 100, null
            ),
            defaultPricing = me.proton.core.plan.domain.entity.PlanPricing(
                9, 90, 200
            )
        ))
        assertNotNull(promotions)
        assertEquals(0, promotions.monthly)
        assertEquals(0, promotions.yearly)
        assertEquals(0, promotions.twoYearly)
        assertNull(promotions.other)
    }

}