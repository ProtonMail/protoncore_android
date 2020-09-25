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
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.crypto.SrpProofProvider
import me.proton.core.auth.domain.entity.Account
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.PerformLogin
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
    private val accountManager = mockk<AccountWorkflowHandler>(relaxed = true)
    private val useCase = mockk<PerformLogin>()
    // endregion

    // region test data
    private val testUserName = "test-username"
    private val testPassword = "test-password"
    private val testSessionId = "test-session-id"
    // endregion

    private lateinit var viewModel: LoginViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = LoginViewModel(accountManager, useCase)
    }

    @Test
    fun `login happy path flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        every { sessionInfo.username } returns testUserName
        coEvery { useCase.invoke(any(), any()) } returns flowOf(
            PerformLogin.LoginState.Processing,
            PerformLogin.LoginState.Success(sessionInfo)
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray())
        // THEN
        val arguments = mutableListOf<PerformLogin.LoginState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.LoginState.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.LoginState.Success)
        assertEquals(sessionInfo, successState.sessionInfo)
        assertEquals(testUserName, successState.sessionInfo.username)
    }

    @Test
    fun `login error path flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockk<SessionInfo>()
        every { sessionInfo.username } returns testUserName
        coEvery { useCase.invoke(any(), any()) } returns flowOf(
            PerformLogin.LoginState.Processing,
            PerformLogin.LoginState.Error.Message(message = "test error")
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray())
        // THEN
        val arguments = mutableListOf<PerformLogin.LoginState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        assertIs<PerformLogin.LoginState.Processing>(arguments[0])
        val errorState = arguments[1]
        assertTrue(errorState is PerformLogin.LoginState.Error.Message)
        assertEquals("test error", errorState.message)
    }

    @Test
    fun `login empty username returns correct state`() = coroutinesTest {
        // GIVEN
        viewModel = LoginViewModel(
            accountManager,
            PerformLogin(authRepository, srpProofProvider, "test-client-secret")
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow("", testPassword.toByteArray())
        // THEN
        val arguments = mutableListOf<PerformLogin.LoginState>()
        verify {
            observer(capture(arguments))
        }
        val errorState = arguments[0]
        assertTrue(errorState is PerformLogin.LoginState.Error.EmptyCredentials)
    }

    @Test
    fun `login empty password returns correct state`() = coroutinesTest {
        // GIVEN
        viewModel = LoginViewModel(
            accountManager,
            PerformLogin(authRepository, srpProofProvider, "test-client-secret")
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, "".toByteArray())
        // THEN
        val arguments = mutableListOf<PerformLogin.LoginState>()
        verify {
            observer(capture(arguments))
        }
        val errorState = arguments[0]
        assertTrue(errorState is PerformLogin.LoginState.Error.EmptyCredentials)
    }

    @Test
    fun `login happy path dispatch account called`() = coroutinesTest {
        // GIVEN
        val testAccessToken = "test-access-token"
        val sessionInfo = mockk<SessionInfo>(relaxed = true)
        every { sessionInfo.username } returns testUserName
        every { sessionInfo.accessToken } returns testAccessToken
        coEvery { useCase.invoke(any(), any()) } returns flowOf(
            PerformLogin.LoginState.Processing,
            PerformLogin.LoginState.Success(sessionInfo)
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray())
        // THEN
        val arguments = mutableListOf<PerformLogin.LoginState>()
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        verify(exactly = 2) { observer(capture(arguments)) }
        coVerify(exactly = 1) { accountManager.handleSession(capture(accountArgument), capture(sessionArgument)) }
        assertIs<PerformLogin.LoginState.Processing>(arguments[0])
        val successState = arguments[1]
        assertTrue(successState is PerformLogin.LoginState.Success)
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
        every { sessionInfo.isSecondFactorNeeded } returns true
        every { sessionInfo.sessionId } returns testSessionId
        coEvery { useCase.invoke(any(), any()) } returns flowOf(
            PerformLogin.LoginState.Processing,
            PerformLogin.LoginState.Success(sessionInfo)
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray())
        // THEN
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountManager.handleSession(capture(accountArgument), capture(sessionArgument)) }
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
        every { sessionInfo.isTwoPassModeNeeded } returns true
        every { sessionInfo.sessionId } returns testSessionId
        coEvery { useCase.invoke(any(), any()) } returns flowOf(
            PerformLogin.LoginState.Processing,
            PerformLogin.LoginState.Success(sessionInfo)
        )
        val observer = mockk<(PerformLogin.LoginState) -> Unit>(relaxed = true)
        viewModel.loginState.observeDataForever(observer)
        // WHEN
        viewModel.startLoginWorkflow(testUserName, testPassword.toByteArray())
        // THEN
        val accountArgument = slot<Account>()
        val sessionArgument = slot<Session>()
        coVerify(exactly = 1) { accountManager.handleSession(capture(accountArgument), capture(sessionArgument)) }
        val account = accountArgument.captured
        val session = sessionArgument.captured
        assertNotNull(account)
        assertTrue(account.isTwoPassModeNeeded)
        assertEquals(testSessionId, session.sessionId.id)
    }
}
