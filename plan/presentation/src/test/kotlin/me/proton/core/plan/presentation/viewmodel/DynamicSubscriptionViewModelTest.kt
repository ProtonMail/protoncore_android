/*
 * Copyright (c) 2023 Proton Technologies AG
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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.entity.DynamicSubscription
import me.proton.core.payment.domain.usecase.CanUpgradeFromMobile
import me.proton.core.payment.domain.usecase.GetDynamicSubscription
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
    private val userIdAbsent = UserId("absent")
    private val mutablePrimaryUserIdFlow = MutableStateFlow<UserId?>(userId1)
    private val subscription = mockk<DynamicSubscription>()

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
    private val getDynamicSubscription = mockk<GetDynamicSubscription> {
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
            observabilityManager,
            accountManager,
            getDynamicSubscription,
            canUpgradeFromMobile
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
        mutablePrimaryUserIdFlow.emit(null)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.UserNotExist>(awaitItem())

            mutablePrimaryUserIdFlow.emit(userId1)
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateLoadingWhenNoPrimaryUserIdThenPerformSetUserId() = runTest {
        // Given
        mutablePrimaryUserIdFlow.emit(null)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.UserNotExist>(awaitItem())

            viewModel.perform(DynamicSubscriptionViewModel.Action.SetUser(DynamicUser.ByUserId(userId1)))
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateSuccessThenPerformSetUserId() = runTest {
        // Given
        mutablePrimaryUserIdFlow.emit(userId1)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())

            viewModel.perform(DynamicSubscriptionViewModel.Action.SetUser(DynamicUser.ByUserId(userId2)))
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())
        }
    }

    @Test
    fun observeStateSuccessThenPerformSetUserIdAndUserNotExist() = runTest {
        // Given
        mutablePrimaryUserIdFlow.emit(userId1)
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Success>(awaitItem())

            viewModel.perform(DynamicSubscriptionViewModel.Action.SetUser(DynamicUser.ByUserId(userIdAbsent)))
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.UserNotExist>(awaitItem())
        }
    }

    @Test
    fun observeStateError() = runTest {
        // Given
        coEvery { getDynamicSubscription.invoke(any()) } throws Exception()
        // When
        viewModel.state.test {
            // Then
            assertIs<DynamicSubscriptionViewModel.State.Loading>(awaitItem())
            assertIs<DynamicSubscriptionViewModel.State.Error>(awaitItem())
        }
    }
}
