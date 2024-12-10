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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.auth.presentation.compose.LoginInputUsernameAction.SetUsername
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.presentation.compose.LocalClipManager
import me.proton.core.challenge.presentation.compose.LocalClipManager.OnClipChangedDisposableEffect
import me.proton.core.challenge.presentation.compose.payload
import me.proton.core.compose.autofill.autofill
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryPriority.Immediate
import me.proton.core.telemetry.presentation.compose.MeasureOnScreenClosed
import me.proton.core.telemetry.presentation.compose.MeasureOnScreenDisplayed
import me.proton.core.telemetry.presentation.compose.rememberClickedMeasureOperation
import me.proton.core.telemetry.presentation.compose.rememberFocusedMeasureOperation

@Composable
public fun LoginInputUsernameScreen(
    modifier: Modifier = Modifier,
    initialUsername: String? = null,
    onClose: () -> Unit = {},
    onErrorMessage: (String?, UserCheckAction?) -> Unit = { _,_ -> },
    onSuccess: (userId: UserId) -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSrp: (AuthInfo.Srp) -> Unit = {},
    onNavigateToSso: (AuthInfo.Sso) -> Unit = {},
    onNavigateToForgotUsername: () -> Unit = {},
    onNavigateToTroubleshoot: () -> Unit = {},
    onNavigateToExternalEmailNotSupported: () -> Unit = {},
    onNavigateToExternalSsoNotSupported: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    externalAction: SharedFlow<LoginInputUsernameAction> = MutableSharedFlow(),
    viewModel: LoginInputUsernameViewModel = hiltViewModel(),
) {
    MeasureOnScreenDisplayed("fe.signin.displayed", priority = Immediate)
    MeasureOnScreenClosed("user.signin.closed", priority = Immediate)

    val focusOp = rememberFocusedMeasureOperation("user.signin.focused", "usernameInput", Immediate)
    val clickOp = rememberClickedMeasureOperation("user.signin.clicked", "usernameContinue", Immediate)

    fun onUsernameInputFocused() {
        focusOp.measure()
    }
    fun onContinueClicked(action: SetUsername) {
        clickOp.measure()
        viewModel.submit(action)
    }

    LaunchedEffect(externalAction) { externalAction.collectLatest { viewModel.submit(it) } }

    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginInputUsernameScreen(
        modifier = modifier,
        initialUsername = initialUsername,
        onClose = onClose,
        onErrorMessage = onErrorMessage,
        onSuccess = onSuccess,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToSrp = onNavigateToSrp,
        onNavigateToSso = onNavigateToSso,
        onNavigateToForgotUsername = onNavigateToForgotUsername,
        onNavigateToExternalEmailNotSupported = onNavigateToExternalEmailNotSupported,
        onNavigateToExternalSsoNotSupported = onNavigateToExternalSsoNotSupported,
        onNavigateToChangePassword = onNavigateToChangePassword,
        onCloseClicked = onClose,
        onUsernameInputFocused = { onUsernameInputFocused() },
        onContinueClicked = { onContinueClicked(it) },
        onTroubleshootClicked = onNavigateToTroubleshoot,
        onFrameUpdated = { viewModel.onFrameUpdated(it) },
        state = state
    )
}

@Composable
public fun LoginInputUsernameScreen(
    modifier: Modifier = Modifier,
    initialUsername: String? = null,
    onClose: () -> Unit = {},
    onErrorMessage: (String?, UserCheckAction?) -> Unit = { _,_ -> },
    onSuccess: (userId: UserId) -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSrp: (AuthInfo.Srp) -> Unit = {},
    onNavigateToSso: (AuthInfo.Sso) -> Unit = {},
    onNavigateToForgotUsername: () -> Unit = {},
    onNavigateToExternalEmailNotSupported: () -> Unit = {},
    onNavigateToExternalSsoNotSupported: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onUsernameInputFocused: () -> Unit = {},
    onContinueClicked: (SetUsername) -> Unit = {},
    onTroubleshootClicked: () -> Unit = {},
    onFrameUpdated: (ChallengeFrameDetails) -> Unit = {},
    state: LoginInputUsernameState = LoginInputUsernameState.Idle
) {
    LaunchedEffect(state) {
        when (state) {
            is LoginInputUsernameState.Idle -> Unit
            is LoginInputUsernameState.Processing -> Unit
            is LoginInputUsernameState.Close -> onClose()
            is LoginInputUsernameState.ValidationError -> Unit
            is LoginInputUsernameState.Error -> onErrorMessage(state.message, null)
            is LoginInputUsernameState.ExternalEmailNotSupported -> onNavigateToExternalEmailNotSupported()
            is LoginInputUsernameState.ExternalSsoNotSupported -> onNavigateToExternalSsoNotSupported()
            is LoginInputUsernameState.NeedSrp -> onNavigateToSrp(state.authInfo)
            is LoginInputUsernameState.NeedSso -> onNavigateToSso(state.authInfo)
            is LoginInputUsernameState.Success -> onSuccess(state.userId)
            is LoginInputUsernameState.ChangePassword -> onNavigateToChangePassword()
            is LoginInputUsernameState.UserCheckError -> onErrorMessage(state.message, state.action)
        }
    }

    LoginInputUsernameScaffold(
        modifier = modifier,
        initialUsername = initialUsername,
        onCloseClicked = onCloseClicked,
        onHelpClicked = onNavigateToHelp,
        onUsernameInputFocused = onUsernameInputFocused,
        onContinueClicked = onContinueClicked,
        onForgotUsernameClicked = onNavigateToForgotUsername,
        onTroubleshootClicked = onTroubleshootClicked,
        onFrameUpdated = onFrameUpdated,
        hasValidationError = state is LoginInputUsernameState.ValidationError,
        isTroubleshootVisible = (state as? LoginInputUsernameState.Error)?.isPotentialBlocking ?: false,
        isLoading = state.isLoading
    )
}

@Composable
public fun LoginInputUsernameScaffold(
    modifier: Modifier = Modifier,
    initialUsername: String? = null,
    onCloseClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onUsernameInputFocused: () -> Unit = {},
    onContinueClicked: (SetUsername) -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    onTroubleshootClicked: () -> Unit = {},
    onFrameUpdated: (ChallengeFrameDetails) -> Unit = {},
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
        LoginInputUsernameColumn(
            initialUsername = initialUsername,
            onUsernameInputFocused = onUsernameInputFocused,
            onContinueClicked = onContinueClicked,
            onForgotUsernameClicked = onForgotUsernameClicked,
            onTroubleshootClicked = onTroubleshootClicked,
            onFrameUpdated = onFrameUpdated,
            protonLogo = protonLogo,
            hasValidationError = hasValidationError,
            isTroubleshootVisible = isTroubleshootVisible,
            isLoading = isLoading
        )
    }
}

@Composable
public fun LoginInputUsernameColumn(
    initialUsername: String? = null,
    onUsernameInputFocused: () -> Unit = {},
    onContinueClicked: (SetUsername) -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    onTroubleshootClicked: () -> Unit = {},
    onFrameUpdated: (ChallengeFrameDetails) -> Unit = {},
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
    @StringRes titleText: Int = R.string.auth_login_sign_in,
    @StringRes subtitleText: Int = R.string.auth_login_details,
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
            text = stringResource(subtitleText),
            style = ProtonTypography.Default.defaultSmallWeak,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = ProtonDimens.SmallSpacing)
        )

        LoginForm(
            initialUsername = initialUsername,
            enabled = !isLoading,
            onUsernameInputFocused = onUsernameInputFocused,
            onContinueClicked = onContinueClicked,
            onForgotUsernameClicked = onForgotUsernameClicked,
            onTroubleshootClicked = onTroubleshootClicked,
            onFrameUpdated = onFrameUpdated,
            hasValidationError = hasValidationError,
            isTroubleshootVisible = isTroubleshootVisible
        )
    }
}

@Composable
private fun LoginForm(
    initialUsername: String?,
    onUsernameInputFocused: () -> Unit,
    onContinueClicked: (SetUsername) -> Unit,
    onForgotUsernameClicked: () -> Unit,
    onTroubleshootClicked: () -> Unit,
    onFrameUpdated: (ChallengeFrameDetails) -> Unit,
    hasValidationError: Boolean,
    isTroubleshootVisible: Boolean,
    enabled: Boolean
) {
    val clipManager = LocalClipManager.current
    val textCopied = remember { MutableStateFlow("") }
    clipManager?.OnClipChangedDisposableEffect { textCopied.value = it }

    val textChange = remember { MutableStateFlow("" to "") }
    val assistiveText = stringResource(R.string.auth_login_assistive_text)
    var username by rememberSaveable { mutableStateOf(initialUsername ?: "") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ProtonOutlinedTextFieldWithError(
            text = username,
            onValueChanged = { new ->
                textChange.value = textChange.value.second to new
                username = new
            },
            enabled = enabled,
            errorText = if (hasValidationError) assistiveText else null,
            requestFocus = { initialUsername == null },
            onFocusChanged = { if (it) onUsernameInputFocused() },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { onContinueClicked(SetUsername(username)) },
            label = { Text(text = stringResource(id = R.string.auth_login_username)) },
            singleLine = true,
            modifier = Modifier
                .autofill(AutofillType.Username) { username = it }
                .fillMaxWidth()
                .padding(top = ProtonDimens.DefaultSpacing)
                .payload("login", "username", textChange, textCopied, onFrameUpdated)
        )

        ProtonSolidButton(
            contained = false,
            loading = !enabled,
            onClick = { onContinueClicked(SetUsername(username)) },
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login_continue))
        }

        ProtonTextButton(
            contained = false,
            enabled = enabled,
            onClick = onForgotUsernameClicked,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.auth_login_forgot_username))
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
internal fun LoginTwoStepUsernameScreenPreview() {
    ProtonTheme {
        LoginInputUsernameScreen(
            state = LoginInputUsernameState.Idle
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
internal fun LoginFormPreview() {
    ProtonTheme {
        LoginForm(
            initialUsername = null,
            onUsernameInputFocused = {},
            onContinueClicked = {},
            onForgotUsernameClicked = {},
            onTroubleshootClicked = {},
            onFrameUpdated = {},
            hasValidationError = true,
            isTroubleshootVisible = true,
            enabled = true
        )
    }
}
