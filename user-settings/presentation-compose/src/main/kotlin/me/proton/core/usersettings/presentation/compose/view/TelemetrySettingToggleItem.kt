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

package me.proton.core.usersettings.presentation.compose.view

import android.content.res.Configuration
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.compose.viewmodel.UserSettingsViewModel
import me.proton.core.usersettings.presentation.compose.viewmodel.UserSettingsViewModel.Action
import me.proton.core.usersettings.presentation.compose.viewmodel.UserSettingsViewModel.State

@Composable
fun TelemetrySettingToggleItem(
    modifier: Modifier = Modifier,
    viewModel: UserSettingsViewModel? = hiltViewModelOrNull(),
    divider: @Composable () -> Unit = { Divider() },
) {
    val state = when (viewModel) {
        null -> State()
        else -> rememberAsState(viewModel.state, viewModel.initialState).value
    }
    TelemetrySettingToggleItem(
        modifier = modifier,
        isEnabled = state.telemetry,
        onToggle = { viewModel?.perform(Action.ToggleTelemetry) },
        divider = { divider() }
    )
}

@Composable
fun TelemetrySettingToggleItem(
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onToggle: (Boolean) -> Unit = {},
    divider: @Composable () -> Unit = { Divider() },
) {
    ProtonSettingsToggleItem(
        modifier = modifier,
        name = stringResource(id = R.string.device_settings_telemetry_title),
        hint = stringResource(id = R.string.device_settings_telemetry_hint),
        value = isEnabled,
        onToggle = { onToggle(!it) }
    )
    divider()
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TelemetrySettingToggleItemPreview() {
    ProtonTheme {
        TelemetrySettingToggleItem()
    }
}
