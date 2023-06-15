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

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveAccountRecoveryState
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class AccountRecoveryViewModelTest : ArchTest by ArchTest(),
    CoroutinesTest by CoroutinesTest() {

    private val observeAccountRecoveryState = mockk<ObserveAccountRecoveryState>(relaxed = true)
    private val cancelRecovery = mockk<CancelRecovery>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>()

    private val testUserId = UserId("test-user-id")

    private lateinit var viewModel: AccountRecoveryViewModel

    @Before
    fun beforeEveryTest() {
        every { keyStoreCrypto.encrypt(any<String>()) } answers { firstArg() }
        coEvery {
            observeAccountRecoveryState.invoke(
                testUserId,
                true
            )
        } returns flowOf(AccountRecoveryState.GracePeriod)
        every { keyStoreCrypto.encrypt(any<String>()) } answers { firstArg() }
        coEvery { cancelRecovery.invoke(any(), testUserId) } returns true
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )

    }

    @Test
    fun `initial state is opened state grace period started`() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state cancellation`() = coroutinesTest {
        // GIVEN
        coEvery { observeAccountRecoveryState.invoke(testUserId, true) } returns flowOf(
            AccountRecoveryState.Cancelled
        )
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened.CancellationHappened>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state recovery ended`() = coroutinesTest {
        // GIVEN
        coEvery { observeAccountRecoveryState.invoke(testUserId, true) } returns
                flowOf(AccountRecoveryState.Expired)
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened.RecoveryEnded>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state reset password`() = coroutinesTest {
        // GIVEN
        coEvery {
            observeAccountRecoveryState.invoke(
                testUserId,
                true
            )
        } returns flowOf(AccountRecoveryState.ResetPassword)
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state none`() = coroutinesTest {
        // GIVEN
        coEvery { observeAccountRecoveryState.invoke(testUserId, true) } returns flowOf(
            AccountRecoveryState.None
        )
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Closed>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state no primary user`() = coroutinesTest {
        // GIVEN
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // THEN
            delay(500)
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start recovery cancellation success`() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            every { observeAccountRecoveryState.invoke(testUserId, true) } returns flowOf(
                AccountRecoveryState.None
            )
            // WHEN
            viewModel.setUser(testUserId)
            viewModel.startAccountRecoveryCancel("password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Closed>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start recovery cancellation failure`() = coroutinesTest {
        // GIVEN
        coEvery { cancelRecovery.invoke(any(), testUserId) } throws ApiException(
            ApiResult.Error.Http(
                500, "Server error", ApiResult.Error.ProtonData(
                    code = 1000, error = "Cancellation error"
                )
            )
        )
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            viewModel.startAccountRecoveryCancel("password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            val event = awaitItem()
            assertTrue(event is AccountRecoveryViewModel.State.Error)
            assertNotNull(event.throwable)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start recovery cancellation invalid password`() = coroutinesTest {
        // GIVEN
        coEvery { cancelRecovery.invoke("invalid-password", testUserId) } throws ApiException(
            ApiResult.Error.Http(
                422, "Unprocessable Content", ApiResult.Error.ProtonData(
                    code = ResponseCodes.PASSWORD_WRONG,
                    error = "Incorrect login credentials. Please try again"
                )
            )
        )
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            viewModel.startAccountRecoveryCancel("invalid-password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            val event = awaitItem()
            assertTrue(event is AccountRecoveryViewModel.State.Error)
            assertNotNull(event.throwable)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `account recovery state throws error`() = coroutinesTest {
        // GIVEN
        every { observeAccountRecoveryState.invoke(testUserId, true) } throws ApiException(
            ApiResult.Error.Http(
                500, "Server error", ApiResult.Error.ProtonData(
                    code = 1000, error = "Cancellation error"
                )
            )
        )
        viewModel = AccountRecoveryViewModel(
            observeAccountRecoveryState,
            cancelRecovery,
            keyStoreCrypto
        )
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Error>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `user acknowledged success`() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            // WHEN
            viewModel.setUser(testUserId)
            viewModel.userAcknowledged()

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Closed>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start recovery cancellation no user success`() = coroutinesTest {
        // GIVEN
        viewModel.state.test {
            every { observeAccountRecoveryState.invoke(testUserId, true) } returns flowOf(
                AccountRecoveryState.None
            )
            // WHEN
            viewModel.startAccountRecoveryCancel("password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
