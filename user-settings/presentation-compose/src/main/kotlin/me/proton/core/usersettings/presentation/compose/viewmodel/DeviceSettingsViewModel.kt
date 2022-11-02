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

package me.proton.core.usersettings.presentation.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.usersettings.domain.FeatureFlags
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.usecase.ObserveDeviceSettings
import me.proton.core.usersettings.domain.usecase.ObserveFeatureFlag
import me.proton.core.usersettings.domain.usecase.UpdateDeviceSettings
import javax.inject.Inject

@HiltViewModel
class DeviceSettingsViewModel @Inject constructor(
    observeDeviceSettings: ObserveDeviceSettings,
    observeFeatureFlag: ObserveFeatureFlag,
    private val updateDeviceSettings: UpdateDeviceSettings,
) : ViewModel() {

    val initialState = State()

    val state: StateFlow<State> = combine(
        observeFeatureFlag(FeatureFlags.ShowDataCollectSettings),
        observeDeviceSettings(),
    ) { featureFlag, deviceSettings ->
        State(
            isSettingsVisible = featureFlag.value,
            isTelemetryEnabled = deviceSettings.isTelemetryEnabled,
            isCrashReportEnabled = deviceSettings.isCrashReportEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = initialState
    )

    fun perform(action: Action) = when (action) {
        is Action.ToggleTelemetry -> onToggleTelemetry()
        is Action.ToggleCrashReport -> onToggleCrashReport()
    }

    private fun onToggleTelemetry() = viewModelScope.launch {
        updateDeviceSettings.updateIsTelemetryEnabled(!state.value.isTelemetryEnabled)
    }

    private fun onToggleCrashReport() = viewModelScope.launch {
        updateDeviceSettings.updateIsCrashReportEnabled(!state.value.isCrashReportEnabled)
    }

    data class State(
        val isSettingsVisible: Boolean = FeatureFlags.ShowDataCollectSettings.default,
        val isTelemetryEnabled: Boolean = DeviceSettings.isTelemetryEnabledDefault,
        val isCrashReportEnabled: Boolean = DeviceSettings.isCrashReportEnabledDefault,
    )

    sealed interface Action {
        object ToggleTelemetry : Action
        object ToggleCrashReport : Action
    }
}
