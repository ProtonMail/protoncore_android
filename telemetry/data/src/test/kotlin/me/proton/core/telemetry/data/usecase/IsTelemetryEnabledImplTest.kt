/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.telemetry.data.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.usecase.IsTelemetryEnabled
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsTelemetryEnabledImplTest {

    // region mocks
    private val getUserSettings = mockk<GetUserSettings>(relaxed = true)
    private lateinit var isTelemetryEnabled: IsTelemetryEnabled
    // endregion

    private val userId = UserId("user-id")

    @Before
    fun beforeEveryTest() {
        // GIVEN
        isTelemetryEnabled = IsTelemetryEnabledImpl(getUserSettings)
    }

    @Test
    fun `no user returns true`() = runTest {
        // WHEN
        val result = isTelemetryEnabled(null)
        // THEN
        assertTrue(result)
    }

    @Test
    fun `user with telemetry enabled returns true`() = runTest {
        // GIVEN
        val userSettings = mockk<UserSettings>()
        coEvery { getUserSettings(userId, false) } returns userSettings
        every { userSettings.telemetry } returns true
        // WHEN
        val result = isTelemetryEnabled(userId)
        // THEN
        assertTrue(result)
    }

    @Test
    fun `user with telemetry disabled returns false`() = runTest {
        // GIVEN
        val userSettings = mockk<UserSettings>()
        coEvery { getUserSettings(userId, false) } returns userSettings
        every { userSettings.telemetry } returns false
        // WHEN
        val result = isTelemetryEnabled(userId)
        // THEN
        assertFalse(result)
    }

    @Test
    fun `user with telemetry is not set returns true`() = runTest {
        // GIVEN
        val userSettings = mockk<UserSettings>()
        coEvery { getUserSettings(userId, false) } returns userSettings
        every { userSettings.telemetry } returns null
        // WHEN
        val result = isTelemetryEnabled(userId)
        // THEN
        assertTrue(result)
    }

    @Test
    fun `user without settings returns true`() = runTest {
        // GIVEN
        coEvery { getUserSettings(userId, false) } throws Throwable()
        // WHEN
        val result = isTelemetryEnabled(userId)
        // THEN
        assertTrue(result)
    }
}
