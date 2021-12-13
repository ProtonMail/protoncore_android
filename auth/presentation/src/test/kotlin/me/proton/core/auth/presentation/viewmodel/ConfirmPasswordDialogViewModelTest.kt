/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.scopes.ObtainAuthInfo
import me.proton.core.auth.domain.usecase.scopes.ObtainLockedScope
import me.proton.core.auth.domain.usecase.scopes.ObtainPasswordScope
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfirmPasswordDialogViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountManager = mockk<AccountManager>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val obtainAuthInfo = mockk<ObtainAuthInfo>(relaxed = true)
    private val obtainLockedScope = mockk<ObtainLockedScope>(relaxed = true)
    private val obtainPasswordScope = mockk<ObtainPasswordScope>(relaxed = true)
    // endregion

    // region test data
    private val testUserIdString = "test-user-id"
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testPasswordEncrypted = "test-password-encrypted"
    private val test2FACode = "test-2fa"
    private val testUserId = UserId(testUserIdString)

    private val testAccount = Account(
        userId = testUserId,
        username = testUsername,
        email = "test-email",
        state = AccountState.Ready,
        sessionState = SessionState.Authenticated,
        sessionId = SessionId("test-session-id"),
        details = AccountDetails(
            account = null,
            session = null
        )
    )

    private val testAuthInfo = AuthInfo(
        username = testUsername,
        modulus = "test-modulus",
        serverEphemeral = "test-server-ephemeral",
        version = 1,
        salt = "test-salt",
        srpSession = "test-srp-session",
        secondFactor = null
    )

    // endregion

    private lateinit var viewModel: ConfirmPasswordDialogViewModel

    @Before
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.accountmanager.domain.AccountManagerExtensionsKt")
        every { keyStoreCrypto.encrypt(testPassword) } returns testPasswordEncrypted
        every { keyStoreCrypto.decrypt(testPasswordEncrypted) } returns testPassword
        coEvery { accountManager.getPrimaryAccount() } returns flowOf(testAccount)
        coEvery { obtainAuthInfo.invoke(testUserId, testUsername) } returns testAuthInfo
        viewModel = ConfirmPasswordDialogViewModel(
            accountManager,
            keyStoreCrypto,
            obtainAuthInfo,
            obtainLockedScope,
            obtainPasswordScope
        )
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.accountmanager.domain.AccountManagerExtensionsKt")
    }

    @Test
    fun `unlock scope success is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { obtainLockedScope.invoke(testUserId, testUsername, testPasswordEncrypted) } returns true
        viewModel.state.test {
            // WHEN
            viewModel.unlock(testPassword)

            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.Success>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `unlock scope failure is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { obtainLockedScope.invoke(testUserId, testUsername, testPasswordEncrypted) } throws ApiException(
            ApiResult.Error.Http(
                400,
                "Bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_ALLOWED, "Invalid input")
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.unlock(testPassword)

            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            val nextItem = awaitItem()
            assertTrue(nextItem is ConfirmPasswordDialogViewModel.State.Error.Message)
            assertEquals("Invalid input", nextItem.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `password scope no 2FA success is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { obtainPasswordScope.invoke(testUserId, testUsername, testPasswordEncrypted, null) } returns true
        viewModel.state.test {
            // WHEN
            viewModel.unlockPassword(testPassword, null)

            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.Success>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `password scope no 2FA failure is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testUsername,
                testPasswordEncrypted,
                null
            )
        } throws ApiException(
            ApiResult.Error.Http(
                400,
                "Bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_ALLOWED, "Invalid input")
            )
        )
        viewModel.state.test {
            // WHEN
            viewModel.unlockPassword(testPassword, null)

            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            val nextItem = awaitItem()
            assertTrue(nextItem is ConfirmPasswordDialogViewModel.State.Error.Message)
            assertEquals("Invalid input", nextItem.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `password scope 2FA success is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testUsername,
                testPasswordEncrypted,
                test2FACode
            )
        } returns true
        viewModel.state.test {
            // WHEN
            viewModel.unlockPassword(testPassword, test2FACode)

            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.Success>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `password scope 2FA failure is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testUsername,
                testPasswordEncrypted,
                test2FACode
            )
        } throws ApiException(
            ApiResult.Error.Http(
                400,
                "Bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_ALLOWED, "Invalid input")
            )
        )
        viewModel.state.test {
            // WHEN
            viewModel.unlockPassword(testPassword, test2FACode)

            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            val nextItem = awaitItem()
            assertTrue(nextItem is ConfirmPasswordDialogViewModel.State.Error.Message)
            assertEquals("Invalid input", nextItem.message)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
