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

package me.proton.core.plan.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.freePlan
import me.proton.core.plan.domain.entity.mailPlusPlan
import me.proton.core.plan.domain.entity.unlimitedPlan
import me.proton.core.plan.domain.repository.PlansRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetDynamicPlansTest {
    @MockK
    private lateinit var plansRepository: PlansRepository

    private lateinit var tested: GetDynamicPlans

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `all plans`() = runTest {
        // GIVEN
        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                freePlan,
                mailPlusPlan,
                unlimitedPlan,
            )
        )

        tested = GetDynamicPlans(plansRepository, AppStore.GooglePlay)

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(3, plans.size)
        assertEquals(freePlan, plans[0])
        assertEquals(mailPlusPlan, plans[1])
        assertEquals(unlimitedPlan, plans[2])
    }
}
