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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.presentation.entity.BillingResult
import me.proton.core.plan.presentation.entity.SelectedPlan
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicPlanSelectionViewModel.State
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.UserManager
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DynamicPlanSelectionViewModelTest : CoroutinesTest by CoroutinesTest() {

    private val savedLocale: Locale = Locale.getDefault()

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
    private val userId2 = UserId("another")
    private val userIdAbsent = UserId("absent")
    private val mutablePrimaryUserIdFlow = MutableStateFlow<UserId?>(userId1)

    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
    private val accountManager = mockk<AccountManager>(relaxed = true) {
        coEvery { this@mockk.getPrimaryUserId() } returns mutablePrimaryUserIdFlow
        coEvery { this@mockk.getAccount(any()) } answers {
            flowOf(
                when (firstArg<UserId>()) {
                    userId1 -> mockk<Account> { every { userId } returns userId1 }
                    userId2 -> mockk<Account> { every { userId } returns userId2 }
                    userIdAbsent -> null
                    else -> null
                }
            )
        }
    }
    private val userManagerManager = mockk<UserManager>(relaxed = true) {
        coEvery { this@mockk.observeUser(any()) } answers {
            flowOf(
                when (firstArg<UserId>()) {
                    userId1 -> mockk {
                        every { userId } returns userId1
                        every { currency } returns "CHF"
                    }

                    userId2 -> mockk {
                        every { userId } returns userId2
                        every { currency } returns "USD"
                    }

                    userIdAbsent -> null
                    else -> null
                }
            )
        }
    }

    private lateinit var tested: DynamicPlanSelectionViewModel

    @BeforeTest
    fun setUp() {
        Locale.setDefault(Locale.US)
        tested = DynamicPlanSelectionViewModel(observabilityManager, userManagerManager, accountManager)
    }

    @AfterTest
    fun cleanUp() {
        Locale.setDefault(savedLocale)
    }

    @Test
    fun localCurrencyIsUSD() = runTest {
        assertEquals(expected = "USD", actual = tested.localCurrency)
    }

    @Test
    fun defaultCurrencyIsUSD() = runTest {
        assertEquals(expected = "USD", actual = tested.defaultCurrency)
    }

    @Test
    fun stateReturnCurrenciesForUser1() = runTest {
        // Given
        val currencies = listOf("CHF", "EUR", "USD")
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Idle>(state)
            assertEquals(expected = currencies, actual = state.currencies)
        }
    }

    @Test
    fun stateReturnCurrenciesForUser2() = runTest {
        // Given
        val currencies = listOf("USD", "CHF", "EUR")
        tested.perform(Action.SetUser(DynamicUser.ByUserId(userId2)))
        // When
        tested.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            val state = awaitItem()
            assertIs<State.Idle>(state)
            assertEquals(expected = currencies, actual = state.currencies)
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
