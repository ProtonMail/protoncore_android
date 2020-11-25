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
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.entity.UserKey
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.GetUser
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.domain.usecase.UpdateUsernameOnlyAccount
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
class SecondFactorViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val authRepository = mockk<AuthRepository>(relaxed = true)
    private val accountManager = mockk<AccountWorkflowHandler>(relaxed = true)
    private val useCase = mockk<PerformSecondFactor>()
    private val useCaseUserSetup = mockk<PerformUserSetup>()
    private lateinit var viewModel: SecondFactorViewModel
    private val testScopeInfo = mockk<ScopeInfo>(relaxed = true)
    private val useCaseGetUser = mockk<GetUser>()
    private val useCaseUpdateUsernameOnly = mockk<UpdateUsernameOnlyAccount>(relaxed = true)
    private val userMock = mockk<User>()
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testSecondFactorCode = "123456"
    private val testLoginPassword = "123456".toByteArray()
    // endregion

    @Before
    fun beforeEveryTest() {
        coEvery { useCaseGetUser.invoke(any()) } returns flowOf(
            GetUser.State.Processing,
            GetUser.State.Success(userMock)
        )
        every { userMock.keys } returns listOf(
            UserKey(
                "test-id",
                1,
                "test-private-key",
                "test-fingerprint",
                null,
                1
            )
        )
        viewModel = SecondFactorViewModel(accountManager, useCase, useCaseUserSetup, useCaseUpdateUsernameOnly, useCaseGetUser)
    }

    @Test
    fun `submit 2fa happy flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val isMailboxLoginNeeded = false
        val requiredAccountType = AccountType.Internal
        coEvery { useCase.invoke(SessionId(testSessionId), testSecondFactorCode) } returns flowOf(
            PerformSecondFactor.State.Processing,
            PerformSecondFactor.State.Success.SecondFactor(SessionId(testSessionId), testScopeInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(mockk())
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(userMock)
        )
        val observer = mockk<(PerformSecondFactor.State) -> Unit>(relaxed = true)
        viewModel.secondFactorState.observeDataForever(observer)
        // WHEN
        viewModel.startSecondFactorFlow(
            SessionId(testSessionId),
            testLoginPassword,
            testSecondFactorCode,
            isMailboxLoginNeeded,
            requiredAccountType
        )
        // THEN
        val arguments = mutableListOf<PerformSecondFactor.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is PerformSecondFactor.State.Processing)
        assertTrue(successState is PerformSecondFactor.State.Success)
    }

    @Test
    fun `submit empty 2fa states flow are handled correctly`() = coroutinesTest {
        // GIVEN
        val isMailboxLoginNeeded = false
        val requiredAccountType = AccountType.Internal
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(mockk())
        )
        viewModel = SecondFactorViewModel(accountManager, PerformSecondFactor(authRepository), useCaseUserSetup, useCaseUpdateUsernameOnly, useCaseGetUser)
        val observer = mockk<(PerformSecondFactor.State) -> Unit>(relaxed = true)
        viewModel.secondFactorState.observeDataForever(observer)
        // WHEN
        viewModel.startSecondFactorFlow(SessionId(testSessionId), testLoginPassword, "", isMailboxLoginNeeded, requiredAccountType)
        // THEN
        val arguments = slot<PerformSecondFactor.State>()
        verify { observer(capture(arguments)) }
        val argument = arguments.captured
        assertTrue(argument is PerformSecondFactor.State.Error.EmptyCredentials)
    }

    @Test
    fun `submit 2fa single pass mode flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val isMailboxLoginNeeded = false
        val requiredAccountType = AccountType.Internal
        coEvery { useCase.invoke(SessionId(testSessionId), testSecondFactorCode) } returns flowOf(
            PerformSecondFactor.State.Processing,
            PerformSecondFactor.State.Success.SecondFactor(SessionId(testSessionId), testScopeInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(mockk())
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(userMock)
        )
        val observer = mockk<(PerformSecondFactor.State) -> Unit>(relaxed = true)
        viewModel.secondFactorState.observeDataForever(observer)
        // WHEN
        viewModel.startSecondFactorFlow(
            SessionId(testSessionId),
            testLoginPassword,
            testSecondFactorCode,
            isMailboxLoginNeeded,
            requiredAccountType
        )
        // THEN
        val arguments = mutableListOf<PerformSecondFactor.State>()
        val accountManagerArguments = slot<SessionId>()
        verify(exactly = 2) { observer(capture(arguments)) }
        coVerify(exactly = 1) { accountManager.handleSecondFactorSuccess(capture(accountManagerArguments), any()) }
        coVerify(exactly = 0) { accountManager.handleSecondFactorFailed(any()) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is PerformSecondFactor.State.Processing)
        assertTrue(successState is PerformSecondFactor.State.Success.UserSetup)
        assertEquals(SessionId(testSessionId), accountManagerArguments.captured)
    }

    @Test
    fun `submit 2fa two pass mode flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val isMailboxLoginNeeded = true
        val requiredAccountType = AccountType.Internal
        coEvery { useCase.invoke(SessionId(testSessionId), testSecondFactorCode) } returns flowOf(
            PerformSecondFactor.State.Processing,
            PerformSecondFactor.State.Success.SecondFactor(SessionId(testSessionId), testScopeInfo)
        )
        coEvery { useCaseUserSetup.invoke(any(), any()) } returns flowOf(
            PerformUserSetup.State.Processing,
            PerformUserSetup.State.Success(mockk())
        )
        val observer = mockk<(PerformSecondFactor.State) -> Unit>(relaxed = true)
        viewModel.secondFactorState.observeDataForever(observer)
        // WHEN
        viewModel.startSecondFactorFlow(
            SessionId(testSessionId),
            testLoginPassword,
            testSecondFactorCode,
            isMailboxLoginNeeded,
            requiredAccountType
        )
        // THEN
        val arguments = mutableListOf<PerformSecondFactor.State>()
        val accountManagerArguments = slot<SessionId>()
        verify(exactly = 2) { observer(capture(arguments)) }
        coVerify(exactly = 1) { accountManager.handleSecondFactorSuccess(capture(accountManagerArguments), any()) }
        coVerify(exactly = 0) { accountManager.handleSecondFactorFailed(any()) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is PerformSecondFactor.State.Processing)
        assertTrue(successState is PerformSecondFactor.State.Success.SecondFactor)
        assertEquals(SessionId(testSessionId), accountManagerArguments.captured)
    }

    @Test
    fun `stop 2fa invokes failed on account manager`() = coroutinesTest {
        // WHEN
        viewModel.stopSecondFactorFlow(SessionId(testSessionId))
        // THEN
        val arguments = slot<SessionId>()
        coVerify(exactly = 1) { accountManager.handleSecondFactorFailed(capture(arguments)) }
        coVerify(exactly = 0) { accountManager.handleSecondFactorSuccess(any(), any()) }
    }
}
