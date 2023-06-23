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
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountrecovery.domain.usecase.CancelRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecoveryState
import me.proton.core.accountrecovery.presentation.compose.ui.Arg
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.AccountRecoveryCancellationTotal
import me.proton.core.observability.domain.metrics.AccountRecoveryScreenViewTotal
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.entity.UserRecovery
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AccountRecoveryViewModelTest : ArchTest by ArchTest(),
    CoroutinesTest by CoroutinesTest() {
    private val testUserId = UserId("test-user-id")

    private lateinit var savedStateHandle: SavedStateHandle

    @MockK(relaxed = true)
    private lateinit var cancelRecovery: CancelRecovery

    @MockK
    private lateinit var keyStoreCrypto: KeyStoreCrypto

    @MockK(relaxed = true)
    private lateinit var observeUserRecoveryState: ObserveUserRecoveryState

    @MockK
    private lateinit var observabilityManager: ObservabilityManager

    private lateinit var viewModel: AccountRecoveryViewModel

    @Before
    fun beforeEveryTest() {
        savedStateHandle = mockk {
            every { this@mockk.get<String>(Arg.UserId) } returns testUserId.id
        }
        MockKAnnotations.init(this)

        every { keyStoreCrypto.encrypt(any<String>()) } answers { firstArg() }
        every { keyStoreCrypto.encrypt(any<String>()) } answers { firstArg() }
        coJustRun { cancelRecovery.invoke(any(), testUserId) }

        viewModel = AccountRecoveryViewModel(
            savedStateHandle,
            observeUserRecoveryState,
            cancelRecovery,
            keyStoreCrypto,
            observabilityManager
        )
    }

    @Test
    fun `initial state is opened state grace period started`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Grace
        )
        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state cancellation`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Cancelled
        )
        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened.CancellationHappened>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state recovery ended`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Expired
        )

        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened.RecoveryEnded>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state reset password`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Insecure
        )

        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state is opened state none`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.None
        )
        viewModel.state.test {
            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Closed>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state no primary user`() = coroutinesTest {
        // GIVEN
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
            every { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
                UserRecovery.State.Grace
            )

            // WHEN
            viewModel.startAccountRecoveryCancel("password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            assertIs<AccountRecoveryViewModel.State.Closed>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancellation metric is enqueued`() = coroutinesTest {
        // GIVEN
        every { observeUserRecoveryState.invoke(testUserId, true) } returns
                flowOf(UserRecovery.State.None)
        coEvery { cancelRecovery(any(), any()) } coAnswers {
            result("account_recovery.cancellation") { /* emulate success call result */ }
        }

        // WHEN
        viewModel.startAccountRecoveryCancel("password").join()

        // THEN
        verify {
            observabilityManager.enqueue(
                AccountRecoveryCancellationTotal(
                    AccountRecoveryCancellationTotal.ApiStatus.http2xx
                ), any()
            )
        }
    }

    @Test
    fun `start recovery cancellation failure`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Grace
        )
        coEvery { cancelRecovery.invoke(any(), testUserId) } throws ApiException(
            ApiResult.Error.Http(
                500, "Server error", ApiResult.Error.ProtonData(
                    code = 1000, error = "Cancellation error"
                )
            )
        )
        viewModel.state.test {
            // WHEN
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
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Grace
        )
        coEvery { cancelRecovery.invoke("invalid-password", testUserId) } throws ApiException(
            ApiResult.Error.Http(
                422, "Unprocessable Content", ApiResult.Error.ProtonData(
                    code = ResponseCodes.PASSWORD_WRONG,
                    error = "Incorrect login credentials. Please try again"
                )
            )
        )
        viewModel.state.test {
            // WHEN
            viewModel.startAccountRecoveryCancel("invalid-password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            val event = awaitItem()
            assertTrue(event is AccountRecoveryViewModel.State.Opened.GracePeriodStarted)
            assertNotNull(event.passwordError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start recovery cancellation empty password`() = coroutinesTest {
        // GIVEN
        coEvery { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
            UserRecovery.State.Grace
        )

        viewModel.state.test {
            // WHEN
            viewModel.startAccountRecoveryCancel("")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())
            val event = awaitItem()
            assertTrue(event is AccountRecoveryViewModel.State.Opened.GracePeriodStarted)
            assertNotNull(event.passwordError)
            assertFalse(event.processing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `account recovery state throws error`() = coroutinesTest {
        // GIVEN
        every { observeUserRecoveryState.invoke(testUserId, true) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 500, message = "Server error", proton = ApiResult.Error.ProtonData(
                    code = 1000, error = "Cancellation error"
                )
            )
        )
        viewModel.state.test {
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
            every { observeUserRecoveryState.invoke(testUserId, true) } returns flowOf(
                UserRecovery.State.None
            )
            // WHEN
            viewModel.startAccountRecoveryCancel("password")

            // THEN
            assertIs<AccountRecoveryViewModel.State.Loading>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `screen view metric is recorded`() {
        // GIVEN
        justRun { observabilityManager.enqueue(any(), any()) }

        // WHEN
        viewModel.onScreenView(AccountRecoveryScreenViewTotal.ScreenId.gracePeriodInfo)

        // THEN
        verify {
            observabilityManager.enqueue(
                AccountRecoveryScreenViewTotal(AccountRecoveryScreenViewTotal.ScreenId.gracePeriodInfo),
                any()
            )
        }
    }

    @Test
    fun `converting state to screenId is correct`() {
        assertNull(AccountRecoveryViewModel.State.Closed.toScreenId())
        assertNull(AccountRecoveryViewModel.State.Error(null).toScreenId())
        assertNull(AccountRecoveryViewModel.State.Loading.toScreenId())
        assertEquals(
            AccountRecoveryScreenViewTotal.ScreenId.recoveryCancelledInfo,
            AccountRecoveryViewModel.State.Opened.CancellationHappened.toScreenId()
        )
        assertEquals(
            AccountRecoveryScreenViewTotal.ScreenId.gracePeriodInfo,
            AccountRecoveryViewModel.State.Opened.GracePeriodStarted().toScreenId()
        )
        assertEquals(
            AccountRecoveryScreenViewTotal.ScreenId.passwordChangeInfo,
            AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted.toScreenId()
        )
        assertEquals(
            AccountRecoveryScreenViewTotal.ScreenId.recoveryExpiredInfo,
            AccountRecoveryViewModel.State.Opened.RecoveryEnded.toScreenId()
        )
    }
}
