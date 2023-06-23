/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.presentation.compose.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryViewModel
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.utils.StringBox
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.util.kotlin.exhaustive
import kotlin.time.Duration.Companion.milliseconds

internal const val PASSWORD_FIELD_TAG = "PASSWORD_FIELD_TAG"

@Composable
fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryViewModel = hiltViewModel(),
    onClosed: () -> Unit,
    onError: (Throwable?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by rememberAsState(viewModel.state, viewModel.initialState)

    LaunchedEffect(Unit) {
        lifecycleOwner.launchOnScreenView {
            viewModel.screenId.collect { screenId ->
                screenId?.let { viewModel.onScreenView(it) }
            }
        }
    }

    LaunchedEffect(state) {
        when (val current = state) {
            is AccountRecoveryViewModel.State.Loading -> Unit
            is AccountRecoveryViewModel.State.Closed -> onClosed()
            is AccountRecoveryViewModel.State.Error -> onError(current.throwable)
            is AccountRecoveryViewModel.State.Opened -> Unit
        }
    }

    AccountRecoveryDialog(
        modifier = modifier,
        state = state,
        onGracePeriodCancel = { viewModel.startAccountRecoveryCancel(it) },
        onDismiss = { viewModel.userAcknowledged() }
    )
}

@Composable
fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    state: AccountRecoveryViewModel.State,
    onGracePeriodCancel: (String) -> Unit = { },
    onDismiss: () -> Unit = { }
) {
    when (state) {
        is AccountRecoveryViewModel.State.Error,
        is AccountRecoveryViewModel.State.Closed -> Unit

        is AccountRecoveryViewModel.State.Loading -> {
            AccountRecoveryDialog(
                modifier = modifier,
                onDismiss = onDismiss,
                isLoading = true
            )
        }

        is AccountRecoveryViewModel.State.Opened.GracePeriodStarted -> {
            val password = remember { mutableStateOf("") }
            AccountRecoveryGracePeriodDialog(
                modifier = modifier,
                isActionButtonLoading = state.processing,
                onGracePeriodCancel = { onGracePeriodCancel(password.value) },
                onDismiss = onDismiss,
                password = password,
                passwordError = state.passwordError
            )
        }

        is AccountRecoveryViewModel.State.Opened.CancellationHappened ->
            AccountRecoveryCancelledDialog(
                modifier,
                onDismiss = onDismiss
            )

        is AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted ->
            AccountRecoveryPasswordPeriodStartedDialog(
                modifier = modifier,
                onDismiss = onDismiss
            )

        is AccountRecoveryViewModel.State.Opened.RecoveryEnded ->
            AccountRecoveryWindowEndedDialog(
                modifier = modifier,
                onDismiss = onDismiss
            )
    }.exhaustive
}

// region all recovery dialog types
@Composable
internal fun AccountRecoveryGracePeriodDialog(
    modifier: Modifier = Modifier,
    isActionButtonLoading: Boolean = false,
    onGracePeriodCancel: () -> Unit,
    onDismiss: () -> Unit,
    password: MutableState<String>,
    passwordError: StringBox?
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_grace_started_title),
        isActionButtonLoading = isActionButtonLoading,
        subtitle = stringResource(id = R.string.account_recovery_grace_started_subtitle),
        actionText = stringResource(id = R.string.account_recovery_cancel),
        dismissText = stringResource(id = R.string.presentation_alert_ok),
        onAction = onGracePeriodCancel,
        onDismiss = onDismiss,
        password = password,
        passwordError = passwordError
    )
}

@Composable
internal fun AccountRecoveryCancelledDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_cancelled_title),
        subtitle = stringResource(id = R.string.account_recovery_cancelled_subtitle),
        dismissText = stringResource(id = R.string.presentation_alert_ok),
        onDismiss = onDismiss
    )
}

@Composable
internal fun AccountRecoveryPasswordPeriodStartedDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_password_started_title),
        subtitle = stringResource(id = R.string.account_recovery_password_started_subtitle),
        dismissText = stringResource(id = R.string.presentation_alert_ok),
        onDismiss = onDismiss
    )
}

@Composable
internal fun AccountRecoveryWindowEndedDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    AccountRecoveryDialog(
        modifier = modifier,
        title = stringResource(id = R.string.account_recovery_window_ended_title),
        subtitle = stringResource(id = R.string.account_recovery_window_ended_subtitle),
        dismissText = stringResource(id = R.string.presentation_alert_ok),
        onDismiss = onDismiss
    )
}
// endregion

@Composable
private fun AccountRecoveryDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    subtitle: String = "",
    isLoading: Boolean = false,
    isActionButtonLoading: Boolean = false,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = { },
    password: MutableState<String>? = null,
    passwordError: StringBox? = null
) {
    val show = remember { mutableStateOf(true) }

    ProtonAlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onDismiss()
            show.value = false
        },
        title = title,
        text = {
            if (isLoading)
                DeferredCircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "progressIndicator" },
                    defer = 0.milliseconds
                )
            else
                Column {
                    ProtonAlertDialogText(text = subtitle)
                    if (password != null) {
                        PasswordField(
                            password = password,
                            passwordError = passwordError,
                            enabled = !isActionButtonLoading
                        )
                    }
                }
        },
        confirmButton = {
            if (!actionText.isNullOrEmpty()) {
                ProtonAlertDialogButton(
                    onClick = {
                        if (onAction != null) {
                            onAction()
                        }
                    },
                    title = actionText,
                    loading = isActionButtonLoading,
                    enabled = true
                )
            }
        },
        dismissButton = {
            if (!dismissText.isNullOrEmpty()) {
                ProtonAlertDialogButton(
                    onClick = onDismiss,
                    title = dismissText
                )
            }
        }
    )
}

@Composable
private fun PasswordField(
    password: MutableState<String>,
    passwordError: StringBox?,
    enabled: Boolean
) {
    ProtonOutlinedTextFieldWithError(
        text = password.value,
        onValueChanged = { password.value = it },
        enabled = enabled,
        errorText = passwordError?.get(LocalContext.current),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        label = {
            Text(text = stringResource(id = R.string.account_recovery_password_label))
        },
        maxLines = 1,
        placeholder = {
            Text(text = stringResource(id = R.string.account_recovery_password_placeholder))
        },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DefaultSpacing)
            .testTag(PASSWORD_FIELD_TAG)
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountRecoveryAlertDialogGracePeriodPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewModel.State.Opened.GracePeriodStarted(),
            onGracePeriodCancel = { },
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountRecoveryAlertDialogGracePeriodProcessingPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewModel.State.Opened.GracePeriodStarted(processing = true),
            onGracePeriodCancel = { },
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountRecoveryAlertDialogChangePasswordPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewModel.State.Opened.PasswordChangePeriodStarted,
            onGracePeriodCancel = { },
            onDismiss = { }
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountRecoveryAlertDialogLoadingPreview() {
    ProtonTheme {
        AccountRecoveryDialog(
            state = AccountRecoveryViewModel.State.Loading,
            onGracePeriodCancel = { },
            onDismiss = { }
        )
    }
}
