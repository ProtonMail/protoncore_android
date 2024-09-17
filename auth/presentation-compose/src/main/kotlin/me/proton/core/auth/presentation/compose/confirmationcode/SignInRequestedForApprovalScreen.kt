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

package me.proton.core.auth.presentation.compose.confirmationcode

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
public fun SignInRequestedForApprovalScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    viewModel: SignInRequestedForApprovalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SignInRequestedForApprovalScreen(
        modifier = modifier,
        onClose = onClose,
        onCloseClicked = { viewModel.submit(SignInRequestedForApprovalAction.Close) },
        onErrorMessage = onErrorMessage,
        onConfirmClicked = { viewModel.submit(it) },
        onRejectClicked = { viewModel.submit(it) },
        onConfirmationCodeInputChange = { viewModel.submit(it) },
        state = state
    )
}

@Composable
public fun SignInRequestedForApprovalScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onConfirmClicked: (SignInRequestedForApprovalAction.Confirm) -> Unit = {},
    onRejectClicked: (SignInRequestedForApprovalAction.Reject) -> Unit = {},
    onConfirmationCodeInputChange: (SignInRequestedForApprovalAction.ValidateConfirmationCode) -> Unit = {},
    state: SignInRequestedForApprovalState
) {
    LaunchedEffect(state) {
        when (state) {
            is SignInRequestedForApprovalState.Close -> onClose()
            is SignInRequestedForApprovalState.ConfirmationCodeResult -> Unit
            is SignInRequestedForApprovalState.ConfirmedSuccessfully -> onClose()
            is SignInRequestedForApprovalState.Error -> onErrorMessage(state.message)
            is SignInRequestedForApprovalState.Idle -> Unit
            is SignInRequestedForApprovalState.Loading -> Unit
            is SignInRequestedForApprovalState.RejectedSuccessfully -> onClose()
        }
    }

    val isCodeVerified = (state as? SignInRequestedForApprovalState.ConfirmationCodeResult)?.success ?: false

    SignInRequestedForApprovalScaffold(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onConfirmClicked = onConfirmClicked,
        onConfirmationCodeInputChange = onConfirmationCodeInputChange,
        onRejectClicked = onRejectClicked,
        confirmationButtonClickable = isCodeVerified,
        isLoading = state is SignInRequestedForApprovalState.Loading
    )
}

@Composable
public fun SignInRequestedForApprovalScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit,
    onConfirmClicked: (SignInRequestedForApprovalAction.Confirm) -> Unit,
    onRejectClicked: (SignInRequestedForApprovalAction.Reject) -> Unit,
    onConfirmationCodeInputChange: (SignInRequestedForApprovalAction.ValidateConfirmationCode) -> Unit,
    confirmationButtonClickable: Boolean,
    isLoading: Boolean
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
                            contentDescription = stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ConfirmationCodeInputScreen(
                onConfirmClicked = onConfirmClicked,
                onRejectClicked = onRejectClicked,
                onConfirmationCodeInputChange = onConfirmationCodeInputChange,
                confirmationButtonClickable = confirmationButtonClickable,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun ConfirmationCodeInputScreen(
    onConfirmClicked: (SignInRequestedForApprovalAction.Confirm) -> Unit,
    onRejectClicked: (SignInRequestedForApprovalAction.Reject) -> Unit,
    onConfirmationCodeInputChange: (SignInRequestedForApprovalAction.ValidateConfirmationCode) -> Unit,
    confirmationButtonClickable: Boolean = false,
    isLoading: Boolean = false
) {
    var confirmationCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = stringResource(id = R.string.auth_login_signin_requested),
            style = ProtonTypography.Default.headline
        )

        Text(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(id = R.string.auth_login_signin_requested_subtitle),
            style = ProtonTypography.Default.defaultSmallWeak
        )

        ProtonOutlinedTextFieldWithError(
            text = confirmationCode,
            onValueChanged = {
                confirmationCode = it
                onConfirmationCodeInputChange(SignInRequestedForApprovalAction.ValidateConfirmationCode(confirmationCode))
            },
            enabled = !isLoading,
            label = { Text(text = stringResource(id = R.string.auth_login_confirmation_code)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.DefaultSpacing)
                .testTag(CONFIRMATION_CODE_FIELD_TAG)
        )

        Text(
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(id = R.string.auth_login_signin_requested_note),
            style = ProtonTypography.Default.defaultSmallWeak
        )

        ProtonSolidButton(
            contained = false,
            onClick = { onConfirmClicked(SignInRequestedForApprovalAction.Confirm) },
            enabled = confirmationButtonClickable && !isLoading,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login_yes_it_was_me))
        }

        ProtonTextButton(
            contained = false,
            onClick = { onRejectClicked(SignInRequestedForApprovalAction.Reject) },
            enabled = !isLoading,
            modifier = Modifier
                .padding(vertical = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.auth_login_no_it_wasnt_me))
        }
    }
}


@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun ConfirmationCodeSignInRequestedForApprovalScreenPreview() {
    ProtonTheme {
        SignInRequestedForApprovalScreen(
            onErrorMessage = {},
            state = SignInRequestedForApprovalState.Idle
        )
    }
}
