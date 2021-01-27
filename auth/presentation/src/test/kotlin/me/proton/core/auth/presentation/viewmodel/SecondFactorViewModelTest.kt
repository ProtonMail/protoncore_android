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
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.usecase.PerformSecondFactor
import me.proton.core.auth.domain.usecase.SetupAccountCheck
import me.proton.core.auth.domain.usecase.SetupOriginalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.network.domain.session.SessionId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecondFactorViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountManager = mockk<AccountWorkflowHandler>(relaxed = true)
    private val performSecondFactor = mockk<PerformSecondFactor>()
    private val unlockUserPrimaryKey = mockk<UnlockUserPrimaryKey>()
    private val setupAccountCheck = mockk<SetupAccountCheck>()
    private val setupPrimaryKeys = mockk<SetupPrimaryKeys>(relaxed = true)
    private val setupOriginalAddress = mockk<SetupOriginalAddress>(relaxed = true)

    private val testSessionResult = mockk<SessionResult>(relaxed = true)
    private val testScopeInfo = mockk<ScopeInfo>(relaxed = true)
    // endregion

    // region test data
    private val testSessionId = "test-session-id"
    private val testSecondFactorCode = "123456"
    private val testLoginPassword = "123456".toByteArray()
    // endregion

    private lateinit var viewModel: SecondFactorViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = SecondFactorViewModel(
            accountManager,
            performSecondFactor,
            unlockUserPrimaryKey,
            setupAccountCheck,
            setupPrimaryKeys,
            setupOriginalAddress
        )
        every { testSessionResult.sessionId } returns testSessionId
        every { testSessionResult.isTwoPassModeNeeded } returns false
    }

    @Test
    fun `submit 2fa happy flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = UserType.Internal
        coEvery { performSecondFactor.invoke(SessionId(testSessionId), testSecondFactorCode) } returns testScopeInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.NoSetupNeeded
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success
        val observer = mockk<(SecondFactorViewModel.State) -> Unit>(relaxed = true)
        viewModel.secondFactorState.observeDataForever(observer)
        // WHEN
        viewModel.startSecondFactorFlow(
            testLoginPassword,
            requiredAccountType,
            testSessionResult,
            testSecondFactorCode
        )
        // THEN
        val arguments = mutableListOf<SecondFactorViewModel.State>()
        verify(exactly = 2) { observer(capture(arguments)) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is SecondFactorViewModel.State.Processing)
        assertTrue(successState is SecondFactorViewModel.State.Success.UserUnLocked)
    }

    @Test
    fun `submit 2fa two pass mode flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = UserType.Internal
        every { testSessionResult.isTwoPassModeNeeded } returns true
        coEvery { performSecondFactor.invoke(SessionId(testSessionId), testSecondFactorCode) } returns testScopeInfo
        coEvery { setupAccountCheck.invoke(any(), any(), any()) } returns SetupAccountCheck.Result.TwoPassNeeded
        val observer = mockk<(SecondFactorViewModel.State) -> Unit>(relaxed = true)
        viewModel.secondFactorState.observeDataForever(observer)
        // WHEN
        viewModel.startSecondFactorFlow(
            testLoginPassword,
            requiredAccountType,
            testSessionResult,
            testSecondFactorCode
        )
        // THEN
        val arguments = mutableListOf<SecondFactorViewModel.State>()
        val accountManagerArguments = slot<SessionId>()
        verify(exactly = 2) { observer(capture(arguments)) }
        coVerify(exactly = 1) { accountManager.handleSecondFactorSuccess(capture(accountManagerArguments), any()) }
        coVerify(exactly = 0) { accountManager.handleSecondFactorFailed(any()) }
        val processingState = arguments[0]
        val successState = arguments[1]
        assertTrue(processingState is SecondFactorViewModel.State.Processing)
        assertTrue(successState is SecondFactorViewModel.State.Need.TwoPassMode)
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
