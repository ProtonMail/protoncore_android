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
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlansViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val getPlansUseCase = mockk<GetPlans>()
    private val getCurrentSubscription = mockk<GetCurrentSubscription>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testDefaultSupportedPlanIds = listOf("plan-id-1", "plan-id-2")
    private val testPlan = Plan(
        id = "plan-id-1",
        type = 1,
        cycle = 1,
        name = "Plan 1",
        title = "Plan Title 1",
        currency = "CHF",
        amount = 10,
        maxDomains = 1,
        maxAddresses = 1,
        maxSpace = 1,
        maxMembers = 1,
        maxVPN = 1,
        services = 0,
        features = 1,
        quantity = 1,
        maxTier = 1,
        pricing = PlanPricing(
            1, 10, 20
        )
    )
    private val testSubscription = Subscription(
        id = "test-subscription-id",
        invoiceId = "test-invoice-id",
        cycle = 12,
        periodStart = 1,
        periodEnd = 2,
        couponCode = null,
        currency = "EUR",
        amount = 5,
        plans = listOf(
            testPlan
        )
    )
    // endregion

    private lateinit var viewModel: PlansViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = PlansViewModel(getPlansUseCase, getCurrentSubscription, testDefaultSupportedPlanIds)
    }

    @Test
    fun `get plans for signup success handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(testDefaultSupportedPlanIds, any()) } returns listOf(
            testPlan,
            testPlan.copy(id = "plan-id-2")
        )
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentPlanWithUpgradeOption()
            // THEN
            assertIs<PlansViewModel.State.Idle>(expectItem())
            assertIs<PlansViewModel.State.Processing>(expectItem())
            val plansStatus = expectItem()
            assertTrue(plansStatus is PlansViewModel.State.Success)
            assertEquals(3, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            val planThree = plansStatus.plans[2]
            assertEquals("free", planOne.id)
            assertEquals("plan-id-1", planTwo.id)
            assertEquals("plan-id-2", planThree.id)
        }
    }

    @Test
    fun `get plans for upgrade success handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(testDefaultSupportedPlanIds, testUserId) } returns listOf(
            testPlan
        )
        coEvery { getCurrentSubscription.invoke(testUserId) } returns testSubscription
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentPlanWithUpgradeOption(testUserId)
            // THEN
            assertIs<PlansViewModel.State.Idle>(expectItem())
            assertIs<PlansViewModel.State.Processing>(expectItem())
            val plansStatus = expectItem()
            assertTrue(plansStatus is PlansViewModel.State.Success)
            assertEquals(1, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            assertEquals("plan-id-1", planOne.id)
        }
    }

    @Test
    fun `get plans for upgrade no active subscription handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(testDefaultSupportedPlanIds, testUserId) } returns listOf(
            testPlan
        )
        coEvery { getCurrentSubscription.invoke(testUserId) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = PlansViewModel.NO_ACTIVE_SUBSCRIPTION,
                    error = "no active subscription"
                )
            )
        )
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentPlanWithUpgradeOption(testUserId)
            // THEN
            assertIs<PlansViewModel.State.Idle>(expectItem())
            assertIs<PlansViewModel.State.Processing>(expectItem())
            val plansStatus = expectItem()
            assertTrue(plansStatus is PlansViewModel.State.Success)
            assertEquals(2, plansStatus.plans.size)
            val planFree = plansStatus.plans[0]
            val planPaid = plansStatus.plans[1]
            assertEquals("free", planFree.id)
            assertEquals("plan-id-1", planPaid.id)
        }
    }
}
