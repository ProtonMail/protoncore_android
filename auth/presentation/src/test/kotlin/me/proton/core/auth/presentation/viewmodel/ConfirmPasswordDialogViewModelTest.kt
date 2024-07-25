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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.feature.IsFido2Enabled
import me.proton.core.auth.domain.usecase.GetAuthInfoSrp
import me.proton.core.auth.domain.usecase.scopes.ObtainLockedScope
import me.proton.core.auth.domain.usecase.scopes.ObtainPasswordScope
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.network.domain.session.SessionId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.flowTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfirmPasswordDialogViewModelTest :
    ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private val accountManager = mockk<AccountManager>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val obtainAuthInfo = mockk<GetAuthInfoSrp>(relaxed = true)
    private val obtainLockedScope = mockk<ObtainLockedScope>(relaxed = true)
    private val obtainPasswordScope = mockk<ObtainPasswordScope>(relaxed = true)
    private val missingScopeListener = mockk<MissingScopeListener>(relaxed = true)

    @MockK
    private lateinit var isFido2Enabled: IsFido2Enabled

    @MockK(relaxed = true)
    private lateinit var observabilityManager: ObservabilityManager
    // endregion

    // region test data
    private val testUserIdString = "test-user-id"
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testPasswordEncrypted = "test-password-encrypted"
    private val test2FACode = SecondFactorProof.SecondFactorCode("test-2fa")
    private val testUserId = UserId(testUserIdString)
    private val testSessionId = SessionId("test-sessionId")

    private val testAccount = Account(
        userId = testUserId,
        username = testUsername,
        email = "test-email",
        state = AccountState.Ready,
        sessionState = SessionState.Authenticated,
        sessionId = testSessionId,
        details = AccountDetails(
            account = null,
            session = null
        )
    )

    private val testAuthInfo = AuthInfo.Srp(
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
        MockKAnnotations.init(this)
        mockkStatic("me.proton.core.accountmanager.domain.AccountManagerExtensionsKt")
        every { keyStoreCrypto.encrypt(testPassword) } returns testPasswordEncrypted
        every { keyStoreCrypto.decrypt(testPasswordEncrypted) } returns testPassword
        coEvery { accountManager.getAccount(testUserId) } returns flowOf(testAccount)
        coEvery { obtainAuthInfo.invoke(testSessionId, testUsername) } returns testAuthInfo
        viewModel = ConfirmPasswordDialogViewModel(
            accountManager,
            keyStoreCrypto,
            obtainAuthInfo,
            isFido2Enabled,
            obtainLockedScope,
            obtainPasswordScope,
            missingScopeListener,
            observabilityManager
        )
    }

    @After
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.accountmanager.domain.AccountManagerExtensionsKt")
    }

    @Test
    fun `unlock scope success is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainLockedScope.invoke(
                testUserId,
                testSessionId,
                testUsername,
                testPasswordEncrypted
            )
        } returns true
        flowTest(viewModel.state) {
            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.Success>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.unlock(testUserId, Scope.LOCKED, testPassword, null)
    }

    @Test
    fun `unlock scope failure is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainLockedScope.invoke(
                testUserId,
                testSessionId,
                testUsername,
                testPasswordEncrypted
            )
        } throws ApiException(
            ApiResult.Error.Http(
                400,
                "Bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_ALLOWED, "Invalid input")
            )
        )

        flowTest(viewModel.state) {
            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            val nextItem = awaitItem()
            assertTrue(nextItem is ConfirmPasswordDialogViewModel.State.Error.General)
            assertEquals("Invalid input", nextItem.error.getUserMessage(mockk()))
            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.unlock(testUserId, Scope.LOCKED, testPassword, null)
    }

    @Test
    fun `password scope no 2FA success is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testSessionId,
                testUsername,
                testPasswordEncrypted,
                null
            )
        } returns true
        flowTest(viewModel.state) {
            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.Success>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.unlock(testUserId, Scope.PASSWORD, testPassword, null)
    }

    @Test
    fun `password scope no 2FA failure is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testSessionId,
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
        flowTest(viewModel.state) {
            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            val nextItem = awaitItem()
            assertTrue(nextItem is ConfirmPasswordDialogViewModel.State.Error.General)
            assertEquals("Invalid input", nextItem.error.getUserMessage(mockk()))

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.unlock(testUserId, Scope.PASSWORD, testPassword, null)
    }

    @Test
    fun `password scope 2FA success is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testSessionId,
                testUsername,
                testPasswordEncrypted,
                test2FACode
            )
        } returns true
        flowTest(viewModel.state) {
            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.Success>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.unlock(testUserId, Scope.PASSWORD, testPassword, test2FACode)
    }

    @Test
    fun `password scope 2FA failure is handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery {
            obtainPasswordScope.invoke(
                testUserId,
                testSessionId,
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
        flowTest(viewModel.state) {
            // THEN
            assertIs<ConfirmPasswordDialogViewModel.State.Idle>(awaitItem())
            assertIs<ConfirmPasswordDialogViewModel.State.ProcessingObtainScope>(awaitItem())
            val nextItem = awaitItem()
            assertTrue(nextItem is ConfirmPasswordDialogViewModel.State.Error.General)
            assertEquals("Invalid input", nextItem.error.getUserMessage(mockk()))

            cancelAndIgnoreRemainingEvents()
        }

        // WHEN
        viewModel.unlock(testUserId, Scope.PASSWORD, testPassword, test2FACode)
    }
}
