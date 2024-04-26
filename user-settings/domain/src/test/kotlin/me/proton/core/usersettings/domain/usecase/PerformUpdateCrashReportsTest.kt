/*
 * Copyright (c) 2023 Proton Technologies AG
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
import me.proton.core.usersettings.domain.entity.PasswordSetting
import me.proton.core.usersettings.domain.entity.RecoverySetting
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.repository.UserSettingsRepository
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PerformUpdateCrashReportsTest {
    // region mocks
    private val repository = mockk<UserSettingsRepository>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")

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
    // endregion

    private lateinit var useCase: PerformUpdateCrashReports

    @Before
    fun beforeEveryTest() {
        useCase = PerformUpdateCrashReports(
            repository = repository
        )
    }

    @Test
    fun `update crash reports returns success`() = runTest {
        // GIVEN
        coEvery {
            repository.updateCrashReports(
                userId = testUserId,
                isEnabled = true
            )
        } returns testUserSettingsResponse

        // WHEN
        val result = useCase(testUserId, true)
        // THEN
        assertEquals(testUserSettingsResponse, result)
    }
}
