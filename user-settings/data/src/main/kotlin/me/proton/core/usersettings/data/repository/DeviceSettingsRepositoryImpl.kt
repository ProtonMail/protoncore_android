/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.usersettings.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import me.proton.core.usersettings.data.local.DeviceSettingsLocalDataSource
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import javax.inject.Inject

class DeviceSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: DeviceSettingsLocalDataSource,
) : DeviceSettingsRepository {

    override fun observeDeviceSettings(): Flow<DeviceSettings> =
        localDataSource.observe()

    override suspend fun getDeviceSettings(): DeviceSettings =
        localDataSource.observe().first()

    override suspend fun updateIsTelemetryEnabled(isEnabled: Boolean) =
        localDataSource.updateIsTelemetryEnabled(isEnabled)

    override suspend fun updateIsCrashReportEnabled(isEnabled: Boolean) =
        localDataSource.updateIsCrashReportEnabled(isEnabled)
}
