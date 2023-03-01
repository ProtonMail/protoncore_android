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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import me.proton.core.domain.entity.Product
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.domain.entity.MASK_MAIL
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.plan.domain.usecase.GetPlanDefault
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.plan.presentation.entity.PlanDetailsItem
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.flowTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignupPlansViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private val getAvailablePaymentProviders = mockk<GetAvailablePaymentProviders>(relaxed = true)
    private val getPlansUseCase = mockk<GetPlans>()
    private lateinit var getPlanDefaultUseCaseSpy: GetPlanDefault
    private val plansRepository = mockk<PlansRepository>(relaxed = true)
    private val paymentOrchestrator = mockk<PaymentsOrchestrator>(relaxed = true)
    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
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
        maxSpace = 2,
        maxMembers = 1,
        maxVPN = 1,
        services = 0,
        features = 1,
        quantity = 1,
        maxTier = 1,
        enabled = true,
        pricing = PlanPricing(
            1, 10, 20
        )
    )

    private val testPlanZeroValueFields = Plan(
        id = "plan-name-1",
        type = 1,
        cycle = 1,
        name = "plan-name-1",
        title = "Plan Title 1",
        currency = "CHF",
        amount = 10,
        maxDomains = 0,
        maxAddresses = 0,
        maxCalendars = 0,
        maxSpace = 2,
        maxMembers = 0,
        maxVPN = 0,
        services = 0,
        features = 1,
        quantity = 1,
        maxTier = 1,
        enabled = true,
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
        maxDomains = 1,
        maxAddresses = 1,
        maxCalendars = 1,
        maxSpace = 1,
        maxMembers = 1,
        maxVPN = 2,
        services = 0,
        features = 0,
        quantity = 0,
        maxTier = 0,
        enabled = true
    )
    // endregion

    private lateinit var viewModel: SignupPlansViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { plansRepository.getPlansDefault(null) } returns testDefaultPlan
        getPlanDefaultUseCaseSpy = spyk(GetPlanDefault(plansRepository))
        coEvery { getAvailablePaymentProviders.invoke() } returns setOf(PaymentProvider.CardPayment)

        viewModel =
            SignupPlansViewModel(
                getAvailablePaymentProviders = getAvailablePaymentProviders,
                getPlans = getPlansUseCase,
                getPlanDefault = getPlanDefaultUseCaseSpy,
                supportPaidPlans = true,
                paymentsOrchestrator = paymentOrchestrator,
                observabilityManager = observabilityManager
            )
    }

    @Test
    fun `get plans for signup success handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan,
            testPlan.copy(id = "plan-name-2", name = "plan-name-2")
        )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(3, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            val planThree = plansStatus.plans[2]
            coVerify { getPlanDefaultUseCaseSpy.invoke(null) }
            assertEquals("plan-default", planThree.name)
            assertEquals(1, planThree.storage)
            assertEquals(2, planOne.storage)
            assertEquals(2, planTwo.storage)
            assertEquals("plan-name-1", planOne.name)
            assertEquals("plan-name-2", planTwo.name)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plan with 0 value fields for signup success handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlanZeroValueFields
        )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(2, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            coVerify { getPlanDefaultUseCaseSpy.invoke(null) }
            assertTrue(planOne is PlanDetailsItem.PaidPlanDetailsItem)
            assertTrue(planTwo is PlanDetailsItem.FreePlanDetailsItem)
            assertEquals("plan-name-1", planOne.name)
            assertEquals("plan-default", planTwo.name)
            assertEquals(1, planOne.addresses)
            assertEquals(1, planOne.calendars)
            assertEquals(1, planOne.domains)
            assertEquals(2, planOne.connections)
            assertEquals(1, planOne.members)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plan with non-0 value fields for signup success handled correctly`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan
        )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(2, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            coVerify { getPlanDefaultUseCaseSpy.invoke(null) }
            assertTrue(planOne is PlanDetailsItem.PaidPlanDetailsItem)
            assertTrue(planTwo is PlanDetailsItem.FreePlanDetailsItem)
            assertEquals("plan-name-1", planOne.name)
            assertEquals("plan-default", planTwo.name)
            assertEquals(1, planOne.addresses)
            assertEquals(1, planOne.calendars)
            assertEquals(1, planOne.domains)
            assertEquals(1, planOne.connections)
            assertEquals(1, planOne.members)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup reward storage respected`() = coroutinesTest {
        coEvery { plansRepository.getPlansDefault(null) } returns testDefaultPlan.copy(maxRewardSpace = 2)
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan,
            testPlan.copy(id = "plan-name-2", name = "plan-name-2")
        )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(3, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            val planThree = plansStatus.plans[2]
            coVerify { getPlanDefaultUseCaseSpy.invoke(null) }
            assertEquals("plan-default", planThree.name)
            assertEquals(2, planThree.storage)
            assertEquals("plan-name-1", planOne.name)
            assertEquals("plan-name-2", planTwo.name)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup payments off handled correctly`() = coroutinesTest {
        coEvery { getAvailablePaymentProviders.invoke() } returns emptySet()
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan,
            testPlan.copy(id = "plan-name-2", name = "plan-name-2")
        )
        viewModel =
            SignupPlansViewModel(
                getAvailablePaymentProviders,
                getPlansUseCase,
                getPlanDefaultUseCaseSpy,
                true,
                paymentOrchestrator,
                observabilityManager = observabilityManager
            )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(0, plansStatus.plans.size)
            coVerify(exactly = 0) { getPlanDefaultUseCaseSpy.invoke(any()) }
            coVerify(exactly = 0) { getPlansUseCase.invoke(any()) }
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup only active and for Product shown`() = coroutinesTest {
        val plansRepository = mockk<PlansRepository>(relaxed = true)
        coEvery { plansRepository.getPlans(null) } returns listOf(
            testPlan.copy(enabled = false),
            testPlan.copy(id = "plan-name-2", name = "plan-name-2", services = MASK_MAIL)
        )
        viewModel =
            SignupPlansViewModel(
                getAvailablePaymentProviders,
                GetPlans(plansRepository = plansRepository, product = Product.Mail, productExclusivePlans = false),
                getPlanDefaultUseCaseSpy,
                true,
                paymentOrchestrator,
                observabilityManager = observabilityManager
            )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(2, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            assertEquals("plan-default", planTwo.name)
            assertEquals("plan-name-2", planOne.name)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup only active, for Product and combo plans shown`() = coroutinesTest {
        val plansRepository = mockk<PlansRepository>(relaxed = true)
        coEvery { plansRepository.getPlans(null) } returns listOf(
            testPlan.copy(enabled = false),
            testPlan.copy(id = "plan-name-2", name = "plan-name-2", services = MASK_MAIL),
            testPlan.copy(id = "plan-name-3", name = "plan-name-3", services = 5)
        )
        viewModel =
            SignupPlansViewModel(
                getAvailablePaymentProviders,
                GetPlans(plansRepository = plansRepository, product = Product.Mail, productExclusivePlans = false),
                getPlanDefaultUseCaseSpy,
                true,
                paymentOrchestrator,
                observabilityManager = observabilityManager
            )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(3, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            val planThree = plansStatus.plans[2]
            assertEquals("plan-default", planThree.name) // default is always last
            assertEquals("plan-name-3", planOne.name)
            assertEquals("plan-name-2", planTwo.name)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup only active, for Product and combo plans NOT shown`() = coroutinesTest {
        val plansRepository = mockk<PlansRepository>(relaxed = true)
        coEvery { plansRepository.getPlans(null) } returns listOf(
            testPlan.copy(enabled = false),
            testPlan.copy(id = "plan-name-2", name = "plan-name-2", services = MASK_MAIL),
            testPlan.copy(id = "plan-name-3", name = "plan-name-3", services = 5)
        )
        viewModel =
            SignupPlansViewModel(
                getAvailablePaymentProviders,
                GetPlans(plansRepository = plansRepository, product = Product.Mail, productExclusivePlans = true),
                getPlanDefaultUseCaseSpy,
                true,
                paymentOrchestrator,
                observabilityManager = observabilityManager
            )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(2, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            assertEquals("plan-default", planTwo.name) // default is always last
            assertEquals("plan-name-2", planOne.name)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup 15`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan,
            testPlan.copy(id = "plan-name-2", name = "plan-name-2", cycle = 15)
        )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(3, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            val planThree = plansStatus.plans[2]
            coVerify { getPlanDefaultUseCaseSpy.invoke(null) }
            assertEquals("plan-default", planThree.name)
            assertEquals("plan-name-1", planOne.name)
            assertEquals("plan-name-2", planTwo.name)
            assertEquals(PlanCycle.MONTHLY, (planOne as PlanDetailsItem.PaidPlanDetailsItem).cycle)
            assertEquals(PlanCycle.OTHER, (planTwo as PlanDetailsItem.PaidPlanDetailsItem).cycle)
            assertEquals(15, planTwo.cycle!!.cycleDurationMonths)
            assertIs<PlanDetailsItem.FreePlanDetailsItem>(planThree)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }

    @Test
    fun `get plans for signup 100`() = coroutinesTest {
        coEvery { getPlansUseCase.invoke(any()) } returns listOf(
            testPlan.copy(id = "plan-name-2", name = "plan-name-2", cycle = 100)
        )
        val job = flowTest(viewModel.availablePlansState) {
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(2, plansStatus.plans.size)
            val planOne = plansStatus.plans[0]
            val planTwo = plansStatus.plans[1]
            coVerify { getPlanDefaultUseCaseSpy.invoke(null) }
            assertEquals("plan-default", planTwo.name)
            assertEquals("plan-name-2", planOne.name)
            assertEquals(PlanCycle.OTHER, (planOne as PlanDetailsItem.PaidPlanDetailsItem).cycle)
            assertEquals(100, planOne.cycle!!.cycleDurationMonths)
            assertIs<PlanDetailsItem.FreePlanDetailsItem>(planTwo)
        }

        // WHEN
        viewModel.getAllPlansForSignup()
        job.join()
    }
}
