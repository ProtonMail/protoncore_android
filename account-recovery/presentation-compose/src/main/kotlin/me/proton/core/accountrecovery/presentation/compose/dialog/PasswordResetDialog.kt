package me.proton.core.accountrecovery.presentation.compose.dialog

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.viewmodel.PasswordResetDialogViewModel
import me.proton.core.accountrecovery.presentation.compose.viewmodel.PasswordResetDialogViewModel.Action
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHint
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.compose.viewmodel.hiltViewModelOrNull

@Composable
fun PasswordResetDialog(
    modifier: Modifier = Modifier,
    onRecoveryMethod: () -> Unit = { },
    onDismiss: () -> Unit = { },
    onError: (Throwable) -> Unit = { },
    onSuccess: () -> Unit = { },
    viewModel: PasswordResetDialogViewModel? = hiltViewModelOrNull()
) {
    val state = when (viewModel) {
        null -> PasswordResetDialogViewModel.State.Ready("example@domain.com")
        else -> viewModel.state.collectAsStateWithLifecycle().value
    }

    PasswordResetDialog(
        modifier = modifier,
        onRequestReset = { viewModel?.perform(Action.RequestReset) },
        onRecoveryMethod = onRecoveryMethod,
        onDismiss = onDismiss,
        onError = onError,
        onSuccess = onSuccess,
        state = state
    )
}

@Composable
fun PasswordResetDialog(
    modifier: Modifier = Modifier,
    onRequestReset: () -> Unit = { },
    onRecoveryMethod: () -> Unit = { },
    onDismiss: () -> Unit = { },
    onError: (Throwable) -> Unit = { },
    onSuccess: () -> Unit = { },
    state: PasswordResetDialogViewModel.State
) {
    val (email, isLoading) = remember(state) {
        when (state) {
            is PasswordResetDialogViewModel.State.Loading -> state.email to true
            is PasswordResetDialogViewModel.State.Ready -> state.email to false
            else -> null to false
        }
    }

    when (state) {
        is PasswordResetDialogViewModel.State.Error -> onError(state.throwable)
        is PasswordResetDialogViewModel.State.Loading,
        is PasswordResetDialogViewModel.State.Ready -> PasswordResetDialog(
            modifier = modifier,
            email = email ?: "...",
            onRequestReset = onRequestReset,
            onRecoveryMethod = onRecoveryMethod,
            onDismiss = onDismiss,
            isRequestResetLoading = isLoading
        )
        is PasswordResetDialogViewModel.State.ResetRequested -> onSuccess()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PasswordResetDialog(
    modifier: Modifier = Modifier,
    email: String,
    onRequestReset: () -> Unit = { },
    onRecoveryMethod: () -> Unit = { },
    onDismiss: () -> Unit = { },
    isRequestResetLoading: Boolean = false
) {
    ProtonAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.account_recovery_reset_dialog_title),
        text = {
            Column {
                Spacer(modifier = Modifier.size(ProtonDimens.DefaultSpacing))
                ProtonAlertDialogText(
                    stringResource(R.string.account_recovery_reset_dialog_text, email)
                )
                Spacer(modifier = Modifier.size(ProtonDimens.LargeSpacing))
                Card(
                    onClick = onRecoveryMethod,
                    backgroundColor = ProtonTheme.colors.backgroundSecondary,
                    shape = ProtonTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(ProtonDimens.SmallSpacing),
                    ) {
                        Row {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.account_recovery_reset_dialog_action_use_recovery),
                                style = ProtonTheme.typography.defaultStrongNorm
                            )
                            Icon(
                                modifier = Modifier.size(ProtonDimens.DefaultIconSize),
                                painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
                                contentDescription = "",
                                tint = ProtonTheme.colors.iconHint
                            )
                        }
                        Spacer(modifier = Modifier.size(ProtonDimens.SmallSpacing))
                        Text(
                            text = stringResource(R.string.account_recovery_reset_dialog_action_use_recovery_hint),
                            style = ProtonTheme.typography.defaultHint
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            Column {
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(R.string.account_recovery_reset_dialog_action_request_reset),
                    onClick = onRequestReset,
                    loading = isRequestResetLoading
                )
                ProtonAlertDialogButton(
                    modifier = Modifier.align(Alignment.End),
                    title = stringResource(id = R.string.account_recovery_dismiss),
                    onClick = onDismiss
                )
            }
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewPasswordResetDialog() {
    ProtonTheme {
        PasswordResetDialog(email = "example@domain.com")
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Composable
internal fun PreviewPasswordResetDialogLoading() {
    ProtonTheme {
        PasswordResetDialog(
            email = "example@domain.com",
            isRequestResetLoading = true
        )
    }
}
