package me.proton.core.usersettings.presentation.compose.view

import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel

data class TelemetrySettingState(
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true
)

fun DeviceSettingsViewModel.State.toTelemetrySettingState() = TelemetrySettingState(
    isVisible = isSettingsVisible,
    isEnabled = isTelemetryEnabled
)
