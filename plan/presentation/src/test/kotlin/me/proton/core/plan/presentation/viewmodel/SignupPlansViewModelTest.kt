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

package me.proton.core.plan.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import me.proton.core.payment.domain.usecase.PurchaseEnabled
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.plan.domain.usecase.GetPlanDefault
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.SupportedPlan
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignupPlansViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val getPlansUseCase = mockk<GetPlans>()
    private val getPlanDefaultUseCase = mockk<GetPlanDefault>(relaxed = true)
    private val paymentOrchestrator = mockk<PaymentsOrchestrator>(relaxed = true)
    private val purchaseEnabled = mockk<PurchaseEnabled>(relaxed = true)
    // endregion

    // region test data
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

    private val testDefaultPlan = Plan(
        id = null,
        type = 1,
        cycle = null,
        name = "plan-default",
        title = "Plan Default",
        currency = null,
        amount = 0,
        maxDomains = 0,
        maxAddresses = 1,
        maxCalendars = 0,
        maxSpace = 1,
        maxMembers = 1,
        maxVPN = 0,
        services = 0,
        features = 0,
        quantity = 0,
        maxTier = 0,
        state = true
    )
    // endregion

    private lateinit var viewModel: SignupPlansViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getPlanDefaultUseCase.invoke(any()) } returns testDefaultPlan
        coEvery { purchaseEnabled.invoke() } returns true

        viewModel =
            SignupPlansViewModel(
                getPlansUseCase,
                getPlanDefaultUseCase,
                true,
                purchaseEnabled,
                paymentOrchestrator
            )
    }

    @Test
    fun `get plans for signup success handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan,
            testPlan.copy(id = "plan-name-2", name = "plan-name-2")
        )
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getAllPlansForSignup()
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(3, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            val planThree = plansStatus.plans[2]
            assertEquals("plan-default", planThree.name)
            assertEquals("plan-name-1", planOne.name)
            assertEquals("plan-name-2", planTwo.name)
        }
    }
}
