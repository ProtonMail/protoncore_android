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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.usecase.SetupInternalAddress
import me.proton.core.auth.domain.usecase.SetupPrimaryKeys
import me.proton.core.auth.domain.usecase.UnlockUserPrimaryKey
import me.proton.core.domain.entity.UserId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.usersettings.domain.usecase.SetupUsername
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CreateAddressViewModelTest : ArchTest, CoroutinesTest {

    // region mocks
    private val accountHandler = mockk<AccountWorkflowHandler>(relaxed = true)
    private val userManager = mockk<UserManager>(relaxed = true)
    private val setupUsername = mockk<SetupUsername>(relaxed = true)
    private val setupInternalAddress = mockk<SetupInternalAddress>(relaxed = true)
    private val setupPrimaryKeys = mockk<SetupPrimaryKeys>(relaxed = true)
    private val unlockUserPrimaryKey = mockk<UnlockUserPrimaryKey>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testPassword = "test-password"
    private val testUsername = "test-username"
    private val testDomain = "test-domain"
    private val testUser = mockk<User>()
    private val testUserKey = mockk<UserKey>()
    private val testAddressInternal = mockk<UserAddress>()
    private val testAddressInternalWithoutKeys = mockk<UserAddress>()
    private val testAddressExternal = mockk<UserAddress>()
    // endregion

    private lateinit var viewModel: CreateAddressViewModel

    @Before
    fun beforeEveryTest() {
        viewModel = CreateAddressViewModel(
            accountHandler,
            userManager,
            setupUsername,
            setupPrimaryKeys,
            setupInternalAddress,
            unlockUserPrimaryKey
        )
        coEvery { userManager.getUser(any(), any()) } returns testUser

        coEvery { setupUsername.invoke(any(), any()) } returns Unit
        coEvery { setupPrimaryKeys.invoke(any(), any(), any()) } returns Unit
        coEvery { setupInternalAddress.invoke(any(), any()) } returns Unit
        coEvery { unlockUserPrimaryKey.invoke(any(), any()) } returns UserManager.UnlockResult.Success

        every { testAddressInternal.type } returns AddressType.Original
        every { testAddressInternal.keys } returns listOf(mockk())

        every { testAddressInternalWithoutKeys.type } returns AddressType.Original
        every { testAddressInternalWithoutKeys.keys } returns emptyList()

        every { testAddressExternal.type } returns AddressType.External
    }

    @Test
    fun `setup username and primary keys`() = coroutinesTest {
        // GIVEN
        every { testUser.keys } returns emptyList()
        coEvery { userManager.getAddresses(any(), any()) } returns emptyList()
        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }
            coVerify(exactly = 1) { setupPrimaryKeys.invoke(any(), any(), any()) }
            coVerify(exactly = 0) { setupInternalAddress.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleCreateAddressSuccess(any()) }
            coVerify(exactly = 1) { unlockUserPrimaryKey.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleAccountReady(any()) }

            assertIs<CreateAddressViewModel.State.Processing>(expectItem())
            assertIs<CreateAddressViewModel.State.Success>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup username and internal address`() = coroutinesTest {
        // GIVEN
        every { testUser.keys } returns listOf(testUserKey)
        coEvery { userManager.getAddresses(any(), any()) } returns listOf(testAddressExternal)
        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            coVerify(exactly = 0) { setupPrimaryKeys.invoke(any(), any(), any()) }
            coVerify(exactly = 1) { setupInternalAddress.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleCreateAddressSuccess(any()) }

            coVerify(exactly = 1) { unlockUserPrimaryKey.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleAccountReady(any()) }

            assertIs<CreateAddressViewModel.State.Processing>(expectItem())
            assertIs<CreateAddressViewModel.State.Success>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup username and internal address because no keys`() = coroutinesTest {
        // GIVEN
        every { testUser.keys } returns listOf(testUserKey)
        coEvery { userManager.getAddresses(any(), any()) } returns listOf(testAddressInternalWithoutKeys)
        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            coVerify(exactly = 0) { setupPrimaryKeys.invoke(any(), any(), any()) }
            coVerify(exactly = 1) { setupInternalAddress.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleCreateAddressSuccess(any()) }

            coVerify(exactly = 1) { unlockUserPrimaryKey.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleAccountReady(any()) }

            assertIs<CreateAddressViewModel.State.Processing>(expectItem())
            assertIs<CreateAddressViewModel.State.Success>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup username and unlock`() = coroutinesTest {
        // GIVEN
        every { testUser.keys } returns listOf(testUserKey)
        coEvery { userManager.getAddresses(any(), any()) } returns listOf(testAddressInternal)
        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            coVerify(exactly = 0) { setupPrimaryKeys.invoke(any(), any(), any()) }
            coVerify(exactly = 0) { setupInternalAddress.invoke(any(), any()) }
            coVerify(exactly = 0) { accountHandler.handleCreateAddressSuccess(any()) }

            coVerify(exactly = 1) { unlockUserPrimaryKey.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleAccountReady(any()) }

            assertIs<CreateAddressViewModel.State.Processing>(expectItem())
            assertIs<CreateAddressViewModel.State.Success>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setup cannot unlock`() = coroutinesTest {
        // GIVEN
        every { testUser.keys } returns emptyList()
        coEvery { userManager.getAddresses(any(), any()) } returns emptyList()
        coEvery {
            unlockUserPrimaryKey.invoke(
                any(),
                any()
            )
        } returns UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase
        viewModel.state.test {
            // WHEN
            viewModel.upgradeAccount(testUserId, testPassword, testUsername, testDomain)

            // THEN
            coVerify(exactly = 1) { setupUsername.invoke(any(), any()) }

            coVerify(exactly = 1) { setupPrimaryKeys.invoke(any(), any(), any()) }
            coVerify(exactly = 0) { setupInternalAddress.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleCreateAddressSuccess(any()) }

            coVerify(exactly = 1) { unlockUserPrimaryKey.invoke(any(), any()) }
            coVerify(exactly = 1) { accountHandler.handleUnlockFailed(any()) }
            coVerify(exactly = 0) { accountHandler.handleAccountReady(any()) }

            assertIs<CreateAddressViewModel.State.Processing>(expectItem())
            assertIs<CreateAddressViewModel.State.Error.CannotUnlockPrimaryKey>(expectItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
