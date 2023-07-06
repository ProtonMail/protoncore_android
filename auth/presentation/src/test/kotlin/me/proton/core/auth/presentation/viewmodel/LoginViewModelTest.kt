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

package me.proton.core.auth.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.auth.domain.usecase.IsSsoEnabled
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.SignupLoginTotal
import me.proton.core.observability.domain.metrics.SignupUnlockUserTotalV1
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private val accountHandler = mockk<AccountWorkflowHandler>()

    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

    private val createLoginSession = mockk<CreateLoginSession>()
    private val postLoginAccountSetup = mockk<PostLoginAccountSetup>()

    @MockK(relaxUnitFun = true)
    private lateinit var observabilityManager: ObservabilityManager
    // endregion

    // region test data
    private val testUserName = "test-username"
    private val testPassword = "test-password"
    private val testUserId = UserId("test-user-id")
    // endregion

    private lateinit var viewModel: LoginViewModel

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        viewModel = makeLoginViewModel()
        every { keyStoreCrypto.encrypt(any<String>()) } returns testPassword
    }

    @Test
    fun `login 2FA flow is handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo(isSecondFactorNeeded = true)
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
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
            postLoginAccountSetup.invoke(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
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
            postLoginAccountSetup.invoke(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
        } throws ApiException(ApiResult.Error.NoInternet())

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            assertTrue(awaitItem() is LoginViewModel.State.Error)

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
            assertTrue(errorState is LoginViewModel.State.Error)
            assertEquals("proton error", errorState.error.getUserMessage(mockk()))

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
            postLoginAccountSetup.invoke(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
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
            postLoginAccountSetup.invoke(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
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
            assertTrue(errorState is LoginViewModel.State.Error)
            assertEquals("Primary key exists", errorState.error.getUserMessage(mockk()))

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { createLoginSession.invoke(testUserName, testPassword, any()) }
        coVerify(exactly = 2) {
            postLoginAccountSetup.invoke(
                any(),
                testPassword,
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
        }
    }

    @Test
    fun `handles invalid password`() = coroutinesTest {
        coEvery { createLoginSession.invoke(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(
                HttpResponseCodes.HTTP_UNPROCESSABLE,
                "Unprocessable Content",
                ApiResult.Error.ProtonData(
                    ResponseCodes.PASSWORD_WRONG,
                    "Incorrect login credentials. Please try again"
                )
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, "invalid-password", mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())
            assertIs<LoginViewModel.State.InvalidPassword>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { createLoginSession.invoke(testUserName, any(), any()) }
        coVerify(exactly = 0) {
            postLoginAccountSetup.invoke(
                any(),
                testPassword,
                any(),
                any(),
                any(),
                any(),
                any(),
                subscribeMetricData = any()
            )
        }
    }

    @Test
    fun `handle unsupported external account`() = coroutinesTest {
        coEvery { createLoginSession.invoke(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(
                HttpResponseCodes.HTTP_UNPROCESSABLE,
                "Unprocessable Content",
                ApiResult.Error.ProtonData(
                    ResponseCodes.APP_VERSION_NOT_SUPPORTED_FOR_EXTERNAL_ACCOUNTS,
                    "Get a Proton Mail address linked to this account in your Proton web settings"
                )
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())
            assertIs<LoginViewModel.State.ExternalAccountNotSupported>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handle SSO account`() = coroutinesTest {
        coEvery { createLoginSession.invoke(any(), any(), any()) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 404,
                message = "Email domain associated to an existing organization. Please sign in with SSO",
                proton = ApiResult.Error.ProtonData(8100, "error")
            )
        )

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword, mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())
            assertIs<LoginViewModel.State.SignInWithSso>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isSsoEnabledIsFalse() {
        val viewModel = makeLoginViewModel(isSsoEnabled = { false })
        assertEquals(expected = false, actual = viewModel.isSsoEnabled,)
    }

    @Test
    fun isSsoEnabledIsTrue() {
        val viewModel = makeLoginViewModel(isSsoEnabled = { true })
        assertEquals(expected = true, actual = viewModel.isSsoEnabled)
    }

    @Test
    fun observabilityEventsAreEnqueued() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo()
        coEvery { createLoginSession.invoke(any(), any(), any()) } coAnswers {
            result("performLogin") { sessionInfo }
        }
        coEvery {
            postLoginAccountSetup.invoke(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } coAnswers {
            result("unlockUserPrimaryKey") { PostLoginAccountSetup.Result.UserUnlocked(mockk()) }
        }
        val viewModel = makeLoginViewModel()
        val loginData = mockk<SignupLoginTotal>()
        val unlockData = mockk<SignupUnlockUserTotalV1>()

        viewModel.startLoginWorkflow(
            username = testUserName,
            password = testPassword,
            requiredAccountType = mockk(),
            loginMetricData = { loginData },
            unlockUserMetricData = { unlockData }
        ).join()

        // THEN
        val dataSlots = mutableListOf<ObservabilityData>()
        verify { observabilityManager.enqueue(capture(dataSlots), any()) }
        assertTrue(dataSlots.contains(loginData))
        assertTrue(dataSlots.contains(unlockData))
    }

    private fun makeLoginViewModel(isSsoEnabled: IsSsoEnabled = IsSsoEnabled { false }): LoginViewModel =
        LoginViewModel(
            savedStateHandle,
            accountHandler,
            createLoginSession,
            keyStoreCrypto,
            postLoginAccountSetup,
            isSsoEnabled,
            observabilityManager
        )

    private fun mockSessionInfo(
        isSecondFactorNeeded: Boolean = false,
        isTwoPassModeNeeded: Boolean = false
    ) = mockk<SessionInfo> {
        every { userId } returns testUserId
        every { this@mockk.isSecondFactorNeeded } returns isSecondFactorNeeded
        every { this@mockk.isTwoPassModeNeeded } returns isTwoPassModeNeeded
        every { this@mockk.temporaryPassword } returns false
    }
}
