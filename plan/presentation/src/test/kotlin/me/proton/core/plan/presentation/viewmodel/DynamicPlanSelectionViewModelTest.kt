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
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.usecase.GetDynamicPlansAdjustedPrices
import me.proton.core.plan.domain.usecase.ObserveUserCurrency
import me.proton.core.plan.presentation.entity.DynamicUser
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.entity.bundlePlan
import me.proton.core.plan.presentation.entity.mailPlusPlan
import me.proton.core.plan.presentation.usecase.ObserveUserId
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.State
import me.proton.core.test.kotlin.CoroutinesTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicPlanSelectionViewModelTest : CoroutinesTest by CoroutinesTest() {

    private val selectedPlanFree = mockk<SelectedPlan> {
        every { free } returns true
    }
    private val selectedPlanPaid = mockk<SelectedPlan> {
        every { free } returns false
    }

    private val billingResult = mockk<BillingResult> {
        every { paySuccess } returns true
    }

    private val userId1 = UserId("userId")

    private val mutableUserIdFlow = MutableStateFlow<UserId?>(userId1)
    private val mutableUserCurrencyFlow = MutableStateFlow("CHF")

    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
    private val observeUserId = mockk<ObserveUserId>(relaxed = true) {
        coEvery { this@mockk.invoke() } returns mutableUserIdFlow
    }
    private val observeUserCurrency = mockk<ObserveUserCurrency>(relaxed = true) {
        coEvery { this@mockk.invoke(any()) } returns mutableUserCurrencyFlow
    }
    private val getDynamicPlans = mockk<GetDynamicPlansAdjustedPrices>(relaxed = true) {
        coEvery { this@mockk.invoke(any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(mailPlusPlan, bundlePlan)
        )
    }

    private lateinit var tested: DynamicPlanSelectionViewModel

    @BeforeTest
    fun setUp() {
        tested = DynamicPlanSelectionViewModel(
            observabilityManager = observabilityManager,
            observeUserId = observeUserId,
            observeUserCurrency = observeUserCurrency,
            getDynamicPlans = getDynamicPlans
        )
    }

    @Test
    fun stateReturnCorrectCycleAndCurrencies() = runTest {
        // Given
        mutableUserIdFlow.emit(userId1)
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Idle>(state)
            assertEquals(expected = listOf("CHF", "EUR", "USD"), actual = state.planFilters.currencies)
            assertEquals(expected = listOf(1, 12), actual = state.planFilters.cycles)
        }
    }

    @Test
    fun onGetDynamicPlansErrorReturnEmptyCycleAndCurrencies() = runTest {
        // Given
        coEvery { getDynamicPlans.invoke(any()) } throws Throwable()
        mutableUserIdFlow.emit(userId1)
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Idle>(state)
            assertEquals(expected = emptyList(), actual = state.planFilters.currencies)
            assertEquals(expected = emptyList(), actual = state.planFilters.cycles)
        }
    }

    @Test
    fun stateReturnCurrenciesForUser1() = runTest {
        // Given
        mutableUserIdFlow.emit(userId1)
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Idle>(state)
            assertEquals(expected = listOf("CHF", "EUR", "USD"), actual = state.planFilters.currencies)
        }
    }

    @Test
    fun performSelectFreePlanThenStateIsFree() = runTest {
        // Given
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SelectPlan(selectedPlanFree))
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Free>(state)
            assertEquals(expected = selectedPlanFree, actual = state.selectedPlan)
        }
    }

    @Test
    fun performSelectPaidPlanThenStateIsBilling() = runTest {
        // Given
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SelectPlan(selectedPlanPaid))
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Billing>(state)
            assertEquals(expected = selectedPlanPaid, actual = state.selectedPlan)
        }
    }

    @Test
    fun performSetBillingResultThenStateIsBilled() = runTest {
        // Given
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SelectPlan(selectedPlanPaid))
        tested.perform(Action.SetBillingResult(billingResult))
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Billed>(state)
            assertEquals(expected = selectedPlanPaid, actual = state.selectedPlan)
            assertEquals(expected = billingResult, actual = state.billingResult)
        }
    }

    @Test
    fun performSetBillingCanceledThenStateIsIdle() = runTest {
        // Given
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        tested.perform(Action.SelectPlan(selectedPlanPaid))
        tested.perform(Action.SetBillingCanceled)
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.Idle>(awaitItem())
        }
    }
}
