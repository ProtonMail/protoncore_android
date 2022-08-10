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
import io.mockk.coVerify
import io.mockk.mockk
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetAvailablePaymentMethods
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.paymentcommon.domain.entity.Subscription
import me.proton.core.paymentcommon.domain.entity.SubscriptionManagement
import me.proton.core.paymentcommon.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.paymentcommon.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.MASK_MAIL
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.plan.domain.usecase.GetPlanDefault
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.usecase.GetUser
import me.proton.core.usersettings.domain.entity.Organization
import me.proton.core.usersettings.domain.usecase.GetOrganization
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpgradePlansViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val getAvailablePaymentProviders = mockk<GetAvailablePaymentProviders>(relaxed = true)
    private val getPlansUseCase = mockk<GetPlans>()
    private val getPlanDefaultUseCase = mockk<GetPlanDefault>(relaxed = true)
    private val getOrganizationUseCase = mockk<GetOrganization>(relaxed = true)
    private val getUserUseCase = mockk<GetUser>(relaxed = true)
    private val getPaymentMethodsUseCase = mockk<GetAvailablePaymentMethods>(relaxed = true)
    private val getSubscriptionUseCase = mockk<GetCurrentSubscription>(relaxed = true)
    private val paymentOrchestrator = mockk<PaymentsOrchestrator>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
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
        enabled = true,
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
        maxDomains = 0,
        maxAddresses = 1,
        maxCalendars = 0,
        maxSpace = 1,
        maxMembers = 1,
        maxVPN = 0,
        services = 0,
        features = 0,
        quantity = 0,
        enabled = true,
        maxTier = 0
    )

    private val testOrganization = Organization(
        userId = testUserId,
        email = "test-email",
        name = "test-name",
        theme = "test-theme",
        flags = 1,
        displayName = "test-display-name",
        planName = "test-plan-name",
        vpnPlanName = null,
        twoFactorGracePeriod = null,
        maxDomains = 1,
        maxAddresses = 10,
        maxSpace = 100,
        maxMembers = 2,
        maxVPN = null,
        features = 2,
        usedDomains = 1,
        usedAddresses = 2,
        usedSpace = 2,
        assignedSpace = 5,
        usedMembers = 2,
        usedVPN = 0,
        hasKeys = 0,
        toMigrate = 1,
        maxCalendars = 0,
        usedCalendars = 0
    )

    private val testUser = User(
        userId = testUserId,
        email = null,
        name = "test-username",
        displayName = null,
        currency = "test-currency",
        credit = 0,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        keys = emptyList()
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
        ),
        external = SubscriptionManagement.PROTON_MANAGED
    )
    // endregion

    private lateinit var viewModel: UpgradePlansViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getPlanDefaultUseCase.invoke(any()) } returns testDefaultPlan
        coEvery { getOrganizationUseCase.invoke(any(), true) } returns testOrganization
        coEvery { getUserUseCase.invoke(any(), true) } returns testUser
        coEvery { getPaymentMethodsUseCase.invoke(any()) } returns emptyList()
        coEvery { getAvailablePaymentProviders.invoke() } returns setOf(PaymentProvider.ProtonPayment)

        coEvery { getPlansUseCase.invoke(testUserId) } returns listOf(
            testPlan
        )

        viewModel = UpgradePlansViewModel(
            getAvailablePaymentProviders,
            getPlansUseCase,
            getPlanDefaultUseCase,
            getSubscriptionUseCase,
            getOrganizationUseCase,
            getUserUseCase,
            getPaymentMethodsUseCase,
            true,
            paymentOrchestrator,
        )
    }

    @Test
    fun `get plans for upgrade currently free success handled correctly`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            coVerify(exactly = 1) { getPlanDefaultUseCase(testUserId) }
            assertEquals(1, plansStatus.plans.size)
            assertEquals("plan-name-1", plansStatus.plans[0].name)
        }
    }

    @Test
    fun `get plans for upgrade currently free current plan success handled correctly`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
        viewModel.subscribedPlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<UpgradePlansViewModel.SubscribedPlansState.Idle>(awaitItem())
            assertIs<UpgradePlansViewModel.SubscribedPlansState.Processing>(awaitItem())
            val currentPlan = awaitItem()
            assertTrue(currentPlan is UpgradePlansViewModel.SubscribedPlansState.Success.SubscribedPlans)
            assertEquals(1, currentPlan.subscribedPlans.size)
            val plan = currentPlan.subscribedPlans[0]
            assertEquals("plan-default", plan.name)
            assertEquals(1, plan.storage)
        }
    }

    @Test
    fun `get plans for upgrade currently free payments off handled`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
        coEvery { getAvailablePaymentProviders.invoke() } returns emptySet()
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(0, plansStatus.plans.size)
            coVerify(exactly = 1) { getPlanDefaultUseCase(any()) }
        }
    }

    @Test
    fun `get plans for upgrade currently free no active plans handled correctly`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
        val plansRepository = mockk<PlansRepository>(relaxed = true)
        coEvery { plansRepository.getPlans(testUserId) } returns listOf(testPlan.copy(enabled = false))
        viewModel = UpgradePlansViewModel(
            getAvailablePaymentProviders,
            GetPlans(plansRepository = plansRepository, product = Product.Mail, productExclusivePlans = false),
            getPlanDefaultUseCase,
            getSubscriptionUseCase,
            getOrganizationUseCase,
            getUserUseCase,
            getPaymentMethodsUseCase,
            true,
            paymentOrchestrator,
        )
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(0, plansStatus.plans.size)
        }
    }

    @Test
    fun `get plans for upgrade currently free no active plans handled correctly for product and combo`() =
        coroutinesTest {
            coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
            val plansRepository = mockk<PlansRepository>(relaxed = true)
            coEvery { plansRepository.getPlans(testUserId) } returns listOf(
                testPlan.copy(services = MASK_MAIL),
                testPlan.copy(services = 5)
            )
            viewModel = UpgradePlansViewModel(
                getAvailablePaymentProviders,
                GetPlans(plansRepository = plansRepository, product = Product.Mail, productExclusivePlans = false),
                getPlanDefaultUseCase,
                getSubscriptionUseCase,
                getOrganizationUseCase,
                getUserUseCase,
                getPaymentMethodsUseCase,
                true,
                paymentOrchestrator,
            )
            viewModel.availablePlansState.test {
                // WHEN
                viewModel.getCurrentSubscribedPlans(testUserId)
                // THEN
                assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
                assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
                val plansStatus = awaitItem()
                assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
                assertEquals(2, plansStatus.plans.size)
            }
        }

    @Test
    fun `get plans for upgrade currently paid plan success handled correctly`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns testSubscription
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(0, plansStatus.plans.size)
        }
    }

    @Test
    fun `get plans for upgrade currently paid payments off`() = coroutinesTest {
        coEvery { getSubscriptionUseCase.invoke(testUserId) } returns testSubscription
        coEvery { getAvailablePaymentProviders.invoke() } returns emptySet()
        viewModel.availablePlansState.test {
            // WHEN
            viewModel.getCurrentSubscribedPlans(testUserId)
            // THEN
            assertIs<BasePlansViewModel.PlanState.Idle>(awaitItem())
            assertIs<BasePlansViewModel.PlanState.Processing>(awaitItem())
            val plansStatus = awaitItem()
            assertTrue(plansStatus is BasePlansViewModel.PlanState.Success.Plans)
            assertEquals(0, plansStatus.plans.size)
            coVerify(exactly = 1) { getPlanDefaultUseCase.invoke(any()) }
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

    @Test
    fun `get plans for upgrade currently free no active plans handled correctly for product and NOT combo`() =
        coroutinesTest {
            coEvery { getSubscriptionUseCase.invoke(testUserId) } returns null
            val plansRepository = mockk<PlansRepository>(relaxed = true)
            coEvery { plansRepository.getPlans(testUserId) } returns listOf(
                testPlan.copy(services = MASK_MAIL),
                testPlan.copy(services = 5)
            )
            viewModel = UpgradePlansViewModel(
                getAvailablePaymentProviders,
                GetPlans(plansRepository = plansRepository, product = Product.Mail, productExclusivePlans = true),
                getPlanDefaultUseCase,
                getSubscriptionUseCase,
                getOrganizationUseCase,
                getUserUseCase,
                getPaymentMethodsUseCase,
                true,
                paymentOrchestrator,
            )
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
