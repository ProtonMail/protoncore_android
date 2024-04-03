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

package me.proton.core.userrecovery.presentation.compose.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.userrecovery.presentation.R
import me.proton.core.userrecovery.presentation.compose.viewmodel.DeviceRecoveryDialogViewModel

@Composable
fun DeviceRecoveryDialog(
    modifier: Modifier = Modifier,
    viewModel: DeviceRecoveryDialogViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DeviceRecoveryDialog(
        modifier = modifier,
        state = state,
        onDismiss = { onDismiss() }
    )
}

@Composable
fun DeviceRecoveryDialog(
    modifier: Modifier = Modifier,
    state: DeviceRecoveryDialogViewModel.State,
    onDismiss: () -> Unit = {},
    onError: (Throwable?) -> Unit = {},
) {
    when (state) {
        is DeviceRecoveryDialogViewModel.State.Error -> onError(state.throwable)
        is DeviceRecoveryDialogViewModel.State.Loading -> DeviceRecoveryDialog(
            modifier = modifier,
            title = stringResource(id = R.string.user_recovery_dialog_title),
            text = stringResource(id = R.string.user_recovery_dialog_text, "..."),
            onDismiss = onDismiss
        )
        is DeviceRecoveryDialogViewModel.State.Idle -> DeviceRecoveryDialog(
            modifier = modifier,
            title = stringResource(id = R.string.user_recovery_dialog_title),
            text = stringResource(id = R.string.user_recovery_dialog_text, state.email),
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun DeviceRecoveryDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onDismiss: () -> Unit = { },
) {
    ProtonAlertDialog(
        modifier = modifier,
        title = title,
        text = { ProtonAlertDialogText(text = text) },
        onDismissRequest = { onDismiss() },
        confirmButton = { ProtonAlertDialogButton(titleResId = R.string.user_recovery_dialog_dismiss) { onDismiss() } },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DeviceRecoveryDialogPreview() {
    ProtonTheme {
        DeviceRecoveryDialog(
            state = DeviceRecoveryDialogViewModel.State.Idle(email = "example@proton.me"),
            onDismiss = { }
        )
    }
}
