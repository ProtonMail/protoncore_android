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

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.test.android.ArchTest
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.test.kotlin.assertIs
import me.proton.core.test.kotlin.flowTest
import me.proton.core.user.domain.entity.User
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateRecoveryEmail
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateRecoveryEmailViewModelTest : ArchTest by ArchTest(), CoroutinesTest by CoroutinesTest() {
    // region mocks
    private val getUserSettingsUseCase = mockk<GetUserSettings>()
    private val performUpdateRecoveryEmailUseCase = mockk<PerformUpdateRecoveryEmail>()
    private val keyStoreCrypto = mockk<KeyStoreCrypto>()
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testUsername = "test-username"
    private val testPassword: EncryptedString = "test-password"

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
        keys = emptyList()
    )
    // endregion

    private lateinit var viewModel: UpdateRecoveryEmailViewModel

    @Before
    fun beforeEveryTest() {
        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse
        viewModel =
            UpdateRecoveryEmailViewModel(
                keyStoreCrypto,
                getUserSettingsUseCase,
                performUpdateRecoveryEmailUseCase
            )
    }

    @Test
    fun `get current recovery email empty handled correctly`() = coroutinesTest {
        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } returns testUserSettingsResponse.copy(
            email = null
        )
        flowTest(viewModel.state) {
            // WHEN
            viewModel.getCurrentRecoveryAddress(testUserId)
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.LoadingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.LoadingSuccess)
            assertNull(result.recoveryEmail)
        }
    }

    @Test
    fun `get current recovery email non-empty handled correctly`() = coroutinesTest {
        flowTest(viewModel.state) {
            // WHEN
            viewModel.getCurrentRecoveryAddress(testUserId)
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.LoadingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.LoadingSuccess)
            assertEquals("test-email", result.recoveryEmail)
        }
    }

    @Test
    fun `get current recovery email error handled correctly`() = coroutinesTest {
        coEvery { getUserSettingsUseCase.invoke(testUserId, any()) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )
        flowTest(viewModel.state) {
            // WHEN
            viewModel.getCurrentRecoveryAddress(testUserId)
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.LoadingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.Error)
            assertEquals("proton error", result.error.getUserMessage(mockk()))
        }
    }

    @Test
    fun `update recovery email set to empty`() = coroutinesTest {
        coEvery {
            performUpdateRecoveryEmailUseCase.invoke(
                sessionUserId = testUserId,
                newRecoveryEmail = "",
                password = "encrypted-test-password",
                secondFactorCode = ""
            )
        } returns testUserSettingsResponse.copy(email = RecoverySetting("", 1, notify = true, reset = true))

        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"

        flowTest(viewModel.state) {
            // WHEN
            viewModel.updateRecoveryEmail(testUserId, "", testPassword, "")
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.UpdatingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.UpdatingSuccess)
            assertEquals("", result.recoveryEmail)
        }
    }

    @Test
    fun `update recovery email set to non-empty`() = coroutinesTest {
        coEvery {
            performUpdateRecoveryEmailUseCase.invoke(
                sessionUserId = testUserId,
                newRecoveryEmail = "new-email",
                password = "encrypted-test-password",
                secondFactorCode = ""
            )
        } returns testUserSettingsResponse.copy(email = RecoverySetting("new-email", 1, notify = true, reset = true))

        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"

        flowTest(viewModel.state) {
            // WHEN
            viewModel.updateRecoveryEmail(testUserId, "new-email", testPassword, "")
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.UpdatingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.UpdatingSuccess)
            assertEquals("new-email", result.recoveryEmail)
        }
    }

    @Test
    fun `update recovery email set to non-empty with second factor`() = coroutinesTest {
        coEvery {
            performUpdateRecoveryEmailUseCase.invoke(
                sessionUserId = testUserId,
                newRecoveryEmail = "new-email",
                password = "encrypted-test-password",
                secondFactorCode = "123456"
            )
        } returns testUserSettingsResponse.copy(email = RecoverySetting("new-email", 1, notify = true, reset = true))

        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"

        flowTest(viewModel.state) {
            // WHEN
            viewModel.updateRecoveryEmail(testUserId, "new-email", testPassword, "123456")
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.UpdatingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.UpdatingSuccess)
            assertEquals("new-email", result.recoveryEmail)
        }
    }

    @Test
    fun `update recovery email error handled correctly`() = coroutinesTest {
        coEvery {
            performUpdateRecoveryEmailUseCase.invoke(
                sessionUserId = testUserId,
                newRecoveryEmail = "new-email",
                password = "encrypted-test-password",
                secondFactorCode = ""
            )
        } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = 1234,
                    error = "proton error"
                )
            )
        )

        every { keyStoreCrypto.decrypt("encrypted-test-password") } returns testPassword
        every { keyStoreCrypto.encrypt(testPassword) } returns "encrypted-test-password"

        flowTest(viewModel.state) {
            // WHEN
            viewModel.updateRecoveryEmail(testUserId, "new-email", testPassword, "")
            // THEN
            assertIs<UpdateRecoveryEmailViewModel.State.Idle>(awaitItem())
            assertIs<UpdateRecoveryEmailViewModel.State.UpdatingCurrent>(awaitItem())
            val result = awaitItem()
            assertTrue(result is UpdateRecoveryEmailViewModel.State.Error)
            assertEquals("proton error", result.error.getUserMessage(mockk()))
        }
    }
}
