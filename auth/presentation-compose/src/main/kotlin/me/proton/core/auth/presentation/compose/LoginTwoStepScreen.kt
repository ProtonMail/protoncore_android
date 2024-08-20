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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.LoginTwoStepViewState.NextStep
import me.proton.core.auth.presentation.compose.LoginTwoStepViewState.PasswordInput
import me.proton.core.auth.presentation.compose.LoginTwoStepViewState.UsernameInput
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.domain.entity.UserId

@Composable
public fun LoginTwoStepScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit,
    onHelpClicked: () -> Unit,
    onForgotUsernameClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onErrorMessage: (String?) -> Unit = {},
    onExternalAccountLoginNeeded: (String) -> Unit = {},
    onExternalAccountNotSupported: () -> Unit = {},
    onLoggedIn: (UserId, NextStep) -> Unit = { _, _ -> },
    viewModel: LoginTwoStepViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginTwoStepScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onHelpClicked = onHelpClicked,
        onContinueClicked = { viewModel.submit(it) },
        onSignInClicked = { viewModel.submit(it) },
        onForgotUsernameClicked = onForgotUsernameClicked,
        onForgotPasswordClicked = onForgotPasswordClicked,
        onErrorMessage = onErrorMessage,
        onExternalAccountLoginNeeded = onExternalAccountLoginNeeded,
        onExternalAccountNotSupported = onExternalAccountNotSupported,
        onLoggedIn = onLoggedIn,
        state = state
    )
}

@Composable
public fun LoginTwoStepScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onContinueClicked: (LoginTwoStepAction.SetUsername) -> Unit = {},
    onSignInClicked: (LoginTwoStepAction.SetPassword) -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onExternalAccountLoginNeeded: (String) -> Unit = {},
    onExternalAccountNotSupported: () -> Unit = {},
    onLoggedIn: (UserId, NextStep) -> Unit = { _, _ -> },
    state: LoginTwoStepViewState = UsernameInput.Idle
) {
    LaunchedEffect(state) {
        when (state) {
            is UsernameInput.Idle -> Unit
            is UsernameInput.Checking -> Unit
            is UsernameInput.Error -> onErrorMessage(state.message)
            is UsernameInput.ExternalAccountLoginNeeded -> onExternalAccountLoginNeeded(state.username)
            is UsernameInput.ExternalAccountNotSupported -> onExternalAccountNotSupported()

            is PasswordInput.Idle -> Unit
            is PasswordInput.Checking -> Unit
            is PasswordInput.LoggedIn -> onLoggedIn(state.userId, state.nextStep)
            is PasswordInput.Error -> onErrorMessage(state.message)
        }
    }

    LoginTwoStepScaffold(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onHelpClicked = onHelpClicked,
        onForgotUsernameClicked = onForgotUsernameClicked,
        onForgotPasswordClicked = onForgotPasswordClicked,
        onContinueClicked = onContinueClicked,
        onSignInClicked = onSignInClicked,
        state = state
    )
}

@Composable
public fun LoginTwoStepScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onHelpClicked: () -> Unit = {},
    onSignInClicked: (LoginTwoStepAction.SetPassword) -> Unit = {},
    onContinueClicked: (LoginTwoStepAction.SetUsername) -> Unit = {},
    onForgotUsernameClicked: () -> Unit = {},
    onForgotPasswordClicked: () -> Unit = {},
    state: LoginTwoStepViewState = UsernameInput.Idle,
    @DrawableRes protonLogo: Int = R.drawable.ic_logo_proton,
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
                            stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                actions = {
                    ProtonTextButton(
                        onClick = onHelpClicked
                    ) {
                        Text(
                            text = stringResource(id = R.string.auth_login_help),
                            color = ProtonTheme.colors.textAccent,
                            style = ProtonTheme.typography.defaultStrongNorm
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (state) {
                is UsernameInput -> LoginInputUsername(
                    onContinueClicked = onContinueClicked,
                    onForgotUsernameClicked = onForgotUsernameClicked,
                    protonLogo = protonLogo,
                    state = state
                )

                is PasswordInput -> LoginInputPassword(
                    onSignInClicked = onSignInClicked,
                    onForgotPasswordClicked = onForgotPasswordClicked,
                    subtitleText = state.username,
                    state = state
                )
            }
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
internal fun LoginTwoStepUsernameScreenPreview() {
    ProtonTheme {
        LoginTwoStepScaffold(
            onForgotUsernameClicked = {},
            onForgotPasswordClicked = {},
            onHelpClicked = {},
            onCloseClicked = {},
            onContinueClicked = {},
            onSignInClicked = {}
        )
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun LoginTwoStepPasswordScreenPreview() {
    ProtonTheme {
        LoginTwoStepScaffold(
            onForgotUsernameClicked = {},
            onForgotPasswordClicked = {},
            onHelpClicked = {},
            onCloseClicked = {},
            onContinueClicked = {},
            onSignInClicked = {},
            state = PasswordInput.Idle("test-username")
        )
    }
}
