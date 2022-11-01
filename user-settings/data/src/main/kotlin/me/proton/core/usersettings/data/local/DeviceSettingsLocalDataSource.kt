package me.proton.core.usersettings.data.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.usersettings.domain.entity.DeviceSettings

interface DeviceSettingsLocalDataSource {
    fun observe(): Flow<DeviceSettings>
    suspend fun updateIsTelemetryEnabled(isEnabled: Boolean)
    suspend fun updateIsCrashReportEnabled(isEnabled: Boolean)
}
