package me.proton.core.usersettings.presentation.compose.view

import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel

data class CrashReportSettingState(
    val isVisible: Boolean = true,
    val isEnabled: Boolean = true
)

fun DeviceSettingsViewModel.State.toCrashReportSettingState() = CrashReportSettingState(
    isVisible = isSettingsVisible,
    isEnabled = isCrashReportEnabled
)
