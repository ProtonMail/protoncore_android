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

package me.proton.core.plan.presentation.viewmodel

import android.content.res.Resources
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutScreenViewTotal
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.isFree
import me.proton.core.plan.domain.usecase.CanUpgrade
import me.proton.core.plan.domain.usecase.GetDynamicPlansAdjustedPrices
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.getSelectedPlan
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DynamicSelectPlanViewModelTest : CoroutinesTest by CoroutinesTest() {
    @MockK
    private lateinit var canUpgrade: CanUpgrade

    @MockK
    private lateinit var getDynamicPlans: GetDynamicPlansAdjustedPrices

    @MockK(relaxUnitFun = true)
    private lateinit var observabilityManager: ObservabilityManager

    @MockK(relaxed = true)
    private lateinit var resources: Resources

    private lateinit var tested: DynamicSelectPlanViewModel

    private val testPlanFree = mockk<DynamicPlan>(relaxed = true) {
        every { type } returns null // free plan has null type
    }

    private val testSelectedPlanFree
        get() = testPlanFree.getSelectedPlan(resources, PlanCycle.FREE.cycleDurationMonths, null)

    private val testPlanPaid = mockk<DynamicPlan>(relaxed = true) {
        every { type } returns IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary)
    }

    private val testSelectedPlanPaid
        get() = testPlanPaid.getSelectedPlan(resources, PlanCycle.YEARLY.cycleDurationMonths, null)

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = DynamicSelectPlanViewModel(
            canUpgrade,
            getDynamicPlans,
            observabilityManager,
        )
    }

    @Test
    fun `cannot upgrade`() = coroutinesTest {
        // GIVEN
        coEvery { getDynamicPlans(any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(testPlanFree)
        )
        coEvery { canUpgrade.invoke(any()) } returns false

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())

            val state = assertIs<DynamicSelectPlanViewModel.State.FreePlanOnly>(awaitItem())
            assertTrue(state.plan.isFree())
            assertEquals(testPlanFree.name, state.plan.name)
        }
    }

    @Test
    fun `cannot upgrade and free plan not returned`() = coroutinesTest {
        // GIVEN
        coEvery { getDynamicPlans(any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(testPlanPaid)
        )
        coEvery { canUpgrade.invoke(any()) } returns false

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            val error = assertIs<DynamicSelectPlanViewModel.State.Error>(awaitItem())
            assertEquals("Could not find a free plan.", error.error.message)
        }
    }

    @Test
    fun `happy path free plan`() = coroutinesTest {
        // GIVEN
        coEvery { canUpgrade.invoke(any()) } returns true

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            assertEquals(DynamicSelectPlanViewModel.State.Idle, awaitItem())

            // WHEN
            tested.perform(DynamicSelectPlanViewModel.Action.SelectFreePlan(testSelectedPlanFree))

            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            assertEquals(
                DynamicSelectPlanViewModel.State.FreePlanSelected(testSelectedPlanFree),
                awaitItem()
            )
        }
    }

    @Test
    fun `happy path paid`() = coroutinesTest {
        // GIVEN
        val billingResult = mockk<BillingResult>()
        coEvery { canUpgrade.invoke(any()) } returns true

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            assertEquals(DynamicSelectPlanViewModel.State.Idle, awaitItem())

            // WHEN
            tested.perform(
                DynamicSelectPlanViewModel.Action.SelectPaidPlan(
                    testSelectedPlanPaid,
                    billingResult
                )
            )

            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            assertEquals(
                DynamicSelectPlanViewModel.State.PaidPlanSelected(
                    testSelectedPlanPaid,
                    billingResult
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `cannot fetch dynamic plans`() = coroutinesTest {
        // GIVEN
        val throwable = ApiException(ApiResult.Error.Timeout(false))
        coEvery { getDynamicPlans(any()) } throws throwable
        coEvery { canUpgrade.invoke(any()) } returns false

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            val error = assertIs<DynamicSelectPlanViewModel.State.Error>(awaitItem())
            assertEquals(throwable, error.error)
        }
    }

    @Test
    fun `perform load action`() = coroutinesTest {
        // GIVEN
        coEvery { canUpgrade.invoke(any()) } returns true

        // WHEN
        tested.state.test {
            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            assertEquals(DynamicSelectPlanViewModel.State.Idle, awaitItem())

            // WHEN
            tested.perform(DynamicSelectPlanViewModel.Action.Load)

            // THEN
            assertEquals(DynamicSelectPlanViewModel.State.Loading, awaitItem())
            assertEquals(DynamicSelectPlanViewModel.State.Idle, awaitItem())
        }
    }

    @Test
    fun `enqueues screen view`() {
        // GIVEN
        coEvery { canUpgrade.invoke(any()) } returns true

        // WHEN
        tested.onScreenView()

        // THEN
        val dataSlot = slot<ObservabilityData>()
        verify { observabilityManager.enqueue(capture(dataSlot), any()) }
        assertIs<CheckoutScreenViewTotal>(dataSlot.captured).let {
            assertEquals(
                CheckoutScreenViewTotal.ScreenId.dynamicPlanSelection,
                it.Labels.screen_id
            )
        }
    }
}
