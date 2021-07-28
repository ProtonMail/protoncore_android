/*
 * Copyright (c) 2020 Proton Technologies AG
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

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.domain.usecase.SetupInternalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.usecase.PerformSubscribe
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.UserManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class LoginViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountHandler = mockk<AccountWorkflowHandler>(relaxed = true)
    private val performLogin = mockk<PerformLogin>()
    private val unlockUserPrimaryKey = mockk<UnlockUserPrimaryKey>()
    private val setupAccountCheck = mockk<SetupAccountCheck>()
    private val setupPrimaryKeys = mockk<SetupPrimaryKeys>(relaxed = true)
    private val setupInternalAddress = mockk<SetupInternalAddress>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
    private val humanVerificationManager = mockk<HumanVerificationManager>(relaxed = true)
    private val humanVerificationOrchestrator = mockk<HumanVerificationOrchestrator>(relaxed = true)
    private val performSubscribe = mockk<PerformSubscribe>(relaxed = true)
    // endregion

    // region test data
    private val testUserName = "test-username"
    private val testPassword = "test-password"
    private val testUserId = UserId("test-user-id")
    private val testSessionId = SessionId("test-session-id")
    // endregion

    private lateinit var viewModel: LoginViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = LoginViewModel(
            savedStateHandle,
            accountHandler,
            performLogin,
            unlockUserPrimaryKey,
            setupAccountCheck,
            setupPrimaryKeys,
            setupInternalAddress,
            keyStoreCrypto,
            performSubscribe,
            humanVerificationManager,
            humanVerificationOrchestrator
        )
        every { keyStoreCrypto.decrypt(any<String>()) } returns testPassword
        every { keyStoreCrypto.encrypt(any<String>()) } returns testPassword
    }

    @Test
    fun `login 2FA 1Pass flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Username
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns true
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.TwoPassNeeded
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            val successState = expectItem()
            assertTrue(successState is LoginViewModel.State.Need.SecondFactor)
            assertEquals(sessionInfo.userId, successState.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            assertEquals(testUserName, accountArgument.captured.username)
            assertEquals(AccountState.NotReady, accountArgument.captured.state)
            assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login 2FA 2Pass flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Username
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns true
        every { sessionInfo.isSecondFactorNeeded } returns true
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            val successState = expectItem()
            assertTrue(successState is LoginViewModel.State.Need.SecondFactor)
            assertEquals(sessionInfo.userId, successState.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            assertEquals(testUserName, accountArgument.captured.username)
            assertEquals(AccountState.NotReady, accountArgument.captured.state)
            assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)
            coVerify(exactly = 0) { setupAccountCheck.invoke(any(), any(), any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login no-2FA 1Pass Username account flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Username
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            val successState = expectItem()
            assertTrue(successState is LoginViewModel.State.Success.UserUnLocked)
            assertEquals(sessionInfo.userId, successState.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            assertEquals(testUserName, accountArgument.captured.username)
            assertEquals(AccountState.NotReady, accountArgument.captured.state)
            assertEquals(SessionState.Authenticated, accountArgument.captured.sessionState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login no-2FA 1Pass Internal account flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery {
            setupAccountCheck.invoke(
                any(),
                any(),
                any()
            )
        } returns SetupAccountCheck.Result.SetupPrimaryKeysNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        coEvery { setupPrimaryKeys.invoke(testUserId, testPassword, any()) } returns Unit
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            val successState = expectItem()
            assertTrue(successState is LoginViewModel.State.Success.UserUnLocked)
            assertEquals(sessionInfo.userId, successState.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            assertEquals(testUserName, accountArgument.captured.username)
            assertEquals(AccountState.NotReady, accountArgument.captured.state)
            assertEquals(SessionState.Authenticated, accountArgument.captured.sessionState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login get user error flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isSecondFactorNeeded } returns false
        every { sessionInfo.isTwoPassModeNeeded } returns false
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } throws ApiException(
            ApiResult.Error.NoInternet()
        )
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            assertTrue(expectItem() is LoginViewModel.State.Error.Message)

            verify { savedStateHandle.set(any(), any<String>()) }

            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            assertEquals(testUserName, accountArgument.captured.username)
            assertEquals(AccountState.NotReady, accountArgument.captured.state)
            assertEquals(SessionState.Authenticated, accountArgument.captured.sessionState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login account with 2FA 2Pass flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isSecondFactorNeeded } returns true
        every { sessionInfo.isTwoPassModeNeeded } returns true
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            assertTrue(expectItem() is LoginViewModel.State.Need.SecondFactor)

            verify { savedStateHandle.set(any(), any<String>()) }

            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            assertEquals(testUserName, accountArgument.captured.username)
            assertEquals(AccountState.NotReady, accountArgument.captured.state)
            assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login error path flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>()
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        coEvery { performLogin.invoke(any(), any()) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())

            val errorState = expectItem()
            assertTrue(errorState is LoginViewModel.State.Error.Message)
            assertEquals("proton error", errorState.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login happy path dispatch account called`() = coroutinesTest {
        // GIVEN
        val testAccessToken = "test-access-token"
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.accessToken } returns testAccessToken
        every { sessionInfo.isSecondFactorNeeded } returns false
        every { sessionInfo.isTwoPassModeNeeded } returns false
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()

            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }

            assertIs<LoginViewModel.State.Processing>(expectItem())

            val successState = expectItem()
            assertTrue(successState is LoginViewModel.State.Success.UserUnLocked)
            assertEquals(sessionInfo.userId, successState.userId)
            val account = accountArgument.captured
            val session = sessionArgument.captured
            assertNotNull(account)
            assertEquals(testUserName, account.username)
            assertEquals(testAccessToken, session.accessToken)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login happy path second factor needed`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.isSecondFactorNeeded } returns true
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.sessionId } returns testSessionId
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()

            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }

            val account = accountArgument.captured
            val session = sessionArgument.captured
            assertNotNull(account)
            assertNotNull(session)
            assertEquals(testSessionId.id, session.sessionId.id)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login happy path mailbox login needed`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.isSecondFactorNeeded } returns false
        every { sessionInfo.isTwoPassModeNeeded } returns true
        every { sessionInfo.sessionId } returns testSessionId
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.TwoPassNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            val accountArgument = slot<Account>()
            val sessionArgument = slot<Session>()
            coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
            val account = accountArgument.captured
            assertNotNull(account)
            assertEquals(AccountState.NotReady, account.state)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login private user of organization returns correct state`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = AccountType.Internal
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        coEvery { performLogin.invoke(any(), any()) } returns sessionInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.ChangePasswordNeeded
        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(expectItem())
            assertIs<LoginViewModel.State.Need.ChangePassword>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
