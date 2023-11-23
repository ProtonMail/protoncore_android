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

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.usecase.GetDynamicPlansAdjustedPrices
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.plan.presentation.usecase.ObserveUserId
import me.proton.core.plan.presentation.viewmodel.DynamicPlanListViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicPlanListViewModel.State
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicPlanListViewModelTest : CoroutinesTest by CoroutinesTest() {

    private val userId1 = UserId("userId")
    private val userId2 = UserId("another")
    private val userIdAbsent = UserId("absent")

    private val dynamicPlan = mockk<DynamicPlan> {
        every { instances } returns emptyMap()
        every { type } returns null
    }
    private val plans = listOf(dynamicPlan)

    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
    private val getDynamicPlans = mockk<GetDynamicPlansAdjustedPrices> {
        coEvery { this@mockk.invoke(any()) } returns DynamicPlans(null, plans)
    }
    private val mutableUserIdFlow = MutableStateFlow<UserId?>(userId1)
    private val observeUserId = mockk<ObserveUserId>(relaxed = true) {
        coEvery { this@mockk.invoke() } returns mutableUserIdFlow
    }

    private lateinit var tested: DynamicPlanListViewModel

    @BeforeTest
    fun setUp() {
        tested = DynamicPlanListViewModel(observabilityManager, observeUserId, getDynamicPlans)
    }

    @Test
    fun `get plans happy path`() = coroutinesTest {
        // GIVEN
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SetCurrency("CHF"))
        // WHEN
        tested.state.test {
            // THEN
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Success>(state)
            assertContentEquals(plans, state.plans)
        }
    }

    @Test
    fun `get plans error`() = coroutinesTest {
        // GIVEN
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SetCurrency("CHF"))

        val apiException = ApiException(ApiResult.Error.Http(500, "Server error"))
        coEvery { getDynamicPlans(any()) } throws apiException

        // WHEN
        tested.state.test {
            // THEN
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Error>(state)
            assertEquals(apiException, state.error)
        }
    }

    @Test
    fun `get plans userId1 return CHF`() = coroutinesTest {
        // GIVEN
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SetCurrency("CHF"))
        // WHEN
        tested.state.test {
            // THEN
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Success>(state)
            assertEquals("CHF", state.filter.currency)
        }
    }

    @Test
    fun `get plans userId2 return USD`() = coroutinesTest {
        // GIVEN
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId2)))
        tested.perform(Action.SetCurrency("USD"))
        // WHEN
        tested.state.test {
            // THEN
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Success>(state)
            assertEquals("USD", state.filter.currency)
        }
    }

    @Test
    fun `get plans userAbsent return success`() = coroutinesTest {
        // GIVEN
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userIdAbsent)))
        tested.perform(Action.SetCurrency("USD"))
        // WHEN
        tested.state.test {
            // THEN
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Success>(state)
            assertEquals("USD", state.filter.currency)
        }
    }
}
