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

package me.proton.core.accountrecovery.presentation.compose.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountrecovery.domain.usecase.StartRecovery
import me.proton.core.accountrecovery.presentation.compose.ui.Arg
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.usecase.ObserveUser
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

internal class PasswordResetDialogViewModelTest :
    ArchTest by ArchTest(),
    CoroutinesTest by CoroutinesTest() {

    private val testUserEmail = "user@email.test"
    private val testUserId = UserId("test-user-id")

    private var savedStateHandle: SavedStateHandle = mockk {
        every { this@mockk.get<String>(Arg.UserId) } returns testUserId.id
    }

    private var startRecovery: StartRecovery = mockk {
        coJustRun { this@mockk.invoke(testUserId) }
    }

    private var observeUser: ObserveUser = mockk {
        coEvery { this@mockk.invoke(any()) } returns flowOf(mockk {
            every { userId } returns testUserId
            every { email } returns testUserEmail
        })
    }

    private var observabilityManager: ObservabilityManager = mockk(relaxed = true)

    private lateinit var viewModel: PasswordResetDialogViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = PasswordResetDialogViewModel(
            savedStateHandle = savedStateHandle,
            observeUser = observeUser,
            startRecovery = startRecovery,
            observabilityManager = observabilityManager
        )
    }

    @Test
    fun stateIsLoading() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            // THEN
            assertIs<PasswordResetDialogViewModel.State.Loading>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stateIsReady() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            // THEN
            assertIs<PasswordResetDialogViewModel.State.Loading>(awaitItem())
            assertIs<PasswordResetDialogViewModel.State.Loading>(awaitItem())
            assertIs<PasswordResetDialogViewModel.State.Ready>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun stateIsResetRequested() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            assertIs<PasswordResetDialogViewModel.State.Loading>(awaitItem())
            assertIs<PasswordResetDialogViewModel.State.Loading>(awaitItem())
            assertIs<PasswordResetDialogViewModel.State.Ready>(awaitItem())

            // WHEN
            viewModel.perform(PasswordResetDialogViewModel.Action.RequestReset)

            // THEN
            assertIs<PasswordResetDialogViewModel.State.Loading>(awaitItem())
            assertIs<PasswordResetDialogViewModel.State.ResetRequested>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
