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
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.presentation.PaymentsOrchestrator
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

class UpgradePlansViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val getPlansUseCase = mockk<GetPlans>()
    private val getSubscriptionUseCase = mockk<GetCurrentSubscription>(relaxed = true)
    private val paymentOrchestrator = mockk<PaymentsOrchestrator>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testDefaultSupportedPlans = listOf("plan-name-1", "plan-name-2")
    private val testSubscribedPlan = Plan(
        id = "subscribed-plan-name-1",
        type = 1,
        cycle = 1,
        name = "subscribed-plan-name-1",
        title = "Subscribed Plan Title 1",
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
        pricing = PlanPricing(
            1, 10, 20
        )
    )

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
            testSubscribedPlan
        )
    )
    // endregion

    private lateinit var viewModel: UpgradePlansViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getPlansUseCase.invoke(testDefaultSupportedPlans, testUserId) } returns listOf(
            testPlan
        )

        viewModel = UpgradePlansViewModel(
            getPlansUseCase,
            getSubscriptionUseCase,
            testDefaultSupportedPlans,
            paymentOrchestrator
        )
    }

    @Test
    fun `get plans for upgrade success handled correctly`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns testSubscription
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(1, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            assertEquals("plan-name-1", planOne.name)
        }
    }

    @Test
    fun `get plans for upgrade no active subscription handled correctly`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(1, plansStatus.plans.size)
            val planPaid = plansStatus.plans[0]
            assertEquals("plan-name-1", planPaid.name)
        }
    }
}
