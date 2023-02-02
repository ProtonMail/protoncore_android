/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.observability.data

import android.content.Context
import android.content.res.Resources
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsObservabilityEnabledImplTest {

    // region mocks
    private val resources = mockk<Resources>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private val deviceSettingsRepository = mockk<DeviceSettingsRepository>(relaxed = true)
    private val deviceSettings = mockk<DeviceSettings>(relaxed = true)
    private lateinit var repository: IsObservabilityEnabled
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        coEvery { deviceSettingsRepository.getDeviceSettings() } returns deviceSettings
        every { context.resources } returns resources
        repository = IsObservabilityEnabledImpl(context, deviceSettingsRepository)
    }

    @Test
    fun `telemetry enabled observability enabled returns true`() = runTest {
        // GIVEN
        every { deviceSettings.isTelemetryEnabled } returns true
        every { resources.getBoolean(R.bool.observability_enabled) } returns true
        // WHEN
        val result = repository.invoke()
        // THEN
        assertTrue(result)
    }

    @Test
    fun `telemetry enabled observability disabled returns false`() = runTest {
        // GIVEN
        every { deviceSettings.isTelemetryEnabled } returns true
        every { resources.getBoolean(R.bool.observability_enabled) } returns false
        // WHEN
        val result = repository.invoke()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `telemetry disabled observability enabled returns false`() = runTest {
        // GIVEN
        every { deviceSettings.isTelemetryEnabled } returns false
        every { resources.getBoolean(R.bool.observability_enabled) } returns true
        // WHEN
        val result = repository.invoke()
        // THEN
        assertFalse(result)
    }

    @Test
    fun `telemetry disabled observability disabled returns false`() = runTest {
        // GIVEN
        every { resources.getBoolean(R.bool.observability_enabled) } returns false
        every { deviceSettings.isTelemetryEnabled } returns false
        // WHEN
        val result = repository.invoke()
        // THEN
        assertFalse(result)
    }
}