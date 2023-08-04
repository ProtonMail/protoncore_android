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

package me.proton.core.plan.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.plan.data.api.PlansApi
import me.proton.core.plan.domain.PlanIconsEndpointProvider
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanState
import me.proton.core.plan.domain.entity.DynamicPlanType
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.PlanOffer
import me.proton.core.plan.domain.entity.PlanOfferPricing
import me.proton.core.plan.domain.entity.PlanPricing
import me.proton.core.test.kotlin.TestDispatcherProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PlansRepositoryImplTest {
    // region mocks
    private val endpointProvider = mockk<PlanIconsEndpointProvider> {
        every { get() } returns "endpoint"
    }
    private val sessionProvider = mockk<SessionProvider>(relaxed = true)
    private val apiFactory = mockk<ApiManagerFactory>(relaxed = true)
    private val apiManager = mockk<ApiManager<PlansApi>>(relaxed = true)
    private lateinit var apiProvider: ApiProvider
    private lateinit var repository: PlansRepositoryImpl
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testUserId = "test-user-id"
    // endregion

    private val dispatcherProvider = TestDispatcherProvider()

    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { sessionProvider.getSessionId(UserId(testUserId)) } returns SessionId(testSessionId)
        apiProvider = ApiProvider(apiFactory, sessionProvider, dispatcherProvider)
        every {
            apiFactory.create(
                interfaceClass = PlansApi::class
            )
        } returns apiManager
        every {
            apiFactory.create(
                SessionId(testSessionId),
                interfaceClass = PlansApi::class
            )
        } returns apiManager
        repository = PlansRepositoryImpl(apiProvider, endpointProvider)
    }

    @Test
    fun `plans return data no user returns non empty`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plans = listOf(
            Plan(
                id = "plan-id-1",
                type = 1,
                cycle = 1,
                name = "Plan 1",
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
        )
        coEvery { apiManager.invoke<List<Plan>>(any()) } returns ApiResult.Success(plans)
        // WHEN
        val plansResponse = repository.getPlans(sessionUserId = null)
        // THEN
        assertNotNull(plansResponse)
        assertEquals(1, plansResponse.size)
        val plan = plans[0]
        assertEquals("plan-id-1", plan.id)
        assertNotNull(plan.pricing)
        assertNull(plan.defaultPricing)
    }

    @Test
    fun `plans return data with offers no user returns non empty`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plans = listOf(
            Plan(
                id = "plan-id-1",
                type = 1,
                cycle = 1,
                name = "Plan 1",
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
                ),
                defaultPricing = PlanPricing(
                    1, 10, 20
                ),
                offers = listOf(
                    PlanOffer("test-offer", 1000, 2000, PlanOfferPricing(
                        1, 10, 20
                    ))
                )
            )
        )
        coEvery { apiManager.invoke<List<Plan>>(any()) } returns ApiResult.Success(plans)
        // WHEN
        val plansResponse = repository.getPlans(sessionUserId = null)
        // THEN
        assertNotNull(plansResponse)
        assertEquals(1, plansResponse.size)
        val plan = plans[0]
        assertEquals("plan-id-1", plan.id)
        assertNotNull(plan.defaultPricing)
        assertNotNull(plan.pricing)
        assertEquals(plan.pricing!!.monthly, plan.defaultPricing!!.monthly)
        assertNotNull(plan.offers)
        assertEquals(1, plan.offers!!.size)
    }

    @Test
    fun `plans return data no user returns empty list`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plans = emptyList<Plan>()
        coEvery { apiManager.invoke<List<Plan>>(any()) } returns ApiResult.Success(plans)
        // WHEN
        val plansResponse = repository.getPlans(sessionUserId = null)
        // THEN
        assertNotNull(plansResponse)
        assertEquals(0, plansResponse.size)
    }

    @Test
    fun `plans return data for user id`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plans = listOf(
            Plan(
                id = "plan-id-1",
                type = 1,
                cycle = 1,
                name = "Plan 1",
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
                ),
                defaultPricing = PlanPricing(
                    1, 10, 20
                ),
                offers = emptyList()
            )
        )
        coEvery { apiManager.invoke<List<Plan>>(any()) } returns ApiResult.Success(plans)
        // WHEN
        val plansResponse = repository.getPlans(sessionUserId = SessionUserId(testUserId))
        // THEN
        assertNotNull(plansResponse)
        assertEquals(1, plansResponse.size)
        val plan = plans[0]
        assertEquals("plan-id-1", plan.id)
    }

    @Test
    fun `plans return data for user id returns empty list`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plans = emptyList<Plan>()
        coEvery { apiManager.invoke<List<Plan>>(any()) } returns ApiResult.Success(plans)
        // WHEN
        val plansResponse = repository.getPlans(sessionUserId = SessionUserId(testUserId))
        // THEN
        assertNotNull(plansResponse)
        assertEquals(0, plansResponse.size)
    }

    @Test
    fun `plans return error`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        coEvery { apiManager.invoke<List<Plan>>(any()) } returns ApiResult.Error.Http(
            httpCode = 401, message = "test http error", proton = ApiResult.Error.ProtonData(1, "test error")
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            repository.getPlans(sessionUserId = SessionUserId(testUserId))
        }
        // THEN
        assertEquals("test error", throwable.message)
        val error = throwable.error as? ApiResult.Error.Http
        assertNotNull(error)
        assertEquals(1, error.proton?.code)
    }

    @Test
    fun `plans default return data`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val planDefault = Plan(
            id = "plan-id-1",
            type = 1,
            cycle = 1,
            name = "Plan 1",
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
        coEvery { apiManager.invoke<Plan>(any()) } returns ApiResult.Success(planDefault)
        // WHEN
        val plansResponse = repository.getPlansDefault(sessionUserId = null)
        // THEN
        assertNotNull(plansResponse)
        assertEquals("plan-id-1", plansResponse.id)
    }

    @Test
    fun `get dynamic plans`() = runTest(dispatcherProvider.Main) {
        // GIVEN
        val plan = DynamicPlan(
            name = "name",
            order = 0,
            state = DynamicPlanState.Available,
            title = "title",
            type = IntEnum(DynamicPlanType.Primary.code, DynamicPlanType.Primary)
        )
        val plans = listOf(plan)
        coEvery { apiManager.invoke<List<DynamicPlan>>(any()) } returns ApiResult.Success(plans)

        // WHEN
        val result = repository.getDynamicPlans(sessionUserId = null)

        // THEN
        assertContentEquals(plans, result)
    }
}
