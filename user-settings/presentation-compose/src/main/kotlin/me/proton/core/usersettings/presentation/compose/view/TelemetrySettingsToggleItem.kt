package me.proton.core.usersettings.presentation.compose.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.core.compose.component.ProtonSettingsToggleItem
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel
import me.proton.core.usersettings.presentation.compose.viewmodel.DeviceSettingsViewModel.Action

@Composable
fun TelemetrySettingsToggleItem(
    viewModel: DeviceSettingsViewModel = hiltViewModel(),
) {
    val state by rememberAsState(viewModel.state, viewModel.initialState)

    TelemetrySettingsToggleItem(
        isEnabled = state.isTelemetryEnabled,
        onToggle = { viewModel.perform(Action.ToggleTelemetry) }
    )
}

@Composable
fun TelemetrySettingsToggleItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ProtonSettingsToggleItem(
        name = stringResource(id = R.string.device_settings_telemetry_title),
        hint = stringResource(id = R.string.device_settings_telemetry_hint),
        value = isEnabled,
        onToggle = { onToggle(!it) }
    )
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TelemetrySettingsToggleItemPreview() {
    ProtonTheme {
        TelemetrySettingsToggleItem(
            isEnabled = true,
            onToggle = {}
        )
    }
}
