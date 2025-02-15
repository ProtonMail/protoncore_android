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
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.auth.domain.feature.IsSsoEnabled
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
import me.proton.core.observability.domain.metrics.SignupUserCheckTotalV1
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.UserManager
import me.proton.core.util.kotlin.coroutine.result
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private val accountHandler = mockk<AccountWorkflowHandler>(relaxed = true)

    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    private val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)

    private val createLoginSession = mockk<CreateLoginSession>()
    private val postLoginAccountSetup = mockk<PostLoginAccountSetup>()

    @MockK(relaxUnitFun = true)
    private lateinit var observabilityManager: ObservabilityManager
    @MockK(relaxUnitFun = true)
    private lateinit var telemetryManager: TelemetryManager
    // endregion

    // region test data
    private val testUserName = "test-username"
    private val testPassword = "test-password"
    private val testUserId = UserId("test-user-id")
    private val requiredAccountType = AccountType.Internal
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
        coEvery { createLoginSession.invoke(any(), any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any()
            )
        } returns PostLoginAccountSetup.Result.Need.SecondFactor(testUserId)

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword)

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
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any()
            )
        } returns PostLoginAccountSetup.Result.AccountReady(testUserId)

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword)

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())

            val successState = awaitItem()
            assertTrue(successState is LoginViewModel.State.AccountSetupResult)
            val result = successState.result
            assertTrue(result is PostLoginAccountSetup.Result.AccountReady)
            assertEquals(sessionInfo.userId, result.userId)

            verify { savedStateHandle.set(any(), any<String>()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login get user error flow states are handled correctly`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo()
        val viewModel = spyk(viewModel, recordPrivateCalls = true)
        every { viewModel.userId } answers { testUserId }

        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any()
            )
        } throws ApiException(ApiResult.Error.NoInternet())

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword)

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
        val viewModel = spyk(viewModel, recordPrivateCalls = true)
        every { viewModel.userId } answers { testUserId }

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
            viewModel.startLoginWorkflow(testUserName, testPassword)

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
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any()
            )
        } returns PostLoginAccountSetup.Result.Need.ChangePassword(testUserId)

        viewModel.state.test {
            // WHEN
            viewModel.startLoginWorkflow(testUserName, testPassword)

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
        val viewModel = spyk(viewModel, recordPrivateCalls = true)
        every { viewModel.userId } answers { testUserId }

        coJustRun { accountHandler.handleAccountDisabled(any()) }
        coEvery { createLoginSession.invoke(any(), any(), any()) } returns sessionInfo
        coEvery {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any()
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
            viewModel.startLoginWorkflow(testUserName, testPassword)

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
                userId = any(),
                encryptedPassword = testPassword,
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any()
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
            viewModel.startLoginWorkflow(testUserName, password = "invalid-password", mockk())

            // THEN
            assertIs<LoginViewModel.State.Processing>(awaitItem())
            assertIs<LoginViewModel.State.InvalidPassword>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { createLoginSession.invoke(testUserName, any(), any()) }
        coVerify(exactly = 0) {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = testPassword,
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any()
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
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any(),
                billingDetails = any(),
                internalAddressDomain = any()
            )
        } coAnswers {
            result("defaultUserCheck") { PostLoginAccountSetup.UserCheckResult.Success }
            result("unlockUserPrimaryKey") { UserManager.UnlockResult.Success }
            PostLoginAccountSetup.Result.AccountReady(mockk())
        }
        val viewModel = makeLoginViewModel()
        val loginData = mockk<SignupLoginTotal>()
        val unlockData = mockk<SignupUnlockUserTotalV1>()
        val userCheckData = mockk<SignupUserCheckTotalV1>()

        viewModel.startLoginWorkflow(
            username = testUserName,
            password = testPassword,
            loginMetricData = { loginData },
            unlockUserMetricData = { unlockData },
            userCheckMetricData = { userCheckData }
        ).join()

        // THEN
        val dataSlots = mutableListOf<ObservabilityData>()
        verify { observabilityManager.enqueue(capture(dataSlots), any()) }
        assertTrue(dataSlots.contains(loginData))
        assertTrue(dataSlots.contains(unlockData))
        assertTrue(dataSlots.contains(userCheckData))
    }

    @Test
    fun `login returns correct state telemetry event correctly enqueued`() = coroutinesTest {
        // GIVEN
        val sessionInfo = mockSessionInfo()
        coEvery { createLoginSession.invoke(any(), any(), any()) } coAnswers {
            result("performLogin") { sessionInfo }
        }

        coEvery {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any(),
                billingDetails = any(),
                internalAddressDomain = any()
            )
        } coAnswers {
            result("defaultUserCheck") { PostLoginAccountSetup.UserCheckResult.Error("error") }
            result("unlockUserPrimaryKey") { UserManager.UnlockResult.Success }
            PostLoginAccountSetup.Result.Error.UserCheckError(PostLoginAccountSetup.UserCheckResult.Error("error"))
        }
        val viewModel = makeLoginViewModel()

        viewModel.startLoginWorkflow(
            username = testUserName,
            password = testPassword,
        ).join()

        // THEN
        val telemetryEventSlot = slot<TelemetryEvent>()
        verify { telemetryManager.enqueue(null, capture(telemetryEventSlot)) }
        val telemetryEvent = telemetryEventSlot.captured
        assertEquals("be.signin.auth", telemetryEvent.name)
        assertEquals("account.any.signup", telemetryEvent.group)
        assertEquals(
            mapOf("account_type" to "internal", "flow" to "mobile_signup_full", "result" to "success"),
            telemetryEvent.dimensions
        )
    }

    @Test
    fun `login returns error telemetry event correctly enqueued`() = coroutinesTest {
        // GIVEN
        coEvery { createLoginSession.invoke(any(), any(), any()) } coAnswers {
            result("performLogin") {
                throw ApiException(
                    ApiResult.Error.Http(
                        httpCode = 123,
                        "http error",
                        ApiResult.Error.ProtonData(
                            code = 1234,
                            error = "proton error"
                        )
                    )
                    )
            }
        }
        coEvery {
            postLoginAccountSetup.invoke(
                userId = any(),
                encryptedPassword = any(),
                requiredAccountType = any(),
                isSecondFactorNeeded = any(),
                isTwoPassModeNeeded = any(),
                temporaryPassword = any(),
                onSetupSuccess = any(),
                billingDetails = any(),
                internalAddressDomain = any()
            )
        } coAnswers {
            result("defaultUserCheck") { PostLoginAccountSetup.UserCheckResult.Success }
            result("unlockUserPrimaryKey") { UserManager.UnlockResult.Success }
            PostLoginAccountSetup.Result.AccountReady(mockk())
        }
        val viewModel = spyk(makeLoginViewModel(), recordPrivateCalls = true)
        every { viewModel.userId } answers { testUserId }

        viewModel.startLoginWorkflow(
            username = testUserName,
            password = testPassword,
        ).join()

        // THEN
        val telemetryEventSlot = slot<TelemetryEvent>()
        verify { telemetryManager.enqueue(null, capture(telemetryEventSlot)) }
        val telemetryEvent = telemetryEventSlot.captured
        assertEquals("be.signin.auth", telemetryEvent.name)
        assertEquals("account.any.signup", telemetryEvent.group)
        assertEquals(
            mapOf("account_type" to "internal", "flow" to "mobile_signup_full", "result" to "failure"),
            telemetryEvent.dimensions
        )
    }

    private fun makeLoginViewModel(isSsoEnabled: IsSsoEnabled = IsSsoEnabled { false }): LoginViewModel {
        coJustRun { accountHandler.handleAccountDisabled(any()) }

        return LoginViewModel(
            requiredAccountType = requiredAccountType,
            savedStateHandle = savedStateHandle,
            accountWorkflow = accountHandler,
            createLoginSession = createLoginSession,
            keyStoreCrypto = keyStoreCrypto,
            postLoginAccountSetup = postLoginAccountSetup,
            isSsoEnabled = isSsoEnabled,
            observabilityManager = observabilityManager,
            telemetryManager = telemetryManager
        )
    }

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
