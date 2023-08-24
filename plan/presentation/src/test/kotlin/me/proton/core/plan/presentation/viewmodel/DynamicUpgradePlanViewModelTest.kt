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
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.usecase.CanUpgradeFromMobile
import me.proton.core.plan.presentation.usecase.CheckUnredeemedGooglePurchase
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.Action
import me.proton.core.plan.presentation.viewmodel.DynamicUpgradePlanViewModel.State
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test

class DynamicUpgradePlanViewModelTest : CoroutinesTest by CoroutinesTest() {

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
    private val checkUnredeemedGooglePurchase = mockk<CheckUnredeemedGooglePurchase> {
        coEvery { this@mockk.invoke(any()) } returns null
    }
    private val canUpgradeFromMobile = mockk<CanUpgradeFromMobile> {
        coEvery { this@mockk.invoke(any()) } returns true
    }

    private lateinit var viewModel: DynamicUpgradePlanViewModel

    @Before
    fun before() {
        viewModel = DynamicUpgradePlanViewModel(
            observabilityManager,
            accountManager,
            checkUnredeemedGooglePurchase,
            canUpgradeFromMobile
        )
    }

    @Test
    fun returnError() = runTest {
        // Given
        coEvery { canUpgradeFromMobile.invoke(any()) } throws ApiException(ApiResult.Error.NoInternet())
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.Error>(awaitItem())
        }
    }

    @Test
    fun retryOnError() = runTest {
        // Given
        coEvery { canUpgradeFromMobile.invoke(any()) } throws ApiException(ApiResult.Error.NoInternet())
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.Error>(awaitItem())
            // Given
            coEvery { canUpgradeFromMobile.invoke(any()) } returns false
            viewModel.perform(Action.Load)
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.UpgradeNotAvailable>(awaitItem())
        }
    }

    @Test
    fun returnUpgradeNotAvailableWhenCannotUpgradeFromMobile() = runTest {
        // Given
        coEvery { canUpgradeFromMobile.invoke(any()) } returns false
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.UpgradeNotAvailable>(awaitItem())
        }
    }

    @Test
    fun returnUpgradeAvailableWhenCanUpgradeFromMobileAndNoUnredeemed() = runTest {
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.UpgradeAvailable>(awaitItem())
        }
    }

    @Test
    fun returnUnredeemed() = runTest {
        // Given
        coEvery { checkUnredeemedGooglePurchase.invoke(any()) } returns mockk()
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.UnredeemedPurchase>(awaitItem())
        }
    }

    @Test
    fun returnUnredeemedForUser2() = runTest {
        // Given
        coEvery { checkUnredeemedGooglePurchase.invoke(userId2) } returns mockk()
        viewModel.perform(Action.SetUser(DynamicUser.ByUserId(userId2)))
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.UnredeemedPurchase>(awaitItem())
        }
    }

    @Test
    fun returnUpgradeAvailableForUser1() = runTest {
        // Given
        coEvery { checkUnredeemedGooglePurchase.invoke(userId2) } returns mockk()
        viewModel.perform(Action.SetUser(DynamicUser.ByUserId(userId1)))
        // When
        viewModel.state.test {
            // Then
            assertIs<State.Loading>(awaitItem())
            assertIs<State.UpgradeAvailable>(awaitItem())
        }
    }
}
