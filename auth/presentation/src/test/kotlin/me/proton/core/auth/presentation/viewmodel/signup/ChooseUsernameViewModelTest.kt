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
import io.mockk.mockk
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.UsernameDomainAvailability
import me.proton.core.auth.domain.usecase.signup.SignupChallengeConfig
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.humanverification.domain.usecase.SendVerificationCodeToEmailDestination
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChooseUsernameViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {

    // region mocks
    private val usernameDomainAvailability = mockk<UsernameDomainAvailability>(relaxed = true)
    private val sendVerificationCodeToEmailDestination = mockk<SendVerificationCodeToEmailDestination>(relaxed = true)
    private val challengeManager = mockk<ChallengeManager>(relaxed = true)
    // endregion

    private val signupChallengeConfig = SignupChallengeConfig()
    private lateinit var viewModel: ChooseUsernameViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { usernameDomainAvailability.getDomains() } returns listOf("protonmail.com", "protonmail.ch")
    }

    @Test
    fun `domains are loaded correctly`() = coroutinesTest {
        // GIVEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        // WHEN
        viewModel.state.test {
            // THEN
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            val domainsItem = awaitItem() as ChooseUsernameViewModel.State.AvailableDomains
            assertEquals(listOf("protonmail.com", "protonmail.ch"), domainsItem.domains)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `domains loading connectivity error`() = coroutinesTest {
        // GIVEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        coEvery { usernameDomainAvailability.getDomains() } throws ApiException(
            ApiResult.Error.NoInternet()
        )
        // WHEN
        viewModel.state.test {
            // THEN
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Error)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `domains loading api error`() = coroutinesTest {
        // GIVEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        coEvery { usernameDomainAvailability.getDomains() } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "domains error"
                )
            )
        )
        // WHEN
        viewModel.state.test {
            // THEN
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            val errorItem = awaitItem() as ChooseUsernameViewModel.State.Error.Message
            assertEquals("domains error", errorItem.error.getUserMessage(mockk()))
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `set Internal Account Type can NOT switch to External`() = coroutinesTest {
        // WHEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        viewModel.selectedAccountTypeState.test {
            // THEN
            val event = awaitItem()
            assertTrue(event is ChooseUsernameViewModel.AccountTypeState.NewAccountType)
            assertEquals(AccountType.Internal, event.type)

            viewModel.onUserSwitchAccountType()
            val eventAfterChange = awaitItem()
            assertTrue(eventAfterChange is ChooseUsernameViewModel.AccountTypeState.NewAccountType)
            assertEquals(AccountType.Internal, eventAfterChange.type)
        }
    }

    @Test
    fun `set Internal Account Type can NOT switch to External a couple of tries`() = coroutinesTest {
        // WHEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        viewModel.selectedAccountTypeState.test {
            // THEN
            val event = awaitItem()
            assertTrue(event is ChooseUsernameViewModel.AccountTypeState.NewAccountType)
            assertEquals(AccountType.Internal, event.type)

            viewModel.onUserSwitchAccountType()
            val eventAfterChange = awaitItem()
            assertTrue(eventAfterChange is ChooseUsernameViewModel.AccountTypeState.NewAccountType)
            assertEquals(AccountType.Internal, eventAfterChange.type)

            viewModel.onUserSwitchAccountType()
            val eventAfter2ndChange = awaitItem()
            assertTrue(eventAfter2ndChange is ChooseUsernameViewModel.AccountTypeState.NewAccountType)
            assertEquals(AccountType.Internal, eventAfter2ndChange.type)

            viewModel.onUserSwitchAccountType()
            val eventAfter3rdChange = awaitItem()
            assertTrue(eventAfter3rdChange is ChooseUsernameViewModel.AccountTypeState.NewAccountType)
            assertEquals(AccountType.Internal, eventAfter3rdChange.type)
        }
    }

    @Test
    fun `check username for Internal is available`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        coEvery { usernameDomainAvailability.isUsernameAvailable(testEmail) } returns true
        viewModel.state.test {
            viewModel.checkUsername(testUsername, testDomain)
            // THEN
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.AvailableDomains)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            val item = awaitItem() as ChooseUsernameViewModel.State.UsernameAvailable
            assertEquals(testUsername, item.username)
            assertEquals(testDomain, item.domain)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username for Internal is NOT available`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        // WHEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        coEvery { usernameDomainAvailability.isUsernameAvailable(testEmail) } returns false
        viewModel.state.test {
            viewModel.checkUsername(testUsername, testDomain)
            // THEN
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.AvailableDomains)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Error.UsernameNotAvailable)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username for Internal API error`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        val testDomain = "test-domain"
        val testEmail = "$testUsername@$testDomain"
        coEvery { usernameDomainAvailability.isUsernameAvailable(testEmail) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 12106,
                    error = "username not available"
                )
            )
        )
        // WHEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.Internal
        )
        viewModel.state.test {
            viewModel.checkUsername(testUsername, testDomain)
            // THEN
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.AvailableDomains)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Error.Message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username for External token sent`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        coEvery { sendVerificationCodeToEmailDestination.invoke(emailAddress = testUsername) } returns Unit
        // WHEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.External
        )
        viewModel.state.test {
            viewModel.checkUsername(testUsername)

            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is ChooseUsernameViewModel.State.ExternalAccountTokenSent)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `check username for External token NOT sent`() = coroutinesTest {
        // GIVEN
        val testUsername = "test-username"
        coEvery { sendVerificationCodeToEmailDestination.invoke(emailAddress = testUsername) } throws Exception("Error with the email")
        // WHEN
        viewModel = ChooseUsernameViewModel(
            usernameDomainAvailability,
            sendVerificationCodeToEmailDestination,
            challengeManager,
            signupChallengeConfig,
            AccountType.External
        )
        viewModel.state.test {
            viewModel.checkUsername(testUsername)

            assertTrue(awaitItem() is ChooseUsernameViewModel.State.Processing)
            val errorItem = awaitItem()
            assertTrue(errorItem is ChooseUsernameViewModel.State.Error.Message)
            assertEquals("Error with the email", errorItem.error.getUserMessage(mockk()))
            cancelAndConsumeRemainingEvents()
        }
    }
}
