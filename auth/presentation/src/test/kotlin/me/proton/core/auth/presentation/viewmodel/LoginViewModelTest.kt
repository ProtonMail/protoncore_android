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
import io.mockk.verify
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.presentation.HumanVerificationOrchestrator
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountHandler = mockk<AccountWorkflowHandler>()

    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
    private val humanVerificationManager = mockk<HumanVerificationManager>()
    private val humanVerificationOrchestrator = mockk<HumanVerificationOrchestrator>()

    private val createLoginSession = mockk<CreateLoginSession>()
    private val postLoginAccountSetup = mockk<PostLoginAccountSetup>()
    // endregion

    // region test data
    private val testUserName = "test-username"
    private val testPassword = "test-password"
    private val testUserId = UserId("test-user-id")
    // endregion

    private lateinit var viewModel: LoginViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = LoginViewModel(
            savedStateHandle,
            accountHandler,
            createLoginSession,
            keyStoreCrypto,
            postLoginAccountSetup,
            humanVerificationManager,
            humanVerificationOrchestrator
        )
        every { keyStoreCrypto.encrypt(any<String>()) } returns testPassword
    }

    @Test
    fun `login 2FA flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo(isSecondFactorNeeded = true)
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(any(), any(), any(), any(), any())
        } returns PostLoginAccountSetup.Result.Need.SecondFactor(testUserId)

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            val successState = awaitItem()
            assertTrue(successState is LoginViewModel.State.AccountSetupResult)
            val result = successState.result
            assertTrue(result is PostLoginAccountSetup.Result.Need.SecondFactor)
            assertEquals(sessionInfo.userId, result.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo()
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(any(), any(), any(), any(), any())
        } returns PostLoginAccountSetup.Result.UserUnlocked(testUserId)

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            val successState = awaitItem()
            assertTrue(successState is LoginViewModel.State.AccountSetupResult)
            val result = successState.result
            assertTrue(result is PostLoginAccountSetup.Result.UserUnlocked)
            assertEquals(sessionInfo.userId, result.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login get user error flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo()
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(any(), any(), any(), any(), any())
        } throws ApiException(ApiResult.Error.NoInternet())

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            assertTrue(awaitItem() is LoginViewModel.State.ErrorMessage)

            verify { savedStateHandle.set(any(), any<String>()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login error path flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        coEvery { createLoginSession.invoke(any(), any(), any()) } throws ApiException(
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
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is LoginViewModel.State.ErrorMessage)
            assertEquals("proton error", errorState.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login private user of organization returns correct state`() = coroutinesTest {
        // GIVEN
        val requiredAccountType = AccountType.Internal
        val sessionInfo = mockSessionInfo()
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(any(), any(), any(), any(), any())
        } returns PostLoginAccountSetup.Result.Need.ChangePassword(testUserId)

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, requiredAccountType)

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            val state = awaitItem()
            assertTrue(state is LoginViewModel.State.AccountSetupResult)
            val result = state.result
            assertIs<PostLoginAccountSetup.Result.Need.ChangePassword>(result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login is retried on Primary Key Exists error`() = coroutinesTest {
        val sessionInfo = mockSessionInfo()
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(any(), any(), any(), any(), any(), any(), any())
        } throws ApiException(
            ApiResult.Error.Http(
                400,
                "Bad request",
                ApiResult.Error.ProtonData(ResponseCodes.NOT_ALLOWED, "Primary key exists")
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())
            assertIs<LoginViewModel.State.Processing>(awaitItem()) // retried

            val errorState = awaitItem()
            assertTrue(errorState is LoginViewModel.State.ErrorMessage)
            assertEquals("Primary key exists", errorState.message)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { createLoginSession.invoke(testUserName, testPassword, any()) }
        coVerify(exactly = 2) { postLoginAccountSetup.invoke(any(), testPassword, any(), any(), any(), any(), any()) }
    }

    private fun mockSessionInfo(
        isSecondFactorNeeded: Boolean = false,
        isTwoPassModeNeeded: Boolean = false
    ) = mockk<SessionInfo> {
        every { userId } returns testUserId
        every { this@mockk.isSecondFactorNeeded } returns isSecondFactorNeeded
        every { this@mockk.isTwoPassModeNeeded } returns isTwoPassModeNeeded
    }
}
