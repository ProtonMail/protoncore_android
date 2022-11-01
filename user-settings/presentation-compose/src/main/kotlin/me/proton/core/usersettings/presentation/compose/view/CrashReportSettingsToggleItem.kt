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
fun CrashReportSettingsToggleItem(
    viewModel: DeviceSettingsViewModel = hiltViewModel(),
) {
    val state by rememberAsState(viewModel.state, viewModel.initialState)

    CrashReportSettingsToggleItem(
        isEnabled = state.isCrashReportEnabled,
        onToggle = { viewModel.perform(Action.ToggleCrashReport) }
    )
}

@Composable
fun CrashReportSettingsToggleItem(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ProtonSettingsToggleItem(
        name = stringResource(id = R.string.device_settings_crashreport_title),
        hint = stringResource(id = R.string.device_settings_crashreport_hint),
        value = isEnabled,
        onToggle = { onToggle(!it) }
    )
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CrashReportSettingsToggleItemPreview() {
    ProtonTheme {
        CrashReportSettingsToggleItem(
            isEnabled = true,
            onToggle = {}
        )
    }
}
