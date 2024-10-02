/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.auth.presentation.compose

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Close
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.DeviceRejected
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Error
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.FirstLogin
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.InvalidSecret
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Loading
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Success
import me.proton.core.auth.presentation.compose.confirmationcode.ShareConfirmationCodeWithAdminScreen
import me.proton.core.auth.presentation.compose.confirmationcode.SignInSentForApprovalScreen
import me.proton.core.auth.presentation.compose.sso.backuppassword.input.BackupPasswordInputScreen
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupScreen
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonErrorMessageWithAction
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId

@Composable
public fun DeviceSecretScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: (userId: UserId) -> Unit = {},
    onNavigateToEnterBackupPassword: () -> Unit = {},
    onNavigateToAskAdminHelp: () -> Unit = {},
    viewModel: DeviceSecretViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DeviceSecretScreen(
        modifier = modifier,
        onClose = onClose,
        onError = onError,
        onSuccess = onSuccess,
        onLoad = { viewModel.submit(DeviceSecretAction.Load()) },
        onCloseClicked = { viewModel.submit(DeviceSecretAction.Close) },
        onNavigateToAskAdminHelp = onNavigateToAskAdminHelp,
        onNavigateToEnterBackupPassword = onNavigateToEnterBackupPassword,
        state = state
    )
}

@Composable
public fun DeviceSecretScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onSuccess: (userId: UserId) -> Unit = {},
    onLoad: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onNavigateToAskAdminHelp: () -> Unit = {},
    onNavigateToEnterBackupPassword: () -> Unit = {},
    state: DeviceSecretViewState
) {
    when (state) {
        is Close -> onClose()
        is Success -> onSuccess(state.userId)
        // TODO: Replace ProtonCenteredProgress by a new screen.
        is Loading -> ProtonCenteredProgress()
        // TODO: Replace ProtonErrorMessageWithAction by a new screen.
        is Error -> ProtonErrorMessageWithAction(
            errorMessage = state.message ?: "Unknown",
            action = stringResource(R.string.presentation_retry),
            onAction = { onLoad() },
            elevation = 0.dp
        )

        is FirstLogin -> BackupPasswordSetupScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onError = onError,
            onSuccess = { onLoad() }
        )

        is InvalidSecret.NoDevice.EnterBackupPassword -> BackupPasswordInputScreen(
            modifier = modifier,
            onAskAdminHelpClicked = onNavigateToAskAdminHelp,
            onCloseClicked = onCloseClicked,
            onError = onError,
            onSuccess = { Unit }
        )

        is InvalidSecret.NoDevice.WaitingAdmin -> ShareConfirmationCodeWithAdminScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onError = onError
        )

        is InvalidSecret.OtherDevice.WaitingMember -> SignInSentForApprovalScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onErrorMessage = onError,
            onEnterBackupPasswordClicked = onNavigateToEnterBackupPassword,
            onAskAdminHelpClicked = onNavigateToAskAdminHelp
        )

        is DeviceRejected -> onClose()
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
private fun DeviceSecretScreenErrorPreview() {
    ProtonTheme {
        DeviceSecretScreen(
            state = Error("An error occurs. Please try again.")
        )
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
private fun DeviceSecretScreenLoadingPreview() {
    ProtonTheme {
        DeviceSecretScreen(
            state = Loading
        )
    }
}
