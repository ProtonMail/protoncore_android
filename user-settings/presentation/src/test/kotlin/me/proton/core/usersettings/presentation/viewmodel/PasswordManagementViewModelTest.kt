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
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountrecovery.domain.IsAccountRecoveryResetEnabled
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecovery
import me.proton.core.accountrecovery.domain.usecase.ObserveUserRecoverySelfInitiated
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.TwoFASetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.IsSessionAccountRecoveryEnabled
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import me.proton.core.usersettings.domain.usecase.PerformResetUserPassword
import me.proton.core.usersettings.domain.usecase.PerformUpdateLoginPassword
import me.proton.core.usersettings.domain.usecase.PerformUpdateUserPassword
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.Action.ObserveState
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.Action.UpdatePassword
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.PasswordType.Both
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.PasswordType.Login
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.PasswordType.Mailbox
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.State
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PasswordManagementViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    // region mocks
    private val observeUserRecovery = mockk<ObserveUserRecovery>()
    private val observeUserSettings = mockk<ObserveUserSettings>()
    private val observeUserRecoverySelfInitiated = mockk<ObserveUserRecoverySelfInitiated>()
    private val performUpdateLoginPassword = mockk<PerformUpdateLoginPassword>()
    private val performUpdateMailboxPassword = mockk<PerformUpdateUserPassword>()
    private val performResetUserPassword = mockk<PerformResetUserPassword>()
    private val isAccountRecoveryResetEnabled = mockk<IsAccountRecoveryResetEnabled>()
    private val observabilityManager = mockk<ObservabilityManager>()
    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    private val isSessionAccountRecoveryEnabled = mockk<IsSessionAccountRecoveryEnabled>()
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testUsername = "test-username"
    private val testPassword = "test-password"
    private val testNewPassword = "test-new-password"

    private val testUserSettingsResponse = UserSettings.nil(testUserId).copy(
        email = RecoverySetting("test-email", 1, notify = true, reset = true),
        password = PasswordSetting(mode = 1, expirationTime = null),
        logAuth = UserSettings.LogAuth.enumOf(1),
        density = UserSettings.Density.enumOf(1),
        dateFormat = UserSettings.DateFormat.enumOf(1),
        timeFormat = UserSettings.TimeFormat.enumOf(2),
        weekStart = UserSettings.WeekStart.enumOf(7),
        earlyAccess = true,
        deviceRecovery = true,
        telemetry = true,
        crashReports = true
    )
    private val testUser = User(
        userId = testUserId,
        email = null,
        name = testUsername,
        displayName = null,
        currency = "test-curr",
        credit = 0,
        createdAtUtc = 1000L,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        recovery = null,
        keys = emptyList(),
        type = Type.Proton
    )
    // endregion

    private lateinit var viewModel: PasswordManagementViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { isAccountRecoveryResetEnabled.invoke(testUserId) } returns false
        coEvery { observeUserRecovery.invoke(testUserId) } returns flowOf(testUser.recovery)
        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(testUserSettingsResponse)
        coEvery { observeUserRecoverySelfInitiated.invoke(testUserId) } returns flowOf(false)
        viewModel =
            PasswordManagementViewModel(
                keyStoreCrypto,
                observeUserRecovery,
                observeUserSettings,
                observeUserRecoverySelfInitiated,
                performUpdateLoginPassword,
                performUpdateMailboxPassword,
                performResetUserPassword,
                isAccountRecoveryResetEnabled,
                observabilityManager,
                isSessionAccountRecoveryEnabled,
                Product.Mail
            )
    }

    @Test
    fun `get current settings 2 Pass handled correctly`() = coroutinesTest {
        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(
            testUserSettingsResponse.copy(
                twoFA = TwoFASetting(true, 1, null),
                password = PasswordSetting(mode = 2, expirationTime = null)
            )
        )
        viewModel.state.test {
            // WHEN
            viewModel.perform(ObserveState(testUserId))
            // THEN
            assertIs<State.Idle>(awaitItem())
            val result = awaitItem()
            assertTrue(result is State.ChangePassword)
            assertTrue(result.mailboxPasswordAvailable)
            assertTrue(result.twoFactorEnabled)
        }
    }

    @Test
    fun `get current settings 1 Pass handled correctly`() = coroutinesTest {
        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(
            testUserSettingsResponse.copy(
                twoFA = TwoFASetting(true, 1, null),
                password = PasswordSetting(mode = 1, expirationTime = null)
            )
        )
        viewModel.state.test {
            // WHEN
            viewModel.perform(ObserveState(testUserId))
            // THEN
            assertIs<State.Idle>(awaitItem())
            val result = awaitItem()
            assertTrue(result is State.ChangePassword)
            assertFalse(result.mailboxPasswordAvailable)
            assertTrue(result.twoFactorEnabled)
        }
    }

    @Test
    fun `update login password handled correctly`() = coroutinesTest {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-password") } returns testNewPassword
        every { keyStoreCrypto.encrypt(testNewPassword) } returns "encrypted-test-new-password"

        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(
            testUserSettingsResponse.copy(
                twoFA = TwoFASetting(false, 1, null),
                password = PasswordSetting(mode = 1, expirationTime = null)
            )
        )

        coEvery { performUpdateMailboxPassword.invoke(any(), testUserId, any(), any(), any()) } returns true

        flowTest(viewModel.state) {
            // WHEN
            viewModel.perform(ObserveState(testUserId))
            assertIs<State.Idle>(awaitItem())
            assertIs<State.ChangePassword>(awaitItem())

            viewModel.perform(UpdatePassword(testUserId, Login, testPassword, testNewPassword))

            // THEN
            assertIs<State.UpdatingPassword>(awaitItem())
            val result = awaitItem()
            assertTrue(result is State.Success)

            coVerify(exactly = 1) {
                performUpdateMailboxPassword.invoke(
                    userId = testUserId,
                    loginPassword = "encrypted-test-password",
                    newPassword = "encrypted-test-new-password",
                    secondFactorCode = "",
                    twoPasswordMode = false
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

        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(
            testUserSettingsResponse.copy(
                twoFA = TwoFASetting(false, 1, null),
                password = PasswordSetting(mode = 2, expirationTime = null)
            )
        )

        coEvery { performUpdateMailboxPassword.invoke(any(), any(), any(), any(), any()) } returns true

        flowTest(viewModel.state) {
            // WHEN
            viewModel.perform(ObserveState(testUserId))
            assertIs<State.Idle>(awaitItem())
            assertIs<State.ChangePassword>(awaitItem())

            viewModel.perform(UpdatePassword(testUserId, Mailbox, testLoginPassword, testNewMailboxPassword))

            // THEN
            assertIs<State.UpdatingPassword>(awaitItem())
            val result = awaitItem()
            assertTrue(result is State.Success)

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

        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(
            testUserSettingsResponse.copy(
                twoFA = TwoFASetting(false, 0, null),
                password = PasswordSetting(mode = 1, expirationTime = null)
            )
        )

        coEvery { performUpdateMailboxPassword.invoke(any(), any(), any(), any(), any()) } returns true

        flowTest(viewModel.state) {
            // WHEN
            viewModel.perform(ObserveState(testUserId))
            assertIs<State.Idle>(awaitItem())
            assertIs<State.ChangePassword>(awaitItem())

            viewModel.perform(UpdatePassword(testUserId, Both, testLoginPassword, testNewMailboxPassword))

            // THEN
            assertIs<State.UpdatingPassword>(awaitItem())
            val result = awaitItem()
            assertTrue(result is State.Success)

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

    @Test
    fun `update password with two factor handled correctly`() = coroutinesTest {
        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"
        every { keyStoreCrypto.decrypt("encrypted-test-new-password") } returns testNewPassword
        every { keyStoreCrypto.encrypt(testNewPassword) } returns "encrypted-test-new-password"

        val testLoginPassword = testPassword
        val testNewMailboxPassword = testNewPassword

        coEvery { observeUserSettings.invoke(testUserId) } returns flowOf(
            testUserSettingsResponse.copy(
                twoFA = TwoFASetting(true, 0, null),
                password = PasswordSetting(mode = 1, expirationTime = null)
            )
        )

        flowTest(viewModel.state) {
            // WHEN
            viewModel.perform(ObserveState(testUserId))
            assertIs<State.Idle>(awaitItem())
            assertIs<State.ChangePassword>(awaitItem())

            viewModel.perform(UpdatePassword(testUserId, Mailbox, testLoginPassword, testNewMailboxPassword))

            // THEN
            assertIs<State.TwoFactorNeeded>(awaitItem())
        }
    }

    @Test
    fun `recovery password reset available`() = coroutinesTest {
        // GIVEN
        coEvery { isAccountRecoveryResetEnabled.invoke(testUserId) } returns true
        coEvery { isSessionAccountRecoveryEnabled.invoke(testUserId) } returns true

        viewModel.state.test {
            // WHEN
            viewModel.perform(ObserveState(testUserId))

            // THEN
            assertIs<State.Idle>(awaitItem())
            val result = assertIs<State.ChangePassword>(awaitItem())
            assertTrue(result.recoveryResetAvailable)
            coVerify { isSessionAccountRecoveryEnabled(testUserId, false) }
        }
    }
}
