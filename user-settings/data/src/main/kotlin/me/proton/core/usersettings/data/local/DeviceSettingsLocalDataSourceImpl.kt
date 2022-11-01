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

package me.proton.core.usersettings.data.local

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.entity.DeviceSettings.Companion.isCrashReportEnabledDefault
import me.proton.core.usersettings.domain.entity.DeviceSettings.Companion.isTelemetryEnabledDefault
import javax.inject.Inject

class DeviceSettingsLocalDataSourceImpl @Inject constructor(
    private val provider: LocalSettingsDataStoreProvider,
) : DeviceSettingsLocalDataSource {

    private val deviceSettingsDataStore = provider.deviceSettingsDataStore
    private val isTelemetryEnabledKey = booleanPreferencesKey("isTelemetryEnabledKey")
    private val isCrashReportEnabledKey = booleanPreferencesKey("isCrashReportEnabledKey")

    override fun observe(): Flow<DeviceSettings> = deviceSettingsDataStore.data.map { pref ->
        DeviceSettings(
            isTelemetryEnabled = pref[isTelemetryEnabledKey] ?: isTelemetryEnabledDefault,
            isCrashReportEnabled = pref[isCrashReportEnabledKey] ?: isCrashReportEnabledDefault,
        )
    }

    override suspend fun updateIsTelemetryEnabled(isEnabled: Boolean) {
        provider.deviceSettingsDataStore.edit { mutablePref ->
            mutablePref[isTelemetryEnabledKey] = isEnabled
        }
    }

    override suspend fun updateIsCrashReportEnabled(isEnabled: Boolean) {
        provider.deviceSettingsDataStore.edit { mutablePref ->
            mutablePref[isCrashReportEnabledKey] = isEnabled
        }
    }
}
