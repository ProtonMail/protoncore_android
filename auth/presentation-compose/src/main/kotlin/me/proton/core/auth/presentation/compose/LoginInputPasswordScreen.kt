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

@file:OptIn(ExperimentalComposeUiApi::class)

package me.proton.core.auth.presentation.compose

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.auth.presentation.compose.LoginInputPasswordAction.SetPassword
import me.proton.core.compose.autofill.autofill
import me.proton.core.compose.component.ProtonPasswordOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryPriority.Immediate
import me.proton.core.telemetry.presentation.compose.rememberClickedMeasureOperation
import me.proton.core.telemetry.presentation.compose.rememberFocusedMeasureOperation

@Composable
public fun LoginInputPasswordScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onErrorMessage: (String?, UserCheckAction?) -> Unit = { _, _ -> },
    onSuccess: (userId: UserId) -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSrp: (AuthInfo.Srp) -> Unit = {},
    onNavigateToSso: (AuthInfo.Sso) -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToTroubleshoot: () -> Unit = {},
    onNavigateToExternalNotSupported: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    viewModel: LoginInputPasswordViewModel = hiltViewModel()
) {
    val focusedOp = rememberFocusedMeasureOperation("user.signin.focused", "passwordInput", Immediate)
    val clickedOp = rememberClickedMeasureOperation("user.signin.clicked", "passwordContinue", Immediate)

    fun onPasswordInputFocused() {
        focusedOp.measure()
    }
    fun onContinueClicked(action: SetPassword) {
        clickedOp.measure()
        viewModel.submit(action)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginInputPasswordScreen(
        username = viewModel.username,
        modifier = modifier,
        onClose = onClose,
        onErrorMessage = onErrorMessage,
        onSuccess = onSuccess,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToSrp = onNavigateToSrp,
        onNavigateToSso = onNavigateToSso,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        onNavigateToTroubleshoot = onNavigateToTroubleshoot,
        onNavigateToExternalNotSupported = onNavigateToExternalNotSupported,
        onNavigateToChangePassword = onNavigateToChangePassword,
        onCloseClicked = onClose,
        onPasswordInputFocused = { onPasswordInputFocused() },
        onContinueClicked = { onContinueClicked(it) },
        state = state
    )
}

@Composable
public fun LoginInputPasswordScreen(
    username: String,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onErrorMessage: (String?, UserCheckAction?) -> Unit = { _, _ -> },
    onSuccess: (userId: UserId) -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSrp: (AuthInfo.Srp) -> Unit = {},
    onNavigateToSso: (AuthInfo.Sso) -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToTroubleshoot: () -> Unit = {},
    onNavigateToExternalNotSupported: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onPasswordInputFocused: () -> Unit = {},
    onContinueClicked: (SetPassword) -> Unit = {},
    state: LoginInputPasswordState = LoginInputPasswordState.Idle
) {
    LaunchedEffect(state) {
        when (state) {
            is LoginInputPasswordState.Idle -> Unit
            is LoginInputPasswordState.Processing -> Unit
            is LoginInputPasswordState.Close -> onClose()
            is LoginInputPasswordState.ValidationError -> Unit
            is LoginInputPasswordState.Error -> onErrorMessage(state.message, null)
            is LoginInputPasswordState.ExternalNotSupported -> onNavigateToExternalNotSupported()
            is LoginInputPasswordState.NeedSrp -> onNavigateToSrp(state.authInfo)
            is LoginInputPasswordState.NeedSso -> onNavigateToSso(state.authInfo)
            is LoginInputPasswordState.Success -> onSuccess(state.userId)
            is LoginInputPasswordState.ChangePassword -> onNavigateToChangePassword()
            is LoginInputPasswordState.UserCheckError -> onErrorMessage(state.message, state.action)
        }
    }

    LoginInputPasswordScaffold(
        username = username,
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onHelpClicked = onNavigateToHelp,
        onPasswordInputFocused = onPasswordInputFocused,
        onContinueClicked = onContinueClicked,
        onForgotPasswordClicked = onNavigateToForgotPassword,
        onTroubleshootClicked = onNavigateToTroubleshoot,
        hasValidationError = state is LoginInputPasswordState.ValidationError,
        isTroubleshootVisible = (state as? LoginInputPasswordState.Error)?.isPotentialBlocking ?: false,
        isLoading = state.isLoading
    )
}

@Composable
public fun LoginInputPasswordScaffold(
    username: String,
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onPasswordInputFocused: () -> Unit = {},
    onContinueClicked: (SetPassword) -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    onTroubleshootClicked: () -> Unit = {},
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
    hasValidationError: Boolean = false,
    isTroubleshootVisible: Boolean = false,
    isLoading: Boolean = false
) {
    LoginScaffold(
        modifier = modifier,
        onBackClicked = onCloseClicked,
        onHelpClicked = onHelpClicked,
    ) {
        LoginInputPasswordColumn(
            username = username,
            onPasswordInputFocused = onPasswordInputFocused,
            onContinueClicked = onContinueClicked,
            onForgotPasswordClicked = onForgotPasswordClicked,
            onTroubleshootClicked = onTroubleshootClicked,
            protonLogo = protonLogo,
            hasValidationError = hasValidationError,
            isTroubleshootVisible = isTroubleshootVisible,
            isLoading = isLoading
        )
    }
}

@Composable
public fun LoginInputPasswordColumn(
    username: String,
    onPasswordInputFocused: () -> Unit = {},
    onContinueClicked: (SetPassword) -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    onTroubleshootClicked: () -> Unit = {},
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
    @StringRes titleText: Int = R.string.auth_login_welcome_back,
    hasValidationError: Boolean = false,
    isTroubleshootVisible: Boolean = false,
    isLoading: Boolean = false
) {
    Column(modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)) {
        Image(
            modifier = Modifier
                .height(64.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(protonLogo),
            contentDescription = null,
            alignment = Alignment.Center
        )

        Text(
            text = stringResource(titleText),
            style = ProtonTypography.Default.headline,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.MediumSpacing)
        )

        Text(
            text = username,
            style = ProtonTypography.Default.defaultSmallWeak,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.SmallSpacing)
        )

        PasswordForm(
            username = username,
            enabled = !isLoading,
            onPasswordInputFocused = onPasswordInputFocused,
            onContinueClicked = onContinueClicked,
            onForgotPasswordClicked = onForgotPasswordClicked,
            onTroubleshootClicked = onTroubleshootClicked,
            hasValidationError = hasValidationError,
            isTroubleshootVisible = isTroubleshootVisible
        )
    }
}

@Composable
private fun PasswordForm(
    username: String,
    onPasswordInputFocused: () -> Unit,
    onContinueClicked: (SetPassword) -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onTroubleshootClicked: () -> Unit,
    hasValidationError: Boolean,
    isTroubleshootVisible: Boolean,
    enabled: Boolean
) {
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ProtonPasswordOutlinedTextFieldWithError(
            text = password,
            onValueChanged = { password = it },
            enabled = enabled,
            errorText = if (hasValidationError) "" else null,
            focusRequester = focusRequester,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            keyboardActions = KeyboardActions { onContinueClicked(SetPassword(username, password)) },
            label = { Text(text = stringResource(id = R.string.auth_login_password)) },
            singleLine = true,
            modifier = Modifier
                .autofill(AutofillType.Password) { password = it }
                .fillMaxWidth()
                .padding(top = ProtonDimens.DefaultSpacing)
                .onGloballyPositioned { focusRequester.requestFocus() }
                .onFocusChanged { if (it.isFocused) onPasswordInputFocused() }
        )

        ProtonSolidButton(
            contained = false,
            loading = !enabled,
            onClick = { onContinueClicked(SetPassword(username, password)) },
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login_continue))
        }

        ProtonTextButton(
            contained = false,
            enabled = enabled,
            onClick = onForgotPasswordClicked,
            modifier = Modifier
                .padding(vertical = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.auth_login_forgot_password))
        }

        if (isTroubleshootVisible) {
            ProtonTextButton(
                contained = false,
                enabled = enabled,
                onClick = onTroubleshootClicked,
                modifier = Modifier.height(ProtonDimens.DefaultButtonMinHeight),
            ) {
                Text(text = stringResource(R.string.auth_login_troubleshhot))
            }
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginInputPasswordScreenPreview() {
    ProtonTheme {
        LoginInputPasswordScreen(
            username = "test@protonmail.com",
            state = LoginInputPasswordState.Idle
        )
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun PasswordFormPreview() {
    ProtonTheme {
        PasswordForm(
            username = "test@protonmail.com",
            enabled = true,
            onPasswordInputFocused = {},
            onContinueClicked = {},
            onForgotPasswordClicked = {},
            onTroubleshootClicked = {},
            hasValidationError = true,
            isTroubleshootVisible = true
        )
    }
}
