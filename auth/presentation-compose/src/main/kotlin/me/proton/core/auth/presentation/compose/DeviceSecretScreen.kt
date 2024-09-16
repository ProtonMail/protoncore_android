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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Close
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.DeviceRejected
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Error
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.FirstLogin
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.InvalidSecret
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Loading
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.SetBackupPasswordNeeded
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.Success
import me.proton.core.auth.presentation.compose.confirmationcode.ShareConfirmationCodeWithAdminScreen
import me.proton.core.auth.presentation.compose.confirmationcode.SignInSentForApprovalScreen
import me.proton.core.auth.presentation.compose.sso.backuppassword.input.BackupPasswordInputScreen
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupScreen
import me.proton.core.compose.component.ProtonCenteredProgress
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
    onCloseClicked: () -> Unit = {},
    onNavigateToAskAdminHelp: () -> Unit = {},
    onNavigateToEnterBackupPassword: () -> Unit = {},
    state: DeviceSecretViewState
) {
    LaunchedEffect(state) {
        when (state) {
            is Close -> onClose()
            is Error -> onError(state.message)
            else -> Unit
        }
    }

    when (state) {
        is Close -> Unit
        is Error -> Unit

        is Loading -> ProtonCenteredProgress()

        is FirstLogin -> BackupPasswordSetupScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onError = onError,
            onSuccess = { Unit }
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

        is SetBackupPasswordNeeded -> BackupPasswordSetupScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onError = onError,
            onSuccess = { Unit }
        )

        is DeviceRejected -> TODO()
        is Success -> onSuccess(state.userId)
    }
}
