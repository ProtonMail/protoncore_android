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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonOutlinedTextFieldWithError
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

public const val SMALL_SCREEN_HEIGHT: Int = 680
internal const val USERNAME_FIELD_TAG = "USERNAME_FIELD_TAG"

@Composable
public fun LoginInputUsername(
    onContinueClicked: (LoginTwoStepAction.SetUsername) -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
    @StringRes titleText: Int = R.string.auth_login_sign_in,
    @StringRes subtitleText: Int = R.string.auth_login_details,
    state: LoginTwoStepViewState.UsernameInput = LoginTwoStepViewState.UsernameInput.Idle
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
            enabled = state is LoginTwoStepViewState.UsernameInput.Idle,
            onContinueClicked = onContinueClicked,
            onForgotUsernameClicked = onForgotUsernameClicked,
            usernameError = null // stringResource(R.string.auth_login_assistive_text)
        )
    }
}

@Composable
private fun LoginForm(
    onContinueClicked: (LoginTwoStepAction.SetUsername) -> Unit,
    onForgotUsernameClicked: () -> Unit,
    usernameError: String? = null,
    enabled: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ProtonOutlinedTextFieldWithError(
            text = username,
            onValueChanged = { username = it },
            enabled = enabled,
            errorText = usernameError,
            focusRequester = focusRequester,
            label = { Text(text = stringResource(id = R.string.auth_login_username)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.DefaultSpacing)
                .testTag(USERNAME_FIELD_TAG)
        )

        ProtonSolidButton(
            contained = false,
            onClick = { onContinueClicked(LoginTwoStepAction.SetUsername(username)) },
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login_continue))
        }

        ProtonTextButton(
            contained = false,
            onClick = onForgotUsernameClicked,
            modifier = Modifier
                .padding(vertical = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.auth_login_forgot_username))
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
internal fun LoginUsernameScreenPreview() {
    ProtonTheme {
        LoginInputUsername()
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginUsernameScreenFormPreview() {
    ProtonTheme {
        LoginForm(
            enabled = true,
            onContinueClicked = {},
            onForgotUsernameClicked = {},
            usernameError = stringResource(id = R.string.auth_login_assistive_text)
        )
    }
}
