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

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.usersettings.domain.usecase.SetupUsername
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
class CreateAddressViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val postLoginAccountSetup = mockk<PostLoginAccountSetup>()
    private val setupUsername = mockk<SetupUsername>()
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPassword = "test-password"
    private val testUsername = "test-username"
    private val testDomain = "test-domain"
    // endregion

    private lateinit var viewModel: CreateAddressViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = CreateAddressViewModel(
            mockk(),
            postLoginAccountSetup,
            setupUsername
        )

        coEvery { setupUsername.invoke(any(), any()) } returns Unit
    }

    @Test
    fun `setup username and primary keys`() = coroutinesTest {
        // GIVEN
        mockPostLoginAccountSetup(PostLoginAccountSetup.Result.UserUnlocked(testUserId))

        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }
            coVerify(exactly = 1) {
                postLoginAccountSetup.invoke(
                    testUserId, testPassword, AccountType.Internal,
                    isSecondFactorNeeded = false,
                    isTwoPassModeNeeded = false,
                    temporaryPassword = false,
                    onSetupSuccess = any(),
                    internalAddressDomain = testDomain
                )
            }

            assertIs<CreateAddressViewModel.State.Processing>(awaitItem())
            val accountSetupResult = assertIs<CreateAddressViewModel.State.AccountSetupResult>(awaitItem())
            assertIs<PostLoginAccountSetup.Result.UserUnlocked>(accountSetupResult.result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup username and internal address`() = coroutinesTest {
        // GIVEN
        mockPostLoginAccountSetup(PostLoginAccountSetup.Result.UserUnlocked(testUserId))

        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            assertIs<CreateAddressViewModel.State.Processing>(awaitItem())
            val accountSetupResult = assertIs<CreateAddressViewModel.State.AccountSetupResult>(awaitItem())
            assertIs<PostLoginAccountSetup.Result.UserUnlocked>(accountSetupResult.result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup username and internal address because no keys`() = coroutinesTest {
        // GIVEN
        mockPostLoginAccountSetup(PostLoginAccountSetup.Result.UserUnlocked(testUserId))

        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            assertIs<CreateAddressViewModel.State.Processing>(awaitItem())
            val accountSetupResult = assertIs<CreateAddressViewModel.State.AccountSetupResult>(awaitItem())
            assertIs<PostLoginAccountSetup.Result.UserUnlocked>(accountSetupResult.result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup username and unlock`() = coroutinesTest {
        // GIVEN
        mockPostLoginAccountSetup(PostLoginAccountSetup.Result.UserUnlocked(testUserId))

        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            assertIs<CreateAddressViewModel.State.Processing>(awaitItem())
            val accountSetupResult = assertIs<CreateAddressViewModel.State.AccountSetupResult>(awaitItem())
            assertIs<PostLoginAccountSetup.Result.UserUnlocked>(accountSetupResult.result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup cannot unlock`() = coroutinesTest {
        // GIVEN
        mockPostLoginAccountSetup(PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError(mockk()))

        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            assertIs<CreateAddressViewModel.State.Processing>(awaitItem())
            val accountSetupResult = assertIs<CreateAddressViewModel.State.AccountSetupResult>(awaitItem())
            assertIs<PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError>(accountSetupResult.result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handles exception`() = coroutinesTest {
        // GIVEN
        coEvery { setupUsername.invoke(any(), any()) } throws Throwable("Something went wrong")

        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            assertIs<CreateAddressViewModel.State.Processing>(awaitItem())
            val result = assertIs<CreateAddressViewModel.State.Error>(awaitItem())
            assertEquals("Something went wrong", result.error.getUserMessage(mockk()))
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun mockPostLoginAccountSetup(result: PostLoginAccountSetup.Result) {
        coEvery {
            postLoginAccountSetup.invoke(
                testUserId,
                testPassword,
                AccountType.Internal,
                isSecondFactorNeeded = false,
                isTwoPassModeNeeded = false,
                temporaryPassword = false,
                onSetupSuccess = any(),
                billingDetails = null,
                internalAddressDomain = testDomain
            )
        } returns result
    }
}
