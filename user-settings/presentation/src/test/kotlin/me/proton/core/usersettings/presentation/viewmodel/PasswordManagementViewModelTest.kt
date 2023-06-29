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

package me.proton.core.usersettings.presentation.viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.TwoFASetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateLoginPassword
import me.proton.core.usersettings.domain.usecase.PerformUpdateUserPassword
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PasswordManagementViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    // region mocks
    private val getUserSettingsUseCase = mockk<GetUserSettings>()
    private val performUpdateLoginPassword = mockk<PerformUpdateLoginPassword>()
    private val performUpdateMailboxPassword = mockk<PerformUpdateUserPassword>()
    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    private val userRepository = mockk<UserRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testNewPassword = "test-new-password"

    private val testUserSettingsResponse = UserSettings(
        userId = testUserId,
        email = RecoverySetting("test-email", 1, notify = true, reset = true),
        phone = null,
        twoFA = null,
        password = PasswordSetting(mode = 1, expirationTime = null),
        news = 0,
        locale = "en",
        logAuth = UserSettings.LogAuth.enumOf(1),
        density = UserSettings.Density.enumOf(1),
        dateFormat = UserSettings.DateFormat.enumOf(1),
        timeFormat = UserSettings.TimeFormat.enumOf(2),
        weekStart = UserSettings.WeekStart.enumOf(7),
        earlyAccess = true
    )
    private val testUser = User(
        userId = testUserId,
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
        recovery = null,
        keys = emptyList()
    )
    // endregion

    private lateinit var viewModel: PasswordManagementViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { userRepository.getUser(any()) } returns testUser
        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse
        viewModel =
            PasswordManagementViewModel(
                keyStoreCrypto,
                getUserSettingsUseCase,
                performUpdateLoginPassword,
                performUpdateMailboxPassword
            )
    }

    @Test
    fun `get current settings 2 Pass handled correctly`() = coroutinesTest {
        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse.copy(
            twoFA = TwoFASetting(true, 1, null),
            password = PasswordSetting(mode = 2, expirationTime = null)
        )
        viewModel.state.test {
            // WHEN
            viewModel.init(testUserId)
            // THEN
            assertIs<PasswordManagementViewModel.State.Idle>(awaitItem())
            val result = awaitItem()
            assertTrue(result is PasswordManagementViewModel.State.Mode)
            assertTrue(result.twoPasswordMode)
        }
    }

    @Test
    fun `get current settings 1 Pass handled correctly`() = coroutinesTest {
        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse.copy(
            twoFA = TwoFASetting(true, 1, null),
            password = PasswordSetting(mode = 1, expirationTime = null)
        )
        viewModel.state.test {
            // WHEN
            viewModel.init(testUserId)
            // THEN
            assertIs<PasswordManagementViewModel.State.Idle>(awaitItem())
            val result = awaitItem()
            assertTrue(result is PasswordManagementViewModel.State.Mode)
            assertFalse(result.twoPasswordMode)
        }
    }

    @Test
    fun `update login password handled correctly`() = coroutinesTest {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-password") } returns testNewPassword
        every { keyStoreCrypto.encrypt(testNewPassword) } returns "encrypted-test-new-password"

        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse.copy(
            twoFA = TwoFASetting(true, 1, null),
            password = PasswordSetting(mode = 1, expirationTime = null)
        )

        coEvery { performUpdateLoginPassword.invoke(testUserId, any(), any(), any()) } returns testUserSettingsResponse

        flowTest(viewModel.state) {
            // WHEN
            viewModel.updateLoginPassword(testUserId, testPassword, testNewPassword)
            // THEN
            assertIs<PasswordManagementViewModel.State.Idle>(awaitItem())
            assertIs<PasswordManagementViewModel.State.UpdatingLoginPassword>(awaitItem())
            val result = awaitItem()
            assertTrue(result is PasswordManagementViewModel.State.Success.UpdatingLoginPassword)
            assertNotNull(result.settings)

            coVerify(exactly = 1) {
                performUpdateLoginPassword(
                    userId = testUserId,
                    password = "encrypted-test-password",
                    newPassword = "encrypted-test-new-password",
                    secondFactorCode = ""
                )
            }
        }
    }

    @Test
    fun `update mailbox password 2 pass mode handled correctly`() = coroutinesTest {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-password") } returns testNewPassword
        every { keyStoreCrypto.encrypt(testNewPassword) } returns "encrypted-test-new-password"

        val testLoginPassword = testPassword
        val testNewMailboxPassword = testNewPassword

        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse.copy(
            twoFA = TwoFASetting(true, 1, null),
            password = PasswordSetting(mode = 2, expirationTime = null)
        )

        coEvery { performUpdateMailboxPassword.invoke(any(), any(), any(), any(), any()) } returns true

        flowTest(viewModel.state) {
            // WHEN
            viewModel.init(testUserId)
            viewModel.updateMailboxPassword(testUserId, testLoginPassword, testNewMailboxPassword)
            // THEN
            assertIs<PasswordManagementViewModel.State.Idle>(awaitItem())
            assertIs<PasswordManagementViewModel.State.Mode>(awaitItem())
            assertIs<PasswordManagementViewModel.State.UpdatingMailboxPassword>(awaitItem())
            val result = awaitItem()
            assertTrue(result is PasswordManagementViewModel.State.Success.UpdatingMailboxPassword)

            coVerify(exactly = 1) {
                performUpdateMailboxPassword(
                    userId = testUserId,
                    loginPassword = "encrypted-test-password",
                    newPassword = "encrypted-test-new-password",
                    secondFactorCode = "",
                    twoPasswordMode = true
                )
            }
        }
    }

    @Test
    fun `update mailbox password 1 pass mode handled correctly`() = coroutinesTest {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-password") } returns testNewPassword
        every { keyStoreCrypto.encrypt(testNewPassword) } returns "encrypted-test-new-password"

        val testLoginPassword = testPassword
        val testNewMailboxPassword = testNewPassword

        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse.copy(
            twoFA = TwoFASetting(true, 1, null),
            password = PasswordSetting(mode = 1, expirationTime = null)
        )

        coEvery { performUpdateMailboxPassword.invoke(any(), any(), any(), any(), any()) } returns true

        flowTest(viewModel.state) {
            // WHEN
            viewModel.init(testUserId)
            viewModel.updateMailboxPassword(testUserId, testLoginPassword, testNewMailboxPassword)
            // THEN
            assertIs<PasswordManagementViewModel.State.Idle>(awaitItem())
            assertIs<PasswordManagementViewModel.State.Mode>(awaitItem())
            assertIs<PasswordManagementViewModel.State.UpdatingSinglePassModePassword>(awaitItem())
            val result = awaitItem()
            assertTrue(result is PasswordManagementViewModel.State.Success.UpdatingSinglePassModePassword)

            coVerify(exactly = 1) {
                performUpdateMailboxPassword(
                    userId = testUserId,
                    loginPassword = "encrypted-test-password",
                    newPassword = "encrypted-test-new-password",
                    secondFactorCode = "",
                    twoPasswordMode = false
                )
            }
        }
    }
}
