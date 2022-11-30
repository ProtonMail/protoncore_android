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

package me.proton.core.usersettings.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.usersettings.domain.entity.Flags
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GetUserSettingsTest {
    // region mocks
    private val repository = mockk<UserSettingsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val testUserSettingsResponse = UserSettings(
        userId = testUserId,
        email = RecoverySetting("test-email", 1, true, true),
        phone = null,
        twoFA = null,
        password = PasswordSetting(mode = 1, expirationTime = null),
        news = 0,
        locale = "en",
        logAuth = UserSettings.LogAuth.enumOf(1),
        density = UserSettings.Density.enumOf(1),
        invoiceText = "",
        dateFormat = UserSettings.DateFormat.enumOf(1),
        timeFormat = UserSettings.TimeFormat.enumOf(2),
        themeType = 1,
        weekStart = UserSettings.WeekStart.enumOf(7),
        welcome = true,
        earlyAccess = true,
        theme = "test-theme",
        flags = Flags(true)
    )
    // endregion
    private lateinit var useCase: GetUserSettings

    @Before
    fun beforeEveryTest() {
        useCase = GetUserSettings(repository)
    }

    @Test
    fun `get user settings returns success`() = runTest {
        // GIVEN
        coEvery { repository.getUserSettings(testUserId, any()) } returns testUserSettingsResponse
        // WHEN
        val result = useCase.invoke(testUserId, refresh = true)
        // THEN
        assertEquals(testUserSettingsResponse, result)
        val email = result.email
        assertNotNull(email)
        assertEquals("test-email", email.value)
    }

    @Test
    fun `get user settings returns error`() = runTest {
        // GIVEN
        coEvery { repository.getUserSettings(testUserId, any()) } throws ApiException(
            ApiResult.Error.Connection(
                false,
                RuntimeException("Test error")
            )
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(testUserId, refresh = true)
        }
        // THEN
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }
}
