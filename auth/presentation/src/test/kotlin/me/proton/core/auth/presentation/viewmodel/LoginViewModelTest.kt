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

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.crypto.SrpProofProvider
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.entity.Addresses
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.GetUser
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
import me.proton.core.network.domain.session.Session
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
@ExperimentalCoroutinesApi
class LoginViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val srpProofProvider = mockk<SrpProofProvider>(relaxed = true)
    private val accountHandler = mockk<AccountWorkflowHandler>(relaxed = true)
    private val useCasePerformLogin = mockk<PerformLogin>()
    private val useCaseUserSetup = mockk<PerformUserSetup>()
    private val useCaseGetUser = mockk<GetUser>()
    private val useCaseUpdateUsernameOnly = mockk<UpdateUsernameOnlyAccount>(relaxed = true)
    private val userMock = mockk<User>()
    // endregion

    // region test data
    private val testUserName = "test-username"
    private val testPassword = "test-password"
    private val testSessionId = "test-session-id"
    // endregion

    private lateinit var viewModel: LoginViewModel

    @Before
    fun beforeEveryTest() {
        every { useCaseGetUser.invoke(any()) } returns flowOf(
            GetUser.State.Processing,
            GetUser.State.Success(userMock)
        )
        every { userMock.keys } returns emptyList()
        every { userMock.addresses } returns Addresses(emptyList())

        viewModel = LoginViewModel(accountHandler, useCasePerformLogin, useCaseUserSetup, useCaseGetUser, useCaseUpdateUsernameOnly)
    }

    @Test
    fun `login 2FA 1Pass UsernameOnly flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Username
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns true
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.Login)
        assertEquals(sessionInfo, successState.sessionInfo)
        assertEquals(testUserName, successState.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.NotReady, accountArgument.captured.state)
        assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)

        verify(exactly = 0) { useCaseGetUser.invoke(any()) }
    }

    @Test
    fun `login 2FA 1Pass Internal flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns true
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.Login)
        assertEquals(sessionInfo, successState.sessionInfo)
        assertEquals(testUserName, successState.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.NotReady, accountArgument.captured.state)
        assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)

        verify(exactly = 0) { useCaseGetUser.invoke(any()) }
    }

    @Test
    fun `login 2FA 2Pass Username-only flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Username
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns true
        every { sessionInfo.isSecondFactorNeeded } returns true
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.Login)
        assertEquals(sessionInfo, successState.sessionInfo)
        assertEquals(testUserName, successState.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.TwoPassModeNeeded, accountArgument.captured.state)
        assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)

        verify(exactly = 0) { useCaseGetUser.invoke(any()) }
    }

    @Test
    fun `login 2FA 2Pass Internal account flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns true
        every { sessionInfo.isSecondFactorNeeded } returns true
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.Login)
        assertEquals(sessionInfo, successState.sessionInfo)
        assertEquals(testUserName, successState.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.TwoPassModeNeeded, accountArgument.captured.state)
        assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)

        verify(exactly = 0) { useCaseGetUser.invoke(any()) }
    }

    @Test
    fun `login no-2FA 1Pass Username-only account flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Username
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        every { userMock.copy(passphrase = any()) } returns mockk()

        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(userMock)
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.UserSetup)
        assertEquals(sessionInfo.sessionId, successState.sessionInfo.sessionId)
        assertEquals(testUserName, successState.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.NotReady, accountArgument.captured.state)
        assertEquals(SessionState.Authenticated, accountArgument.captured.sessionState)

        verify(exactly = 1) { useCaseGetUser.invoke(any()) }
    }

    @Test
    fun `login no-2FA 1Pass Internal account flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        every { userMock.copy(passphrase = any()) } returns mockk()
        every { userMock.name } returns testUserName
        every { userMock.role } returns 1
        every { userMock.private } returns false

        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(userMock)
        )
        coEvery {
            useCaseUpdateUsernameOnly.invoke(
                any(),
                any(),
                testUserName,
                testPassword.toByteArray()
            )
        } returns
            flowOf(
                UpdateUsernameOnlyAccount.State.Processing,
                UpdateUsernameOnlyAccount.State.Success(mockk())
            )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.UserSetup)
        assertEquals(sessionInfo.sessionId, successState.sessionInfo.sessionId)
        assertEquals(testUserName, successState.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.NotReady, accountArgument.captured.state)
        assertEquals(SessionState.Authenticated, accountArgument.captured.sessionState)

        verify(exactly = 1) { useCaseGetUser.invoke(any()) }
    }

    @Test
    fun `login get user error flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isSecondFactorNeeded } returns false
        every { sessionInfo.isTwoPassModeNeeded } returns false
        coEvery { useCaseGetUser.invoke(any()) } returns flowOf(
            GetUser.State.Processing,
            GetUser.State.Error.Message("test-error")
        )
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )

        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val state = arguments[1]
        assertTrue(state is PerformLogin.State.Error.FetchUser)
        assertEquals("test-error", (state.state as GetUser.State.Error.Message).message)
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.NotReady, accountArgument.captured.state)
        assertEquals(SessionState.Authenticated, accountArgument.captured.sessionState)
    }

    @Test
    fun `login account with 2FA 2Pass flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isSecondFactorNeeded } returns true
        every { sessionInfo.isTwoPassModeNeeded } returns true
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )

        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val state = arguments[1]
        assertTrue(state is PerformLogin.State.Success.Login)
        assertEquals(testUserName, state.sessionInfo.username)

        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertEquals(testUserName, accountArgument.captured.username)
        assertEquals(AccountState.TwoPassModeNeeded, accountArgument.captured.state)
        assertEquals(SessionState.SecondFactorNeeded, accountArgument.captured.sessionState)
    }

    @Test
    fun `login error path flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>()
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.username } returns testUserName
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Error.Message(message = "test error")
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val errorState = arguments[1]
        assertTrue(errorState is PerformLogin.State.Error.Message)
        assertEquals("test error", errorState.message)
    }

    @Test
    fun `login empty username returns correct state`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = AccountType.Internal
        viewModel = LoginViewModel(
            accountHandler,
            PerformLogin(authRepository, srpProofProvider, "test-client-secret"),
            useCaseUserSetup,
            useCaseGetUser,
            useCaseUpdateUsernameOnly
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow("", testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify {
            observer(capture(arguments))
        }
        val errorState = arguments[0]
        assertTrue(errorState is PerformLogin.State.Error.EmptyCredentials)
    }

    @Test
    fun `login empty password returns correct state`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = AccountType.Internal
        viewModel = LoginViewModel(
            accountHandler,
            PerformLogin(authRepository, srpProofProvider, "test-client-secret"),
            useCaseUserSetup,
            useCaseGetUser,
            useCaseUpdateUsernameOnly
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, "".toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify {
            observer(capture(arguments))
        }
        val errorState = arguments[0]
        assertTrue(errorState is PerformLogin.State.Error.EmptyCredentials)
    }

    @Test
    fun `login happy path dispatch account called`() = coroutinesTest {
        // GIVEN
        val testAccessToken = "test-access-token"
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { userMock.keys } returns listOf(mockk())
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.accessToken } returns testAccessToken
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(mockk())
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        verify(exactly = 2) { observer(capture(arguments)) }
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertIs<PerformLogin.State.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.State.Success.UserSetup)
        assertEquals(sessionInfo, successState.sessionInfo)
        assertEquals(testUserName, successState.sessionInfo.username)
        val account = accountArgument.captured
        val session = sessionArgument.captured
        assertNotNull(account)
        assertEquals(testUserName, account.username)
        assertEquals(testAccessToken, session.accessToken)
    }

    @Test
    fun `login happy path second factor needed`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.isSecondFactorNeeded } returns true
        every { sessionInfo.sessionId } returns testSessionId
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(mockk())
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        val account = accountArgument.captured
        val session = sessionArgument.captured
        assertNotNull(account)
        assertNotNull(session)
        assertEquals(testSessionId, session.sessionId.id)
    }

    @Test
    fun `login happy path mailbox login needed`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        val requiredAccountType = AccountType.Internal
        every { sessionInfo.isTwoPassModeNeeded } returns true
        every { sessionInfo.sessionId } returns testSessionId
        every { userMock.copy(passphrase = any()) } returns mockk()
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(userMock)
        )
        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountHandler.handleSession(capture(accountArgument), capture(sessionArgument)) }
        val account = accountArgument.captured
        val session = sessionArgument.captured
        assertNotNull(account)
        assertEquals(AccountState.TwoPassModeNeeded, account.state)
        assertEquals(testSessionId, session.sessionId.id)
    }

    @Test
    fun `login private user of organization returns correct state`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = AccountType.Internal
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        every { userMock.private } returns true
        every { userMock.role } returns 1

        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify {
            observer(capture(arguments))
        }
        assertEquals(2, arguments.size)
        val errorState = arguments[1]
        assertTrue(errorState is PerformLogin.State.Error.PasswordChange)
    }

    @Test
    fun `login private user of organization incorrect role not triggered`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = AccountType.Internal
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.isTwoPassModeNeeded } returns false
        every { sessionInfo.isSecondFactorNeeded } returns false
        coEvery { useCasePerformLogin.invoke(any(), any()) } returns flowOf(
            PerformLogin.State.Processing,
            PerformLogin.State.Success.Login(sessionInfo)
        )
        every { userMock.name } returns testUserName
        every { userMock.private } returns true
        every { userMock.role } returns 0

        coEvery {
            useCaseUpdateUsernameOnly.invoke(
                any(),
                any(),
                testUserName,
                testPassword.toByteArray()
            )
        } returns
            flowOf(
                UpdateUsernameOnlyAccount.State.Processing,
                UpdateUsernameOnlyAccount.State.Success(mockk())
            )

        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(userMock)
        )

        val observer = mockk<(PerformLogin.State) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray(), requiredAccountType)
        // THEN
        val arguments = mutableListOf<PerformLogin.State>()
        verify {
            observer(capture(arguments))
        }
        assertEquals(2, arguments.size)
        val firstState = arguments[0]
        val secondState = arguments[1]
        assertTrue(firstState is PerformLogin.State.Processing)
        assertTrue(secondState is PerformLogin.State.Success.UserSetup)
    }
}
