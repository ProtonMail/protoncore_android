/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.plan.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.plan.domain.repository.PlansRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPlansTest {
    // region mocks
    private val repository = mockk<PlansRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
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
        maxSpace = 1,
        maxMembers = 1,
        maxVPN = 1,
        services = 0,
        features = 1,
        quantity = 1,
        maxTier = 1,
        state = true,
        pricing = PlanPricing(
            1, 10, 20
        )
    )

    // endregion
    private lateinit var useCase: GetPlans

    @Before
    fun beforeEveryTest() {
        useCase = GetPlans(repository)
    }

    @Test
    fun `get plans returns success`() = runBlockingTest {
        // GIVEN
        coEvery { repository.getPlans(testUserId) } returns listOf(testPlan)
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(1, result.size)
        assertEquals(testPlan, result[0])
        assertEquals("plan-name-1", result[0].id)
    }

    @Test
    fun `get plans returns empty because of disabled plans`() = runBlockingTest {
        // GIVEN
        useCase = GetPlans(repository)
        coEvery { repository.getPlans(testUserId) } returns listOf(testPlan.copy(state = false))
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `get plans returns success no user id`() = runBlockingTest {
        // GIVEN
        coEvery { repository.getPlans(null) } returns listOf(testPlan)
        // WHEN
        val result = useCase.invoke(null)
        // THEN
        assertEquals(1, result.size)
        assertEquals(testPlan, result[0])
        assertEquals("plan-name-1", result[0].id)
    }
}
