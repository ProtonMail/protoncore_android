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
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.crypto.CryptoProvider
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.PerformMailboxLogin
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Dino Kadrikj.
 */
class MailboxLoginViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val cryptoProvider = mockk<CryptoProvider>(relaxed = true)
    private val accountManager = mockk<AccountWorkflowHandler>(relaxed = true)
    private val useCase = mockk<PerformMailboxLogin>()
    private val testUser = mockk<User>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testPassword = "test-password"
    private val testName = "test-name"
    private val testEmail = "test-email"

    // endregion
    private lateinit var viewModel: MailboxLoginViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = MailboxLoginViewModel(accountManager, useCase)
    }

    @Test
    fun `mailbox login happy flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        every { testUser.name } returns testName
        every { testUser.email } returns testEmail
        coEvery { useCase.invoke(SessionId(testSessionId), testPassword.toByteArray()) } returns flowOf(
            PerformMailboxLogin.MailboxLoginState.Processing,
            PerformMailboxLogin.MailboxLoginState.Success(testUser)
        )
        val observer = mockk<(PerformMailboxLogin.MailboxLoginState) -> Unit>(relaxed = true)
        viewModel.mailboxLoginState.observeDataForever(observer)
        // WHEN
        viewModel.startMailboxLoginFlow(SessionId(testSessionId), testPassword.toByteArray())
        // THEN
        val arguments = mutableListOf<PerformMailboxLogin.MailboxLoginState>()
        verify(exactly = 2) { observer(capture(arguments)) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is PerformMailboxLogin.MailboxLoginState.Processing)
        assertTrue(successState is PerformMailboxLogin.MailboxLoginState.Success)
        assertEquals(testUser, successState.user)
        assertEquals(testName, successState.user.name)
        assertEquals(testEmail, successState.user.email)
    }

    @Test
    fun `mailbox login empty password returns correct state of events`() = coroutinesTest {
        // GIVEN
        viewModel =
            MailboxLoginViewModel(accountManager, PerformMailboxLogin(authRepository, cryptoProvider))
        val observer = mockk<(PerformMailboxLogin.MailboxLoginState) -> Unit>(relaxed = true)
        viewModel.mailboxLoginState.observeDataForever(observer)
        // WHEN
        viewModel.startMailboxLoginFlow(SessionId(testSessionId), "".toByteArray())
        // THEN
        val arguments = slot<PerformMailboxLogin.MailboxLoginState>()
        verify { observer(capture(arguments)) }
        val argument = arguments.captured
        assertTrue(argument is PerformMailboxLogin.MailboxLoginState.Error.EmptyCredentials)
    }

    @Test
    fun `success mailbox login invokes success on account manager`() = coroutinesTest {
        // GIVEN
        coEvery { useCase.invoke(SessionId(testSessionId), testPassword.toByteArray()) } returns flowOf(
            PerformMailboxLogin.MailboxLoginState.Processing,
            PerformMailboxLogin.MailboxLoginState.Success(testUser)
        )
        // WHEN
        viewModel.startMailboxLoginFlow(SessionId(testSessionId), testPassword.toByteArray())
        // THEN
        val arguments = slot<SessionId>()
        coVerify(exactly = 1) { accountManager.handleTwoPassModeSuccess(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleTwoPassModeFailed(any()) }
        assertEquals(testSessionId, arguments.captured.id)
    }

    @Test
    fun `failed mailbox login invokes failed on account manager`() = coroutinesTest {
        // GIVEN
        coEvery { useCase.invoke(SessionId(testSessionId), testPassword.toByteArray()) } returns flowOf(
            PerformMailboxLogin.MailboxLoginState.Processing,
            PerformMailboxLogin.MailboxLoginState.Error.Message("test error")
        )
        // WHEN
        viewModel.startMailboxLoginFlow(SessionId(testSessionId), testPassword.toByteArray())
        // THEN
        val arguments = slot<SessionId>()
        coVerify(exactly = 1) { accountManager.handleTwoPassModeFailed(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleTwoPassModeSuccess(any()) }
        assertEquals(testSessionId, arguments.captured.id)
    }
}
