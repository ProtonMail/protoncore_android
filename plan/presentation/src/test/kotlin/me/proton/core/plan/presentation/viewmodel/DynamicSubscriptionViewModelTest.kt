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
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.usecase.CanUpgradeFromMobile
import me.proton.core.plan.domain.usecase.GetDynamicSubscriptionAdjustedPrices
import me.proton.core.plan.domain.usecase.ObserveUserCurrency
import me.proton.core.plan.presentation.usecase.ObserveUserId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Before
import org.junit.Test

class DynamicSubscriptionViewModelTest : ArchTest by ArchTest(),
    CoroutinesTest by CoroutinesTest() {

    private val userId1 = UserId("userId")
    private val userId2 = UserId("another")
    private val subscription = mockk<DynamicSubscription>()

    private val mutableUserIdFlow = MutableStateFlow<UserId?>(userId1)
    private val mutableUserCurrencyFlow = MutableStateFlow("USD")

    private val observabilityManager = mockk<ObservabilityManager>(relaxed = true)
    private val observeUserId = mockk<ObserveUserId>(relaxed = true) {
        coEvery { this@mockk.invoke() } returns mutableUserIdFlow
    }
    private val observeUserCurrency = mockk<ObserveUserCurrency>(relaxed = true) {
        coEvery { this@mockk.invoke(any()) } returns mutableUserCurrencyFlow
    }
    private val getDynamicSubscriptionAdjustedPrices = mockk<GetDynamicSubscriptionAdjustedPrices> {
        coEvery { this@mockk.invoke(any()) } coAnswers {
            result("getDynamicSubscription") {
                subscription
            }
        }
    }
    private val canUpgradeFromMobile = mockk<CanUpgradeFromMobile> {
        coEvery { this@mockk.invoke(any()) } returns true
    }

    private lateinit var viewModel: DynamicSubscriptionViewModel

    @Before
    fun before() {
        viewModel = DynamicSubscriptionViewModel(
            observabilityManager = observabilityManager,
            observeUserId = observeUserId,
            observeUserCurrency = observeUserCurrency,
            getDynamicSubscriptionAdjustedPrices = getDynamicSubscriptionAdjustedPrices,
            canUpgradeFromMobile = canUpgradeFromMobile
        )
    }

    @Test
    fun observeStateSuccess() = runTest {
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateLoadingWhenNoPrimaryUserIdThenSuccess() = runTest {
        // Given
        mutableUserIdFlow.emit(null)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.UserNotExist>(awaitItem())

            mutableUserIdFlow.emit(userId1)
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateLoadingWhenNoPrimaryUserIdThenPerformSetUserId() = runTest {
        // Given
        mutableUserIdFlow.emit(null)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.UserNotExist>(awaitItem())

            mutableUserIdFlow.emit(userId1)
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateSuccessThenPerformSetUserId() = runTest {
        // Given
        mutableUserIdFlow.emit(userId1)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())

            mutableUserIdFlow.emit(userId2)
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateError() = runTest {
        // Given
        coEvery { getDynamicSubscriptionAdjustedPrices.invoke(any()) } throws Exception()
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Error>(awaitItem())
        }
    }
}
