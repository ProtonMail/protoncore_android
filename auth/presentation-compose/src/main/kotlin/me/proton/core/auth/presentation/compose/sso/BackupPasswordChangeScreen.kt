/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.presentation.compose.sso

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography

@Composable
public fun BackupPasswordChangeScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onCloseMessage: (String?) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: BackupPasswordChangeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackupPasswordChangeScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onContinueClicked = { viewModel.submit(it) },
        onCloseMessage = onCloseMessage,
        onErrorMessage = onErrorMessage,
        onSuccess = onSuccess,
        state = state
    )
}

@Composable
public fun BackupPasswordChangeScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: (BackupPasswordChangeAction.ChangePassword) -> Unit = {},
    onCloseMessage: (String?) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    state: BackupPasswordChangeState,
) {
    LaunchedEffect(state) {
        when (state) {
            is BackupPasswordChangeState.Close -> onCloseMessage(state.message)
            is BackupPasswordChangeState.Error -> onErrorMessage(state.message)
            is BackupPasswordChangeState.Success -> onSuccess()
            else -> Unit
        }
    }
    BackupPasswordChangeScaffold(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onContinueClicked = onContinueClicked,
        isPasswordTooShort = state.isPasswordTooShort(),
        arePasswordsNotMatching = state.arePasswordsNotMatching(),
        isLoading = state is BackupPasswordChangeState.Loading
    )
}

@Composable
public fun BackupPasswordChangeScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: (BackupPasswordChangeAction.ChangePassword) -> Unit = {},
    isPasswordTooShort: Boolean = false,
    arePasswordsNotMatching: Boolean = false,
    isLoading: Boolean = false,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onCloseClicked) {
                        Icon(
                            painterResource(id = R.drawable.ic_proton_close),
                            contentDescription = stringResource(id = R.string.presentation_close)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(ProtonDimens.DefaultSpacing),
            ) {

                val errorTooShort = stringResource(R.string.backup_password_setup_password_too_short)
                val errorNotMatch = stringResource(R.string.backup_password_setup_password_not_matching)

                BackupPasswordChangeForm(
                    backupPasswordError = errorTooShort.takeIf { isPasswordTooShort },
                    backupPasswordRepeatedError = errorNotMatch.takeIf { arePasswordsNotMatching },
                    onContinueClicked = onContinueClicked,
                    isLoading = isLoading,
                )
            }
        }
    }
}

@Composable
private fun BackupPasswordChangeForm(
    backupPasswordError: String?,
    backupPasswordRepeatedError: String?,
    isLoading: Boolean,
    onContinueClicked: (BackupPasswordChangeAction.ChangePassword) -> Unit,
    modifier: Modifier = Modifier,
) {
    var backupPassword by rememberSaveable { mutableStateOf("") }
    var repeatBackupPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.backup_password_change_title),
            style = ProtonTypography.Default.headline
        )
        Text(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(id = R.string.backup_password_change_description),
            style = ProtonTypography.Default.body2Regular
        )
        ProtonPasswordOutlinedTextFieldWithError(
            text = backupPassword,
            onValueChanged = { backupPassword = it },
            enabled = !isLoading,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.backup_password_setup_password_label)) },
            errorText = backupPasswordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
        )
        ProtonPasswordOutlinedTextFieldWithError(
            text = repeatBackupPassword,
            onValueChanged = { repeatBackupPassword = it },
            enabled = !isLoading,
            singleLine = true,
            label = { Text(text = stringResource(id = R.string.backup_password_setup_repeat_password_label)) },
            errorText = backupPasswordRepeatedError,
            modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
        )
        ProtonSolidButton(
            contained = false,
            loading = isLoading,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
            onClick = { onContinueClicked(BackupPasswordChangeAction.ChangePassword(backupPassword, repeatBackupPassword)) }
        ) {
            Text(text = stringResource(id = R.string.backup_password_setup_continue_action))
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(device = Devices.TABLET)
@Composable
private fun BackupPasswordChangeScreenPreview() {
    ProtonTheme {
        BackupPasswordChangeScreen(state = BackupPasswordChangeState.Idle)
    }
}
