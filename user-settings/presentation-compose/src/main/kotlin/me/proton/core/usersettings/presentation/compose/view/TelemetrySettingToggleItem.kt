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
import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel
import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel.Action
import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel.State

@Composable
fun TelemetrySettingToggleItem(
    modifier: Modifier = Modifier,
    viewModel: DeviceSettingsViewModel? = hiltViewModelOrNull(),
    divider: @Composable () -> Unit = { Divider() }
) {
    val state = when (viewModel) {
        null -> State(isSettingsVisible = true)
        else -> rememberAsState(viewModel.state, viewModel.initialState).value
    }
    TelemetrySettingToggleItem(
        modifier = modifier,
        state = state.toTelemetrySettingState(),
        onToggle = { viewModel?.perform(Action.ToggleTelemetry) },
        divider = { divider() }
    )
}

@Composable
fun TelemetrySettingToggleItem(
    modifier: Modifier = Modifier,
    state: TelemetrySettingState = TelemetrySettingState(),
    onToggle: (Boolean) -> Unit = {},
    divider: @Composable () -> Unit = { Divider() }
) {
    if (state.isVisible) {
        ProtonSettingsToggleItem(
            modifier = modifier,
            name = stringResource(id = R.string.device_settings_telemetry_title),
            hint = stringResource(id = R.string.device_settings_telemetry_hint),
            value = state.isEnabled,
            onToggle = { onToggle(!it) }
        )
        divider()
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TelemetrySettingToggleItemPreview() {
    ProtonTheme {
        TelemetrySettingToggleItem()
    }
}
