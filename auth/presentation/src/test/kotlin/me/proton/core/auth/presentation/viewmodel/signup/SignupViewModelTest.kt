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

package me.proton.core.auth.presentation.viewmodel.signup

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.domain.usecase.signup.PerformCreateExternalEmailUser
import me.proton.core.auth.domain.usecase.signup.PerformCreateUser
import me.proton.core.auth.domain.usecase.signup.SignupChallengeConfig
import me.proton.core.auth.presentation.entity.signup.RecoveryMethod
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationExternalInput
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.client.CookieSessionId
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.user.domain.entity.CreateUserType
import me.proton.core.user.domain.entity.User
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SignupViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val humanVerificationExternalInput = mockk<HumanVerificationExternalInput>(relaxed = true)
    private val performCreateUser = mockk<PerformCreateUser>(relaxed = true)
    private val performCreateExternalUser = mockk<PerformCreateExternalEmailUser>(relaxed = true)
    private val keyStoreCrypto = mockk<KeyStoreCrypto>(relaxed = true)
    private val plansOrchestrator = mockk<PlansOrchestrator>(relaxed = true)
    private val paymentsOrchestrator = mockk<PaymentsOrchestrator>(relaxed = true)
    private val clientIdProvider = mockk<ClientIdProvider>(relaxed = true)
    private val performLogin = mockk<PerformLogin>()
    private val challengeManager = mockk<ChallengeManager>(relaxed = true)
    // endregion


    // region test data
    private val testUsername = "test-username"
    private val testClientIdString = "test-clientId"
    private val testClientId = ClientId.CookieSession(CookieSessionId(testClientIdString))
    private val testPassword = "test-password"
    private val testEmail = "test-email"
    private val testPhone = "test-phone"

    private val testUser = User(
        userId = UserId("test-user-id"),
        email = null,
        name = testUsername,
        displayName = null,
        currency = "test-curr",
        credit = 0,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        keys = emptyList()
    )

    private val usernameTakenError: ApiException
        get() = ApiException(
            ApiResult.Error.Http(
                409,
                "Conflict",
                ApiResult.Error.ProtonData(ResponseCodes.USER_CREATE_NAME_INVALID, "Username taken")
            )
        )

    private val signupChallengeConfig = SignupChallengeConfig()
    // endregion

    private lateinit var viewModel: SignupViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = SignupViewModel(
            humanVerificationExternalInput,
            performCreateUser,
            performCreateExternalUser,
            keyStoreCrypto,
            plansOrchestrator,
            paymentsOrchestrator,
            performLogin,
            challengeManager,
            signupChallengeConfig,
            mockk(relaxed = true)
        )
        coEvery { clientIdProvider.getClientId(any()) } returns testClientId
        every { keyStoreCrypto.decrypt(any<String>()) } returns testPassword
        every { keyStoreCrypto.encrypt(any<String>()) } returns "encrypted-$testPassword"

        coEvery {
            performCreateUser.invoke(
                username = testUsername,
                domain = any(),
                password = any(),
                recoveryEmail = any(),
                recoveryPhone = any(),
                referrer = null,
                type = any(),
            )
        } returns testUser.userId

        coEvery {
            performCreateExternalUser.invoke(
                email = testEmail,
                password = any(),
                referrer = null
            )
        } returns testUser.userId
    }

    @Test
    fun `create Internal user no recovery method set`() = coroutinesTest {
        // GIVEN
        viewModel.username = testUsername
        viewModel.setPassword(testPassword)
        viewModel.state.test {
            // WHEN
            viewModel.startCreateUserWorkflow()
            // THEN
            assertTrue(awaitItem() is SignupViewModel.State.Idle)
            assertTrue(awaitItem() is SignupViewModel.State.CreateUserProcessing)
            val successItem = awaitItem()
            assertTrue(successItem is SignupViewModel.State.CreateUserSuccess)
            assertEquals(testUser.userId.id, successItem.userId)

            coVerify(exactly = 1) {
                performCreateUser(
                    username = testUsername,
                    password = "encrypted-$testPassword",
                    recoveryEmail = null,
                    recoveryPhone = null,
                    referrer = null,
                    type = CreateUserType.Normal,
                    domain = any(),
                )
            }
        }
    }

    @Test
    fun `create Internal user email recovery method set`() = coroutinesTest {
        // GIVEN
        val emailRecovery = RecoveryMethod(RecoveryMethodType.EMAIL, testEmail)
        viewModel.username = testUsername
        viewModel.setPassword(testPassword)
        viewModel.setRecoveryMethod(emailRecovery)

        viewModel.state.test {
            // WHEN
            assertIs<SignupViewModel.State.CreateUserInputReady>(awaitItem())
            viewModel.startCreateUserWorkflow()
            // THEN
            assertIs<SignupViewModel.State.CreateUserProcessing>(awaitItem())
            val successItem = awaitItem()
            assertIs<SignupViewModel.State.CreateUserSuccess>(successItem)
            assertEquals(testUser.userId.id, successItem.userId)

            coVerify(exactly = 1) {
                performCreateUser(
                    username = testUsername,
                    password = "encrypted-$testPassword",
                    recoveryEmail = testEmail,
                    recoveryPhone = null,
                    referrer = null,
                    type = CreateUserType.Normal,
                    domain = any(),
                )
            }
        }
    }

    @Test
    fun `create Internal user phone recovery method set`() = coroutinesTest {
        // GIVEN
        val emailRecovery = RecoveryMethod(RecoveryMethodType.SMS, testPhone)
        viewModel.username = testUsername
        viewModel.setPassword(testPassword)
        viewModel.setRecoveryMethod(emailRecovery)
        viewModel.state.test {
            // WHEN
            assertIs<SignupViewModel.State.CreateUserInputReady>(awaitItem())
            viewModel.startCreateUserWorkflow()
            // THEN
            assertIs<SignupViewModel.State.CreateUserProcessing>(awaitItem())
            val successItem = awaitItem()
            assertIs<SignupViewModel.State.CreateUserSuccess>(successItem)
            assertEquals(testUser.userId.id, successItem.userId)

            coVerify(exactly = 1) {
                performCreateUser(
                    username = testUsername,
                    password = "encrypted-$testPassword",
                    recoveryEmail = null,
                    recoveryPhone = testPhone,
                    referrer = null,
                    type = CreateUserType.Normal,
                    domain = any(),
                )
            }
        }
    }

    @Test
    fun `create Internal user API error`() = coroutinesTest {
        // GIVEN
        coEvery {
            performCreateUser.invoke(
                username = testUsername,
                domain = any(),
                password = any(),
                recoveryEmail = any(),
                recoveryPhone = any(),
                referrer = null,
                type = any(),
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 12106,
                    error = "create user error"
                )
            )
        )
        viewModel.username = testUsername
        viewModel.setPassword(testPassword)
        viewModel.state.test {
            // WHEN
            viewModel.startCreateUserWorkflow()
            // THEN
            assertTrue(awaitItem() is SignupViewModel.State.Idle)
            assertTrue(awaitItem() is SignupViewModel.State.CreateUserProcessing)
            val errorItem = awaitItem()
            assertTrue(errorItem is SignupViewModel.State.Error.Message)
            assertEquals("create user error", errorItem.message)

            coVerify(exactly = 1) {
                performCreateUser(
                    username = testUsername,
                    password = "encrypted-$testPassword",
                    recoveryEmail = null,
                    recoveryPhone = null,
                    referrer = null,
                    type = CreateUserType.Normal,
                    domain = any(),
                )
            }
        }
    }

    @Test
    fun `create External user success`() = coroutinesTest {
        // GIVEN
        viewModel.currentAccountType = AccountType.External
        viewModel.externalEmail = testEmail
        viewModel.setPassword(testPassword)
        viewModel.state.test {
            // WHEN
            viewModel.startCreateUserWorkflow()
            // THEN
            assertTrue(awaitItem() is SignupViewModel.State.Idle)
            assertTrue(awaitItem() is SignupViewModel.State.CreateUserProcessing)
            val successItem = awaitItem()
            assertTrue(successItem is SignupViewModel.State.CreateUserSuccess)
            assertEquals(testUser.userId.id, successItem.userId)

            coVerify(exactly = 1) {
                performCreateExternalUser(
                    email = testEmail,
                    password = "encrypted-$testPassword",
                    referrer = null
                )
            }
        }
    }

    @Test
    fun `create External user error`() = coroutinesTest {
        // GIVEN
        coEvery {
            performCreateExternalUser.invoke(
                email = testEmail,
                password = any(),
                referrer = null
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 12106,
                    error = "create user error"
                )
            )
        )

        viewModel.currentAccountType = AccountType.External
        viewModel.externalEmail = testEmail
        viewModel.setPassword(testPassword)
        viewModel.state.test {
            // WHEN
            viewModel.startCreateUserWorkflow()
            // THEN
            assertTrue(awaitItem() is SignupViewModel.State.Idle)
            assertTrue(awaitItem() is SignupViewModel.State.CreateUserProcessing)
            val errorItem = awaitItem()
            assertTrue(errorItem is SignupViewModel.State.Error.Message)
            assertEquals("create user error", errorItem.message)

            coVerify(exactly = 1) {
                performCreateExternalUser(
                    email = testEmail,
                    password = "encrypted-$testPassword",
                    referrer = null
                )
            }
        }
    }

    @Test
    fun `tries login if internal username taken`() = coroutinesTest {
        coEvery {
            performCreateUser.invoke(
                username = testUsername,
                domain = any(),
                password = any(),
                recoveryEmail = any(),
                recoveryPhone = any(),
                referrer = null,
                type = any(),
            )
        } throws usernameTakenError

        coEvery { performLogin.invoke(testUsername, any()) } returns mockk {
            every { userId } returns testUser.userId
        }

        // GIVEN
        viewModel.username = testUsername
        viewModel.setPassword(testPassword)

        viewModel.state.test {
            // WHEN
            viewModel.startCreateUserWorkflow()
            // THEN
            assertTrue(awaitItem() is SignupViewModel.State.Idle)
            assertTrue(awaitItem() is SignupViewModel.State.CreateUserProcessing)
            val successItem = awaitItem()
            assertTrue(successItem is SignupViewModel.State.CreateUserSuccess)
            assertEquals(testUser.userId.id, successItem.userId)

            coVerify(exactly = 1) {
                performCreateUser(
                    username = testUsername,
                    password = "encrypted-$testPassword",
                    recoveryEmail = null,
                    recoveryPhone = null,
                    referrer = null,
                    type = CreateUserType.Normal,
                    domain = any(),
                )
            }

            coVerify(exactly = 1) {
                performLogin(
                    username = testUsername,
                    password = "encrypted-$testPassword"
                )
            }
        }
    }

    @Test
    fun `tries login if External username taken`() = coroutinesTest {
        coEvery {
            performCreateExternalUser.invoke(
                email = testEmail,
                password = any(),
                referrer = null
            )
        } throws usernameTakenError

        coEvery { performLogin.invoke(testEmail, any()) } returns mockk {
            every { userId } returns testUser.userId
        }

        // GIVEN
        viewModel.currentAccountType = AccountType.External
        viewModel.externalEmail = testEmail
        viewModel.setPassword(testPassword)
        viewModel.state.test {
            // WHEN
            viewModel.startCreateUserWorkflow()
            // THEN
            assertTrue(awaitItem() is SignupViewModel.State.Idle)
            assertTrue(awaitItem() is SignupViewModel.State.CreateUserProcessing)
            val successItem = awaitItem()
            assertTrue(successItem is SignupViewModel.State.CreateUserSuccess)
            assertEquals(testUser.userId.id, successItem.userId)

            coVerify(exactly = 1) {
                performCreateExternalUser(
                    email = testEmail,
                    password = "encrypted-$testPassword",
                    referrer = null
                )
            }

            coVerify(exactly = 1) {
                performLogin(
                    username = testEmail,
                    password = "encrypted-$testPassword"
                )
            }
        }
    }
}
