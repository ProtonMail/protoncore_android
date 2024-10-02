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

package me.proton.core.auth.presentation.compose.sso

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputState.FormError
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme

@Composable
public fun BackupPasswordInputScreen(
    onRequestAdminHelpClicked: () -> Unit,
    onCloseClicked: () -> Unit,
    onErrorMessage: (String?) -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BackupPasswordInputViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackupPasswordInputScreen(
        state = state,
        modifier = modifier,
        onAskAdminForHelp = onRequestAdminHelpClicked,
        onClose = onCloseClicked,
        onError = onErrorMessage,
        onPasswordSubmitted = { viewModel.submit(it) },
        onSuccess = onSuccess,
    )
}

@Composable
public fun BackupPasswordInputScreen(
    modifier: Modifier = Modifier,
    onAskAdminForHelp: () -> Unit = {},
    onClose: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onPasswordSubmitted: (BackupPasswordInputAction.Submit) -> Unit = {},
    onSuccess: () -> Unit = {},
    state: BackupPasswordInputState = BackupPasswordInputState.Idle,
) {
    LaunchedEffect(state) {
        when (state) {
            is BackupPasswordInputState.Error -> onError(state.message)
            is BackupPasswordInputState.Success -> onSuccess()
            else -> Unit
        }
    }

    val isLoading = state is BackupPasswordInputState.Loading
    var password by remember { mutableStateOf("") }
    val passwordError = (state as? FormError)?.message

    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            painterResource(id = R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(ProtonDimens.DefaultSpacing),
            ) {
                Text(
                    style = LocalTypography.current.headline,
                    text = stringResource(id = R.string.backup_password_input_title)
                )
                Text(
                    style = LocalTypography.current.body2Regular,
                    text = stringResource(id = R.string.backup_password_input_subtitle),
                    modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
                )
                ProtonPasswordOutlinedTextFieldWithError(
                    text = password,
                    onValueChanged = { password = it },
                    enabled = !isLoading,
                    singleLine = true,
                    label = { Text(text = stringResource(id = R.string.backup_password_input_label)) },
                    errorText = passwordError?.let { stringResource(it) },
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )
                ProtonSolidButton(
                    contained = false,
                    loading = isLoading,
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight),
                    onClick = { onPasswordSubmitted(BackupPasswordInputAction.Submit(password)) }
                ) {
                    Text(
                        text = stringResource(id = R.string.backup_password_input_action_continue)
                    )
                }
                ProtonTextButton(
                    contained = false,
                    enabled = !isLoading,
                    modifier = Modifier
                        .padding(top = ProtonDimens.SmallSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight),
                    onClick = onAskAdminForHelp
                ) {
                    Text(
                        text = stringResource(id = R.string.backup_password_input_action_help)
                    )
                }
            }
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun BackupPasswordInputScreenPreview() {
    ProtonTheme {
        BackupPasswordInputScreen()
    }
}
