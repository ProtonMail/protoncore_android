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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.arch.mapSuccessValueOrNull
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateCrashReports
import me.proton.core.usersettings.domain.usecase.PerformUpdateTelemetry
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    accountManager: AccountManager,
    observeUserSettings: ObserveUserSettings,
    private val performUpdateCrashReports: PerformUpdateCrashReports,
    private val performUpdateTelemetry: PerformUpdateTelemetry,
) : ViewModel() {

    private val primaryUserId = accountManager.getPrimaryUserId().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis),
        initialValue = null
    )

    val initialState = State()

    val state: StateFlow<State> = primaryUserId
        .filterNotNull()
        .flatMapLatest { observeUserSettings(it) }
        .filterNotNull()
        .map { userSettings ->
            State(
                telemetry = userSettings.telemetry ?: true,
                crashReports = userSettings.crashReports ?: true,
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
        state.value.telemetry.let { isEnabled ->
            performUpdateTelemetry(requireNotNull(primaryUserId.value), !isEnabled)
        }
    }

    private fun onToggleCrashReport() = viewModelScope.launch {
        state.value.crashReports.let { isEnabled ->
            performUpdateCrashReports(requireNotNull(primaryUserId.value), !isEnabled)
        }
    }

    data class State(
        val telemetry: Boolean = true,
        val crashReports: Boolean = true,
    )

    sealed interface Action {
        object ToggleTelemetry : Action
        object ToggleCrashReport : Action
    }
}
