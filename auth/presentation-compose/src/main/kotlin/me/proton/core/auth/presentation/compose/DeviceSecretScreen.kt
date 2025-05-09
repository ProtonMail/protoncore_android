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
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import me.proton.core.auth.presentation.compose.DeviceSecretViewState.*
import me.proton.core.auth.presentation.compose.sso.BackupPasswordChangeScreen
import me.proton.core.auth.presentation.compose.sso.BackupPasswordInputScreen
import me.proton.core.auth.presentation.compose.sso.BackupPasswordSetupScreen
import me.proton.core.auth.presentation.compose.sso.AccessDeniedScreen
import me.proton.core.auth.presentation.compose.sso.AccessGrantedScreen
import me.proton.core.auth.presentation.compose.sso.RequestAdminHelpScreen
import me.proton.core.auth.presentation.compose.sso.WaitingAdminScreen
import me.proton.core.auth.presentation.compose.sso.WaitingMemberScreen
import me.proton.core.compose.component.ProtonCenteredProgress
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.util.LaunchResumeEffect
import me.proton.core.domain.entity.UserId

@Composable
public fun DeviceSecretScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onSuccess: (userId: UserId) -> Unit = {},
    onCloseMessage: (String?) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onNavigateToRequestAdminHelp: () -> Unit = {},
    onNavigateToBackupPasswordInput: () -> Unit = {},
    externalAction: SharedFlow<DeviceSecretAction> = MutableSharedFlow(),
    viewModel: DeviceSecretViewModel = hiltViewModel()
) {
    LaunchResumeEffect { externalAction.collectLatest { viewModel.submit(it) } }

    val state by viewModel.state.collectAsStateWithLifecycle()

    Crossfade(state, label = "DeviceSecretScreen state fade") {
        DeviceSecretScreen(
            modifier = modifier,
            onClose = onClose,
            onCloseMessage = onCloseMessage,
            onErrorMessage = onErrorMessage,
            onSuccess = onSuccess,
            onReloadState = { viewModel.submit(DeviceSecretAction.Load()) },
            onCloseClicked = { viewModel.submit(DeviceSecretAction.Close) },
            onContinueClicked = { viewModel.submit(DeviceSecretAction.Continue) },
            onNavigateToRequestAdminHelp = onNavigateToRequestAdminHelp,
            onNavigateToBackupPasswordInput = onNavigateToBackupPasswordInput,
            state = it
        )
    }
}

@Composable
public fun DeviceSecretScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onSuccess: (userId: UserId) -> Unit = {},
    onCloseMessage: (String?) -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onReloadState: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {},
    onNavigateToRequestAdminHelp: () -> Unit = {},
    onNavigateToBackupPasswordInput: () -> Unit = {},
    state: DeviceSecretViewState
) {
    when (state) {
        is Close -> onClose()
        is Success -> onSuccess(state.userId)
        is Loading -> DeviceSecretScaffold(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onRetryClicked = onReloadState,
            isLoading = true,
            email = state.email
        )
        is Error -> DeviceSecretScaffold(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onRetryClicked = onReloadState,
            email = state.email,
            error = state.message
        )

        is FirstLogin -> BackupPasswordSetupScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onErrorMessage = onErrorMessage,
            onSuccess = onReloadState,
            userId = state.userId
        )

        is InvalidSecret.NoDevice.BackupPassword -> BackupPasswordInputScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onCloseMessage = onCloseMessage,
            onErrorMessage = onErrorMessage,
            onSuccess = { Unit },
            onNavigateToRoot = { Unit },
            onNavigateToRequestAdminHelp = onNavigateToRequestAdminHelp
        )

        is InvalidSecret.NoDevice.WaitingAdmin -> WaitingAdminScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onErrorMessage = onErrorMessage,
            onBackupPasswordClicked = onNavigateToBackupPasswordInput
        )

        is InvalidSecret.OtherDevice.WaitingMember -> WaitingMemberScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onErrorMessage = onErrorMessage,
            onBackupPasswordClicked = onNavigateToBackupPasswordInput,
            onRequestAdminHelpClicked = onNavigateToRequestAdminHelp
        )

        is InvalidSecret.NoDevice.RequireAdmin -> RequestAdminHelpScreen(
            modifier = modifier,
            onBackClicked = onCloseClicked,
            onErrorMessage = onErrorMessage,
            onSuccess = onReloadState,
        )

        is DeviceRejected -> AccessDeniedScreen(
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onBackToSignInClicked = onCloseClicked
        )

        is DeviceGranted -> AccessGrantedScreen(
            modifier = modifier,
            onCloseClicked = onContinueClicked,
            onContinueClicked = onContinueClicked
        )

        is ChangePassword -> BackupPasswordChangeScreen(
            userId = state.userId,
            modifier = modifier,
            onCloseClicked = onCloseClicked,
            onCloseMessage = onCloseMessage,
            onErrorMessage = onErrorMessage,
            onSuccess = onReloadState
        )
    }
}

@Composable
public fun DeviceSecretScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onRetryClicked: () -> Unit= {},
    isLoading: Boolean = false,
    error: String? = null,
    email: String? = null
) {
    val hostState = remember { ProtonSnackbarHostState() }
    val retry = stringResource(R.string.presentation_retry)
    LaunchedEffect(error) {
        error?.let {
            hostState.showSnackbar(
                type = ProtonSnackbarType.ERROR,
                message = error,
                actionLabel = retry,
                duration = SnackbarDuration.Indefinite
            ).also { onRetryClicked() }
        }
    }

    Scaffold(
        snackbarHost = { ProtonSnackbarHost(hostState) },
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
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ProtonDimens.DefaultSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.size(ProtonDimens.DefaultIconWithPadding)) {
                    if (isLoading) {
                        ProtonCenteredProgress()
                    } else {
                        val defaultLogo = painterResource(R.drawable.default_org_logo)
                        Image(painter = defaultLogo, contentDescription = null)
                    }
                }
                Text(
                    text = stringResource(R.string.auth_login_sso_main_signing_you_in),
                    style = ProtonTypography.Default.headline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing)
                )
                Text(
                    text = stringResource(R.string.auth_login_sso_main_to_your_organization),
                    style = ProtonTypography.Default.defaultSmallWeak,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = ProtonDimens.SmallSpacing)
                )
                Card(
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
                    shape = ProtonTheme.shapes.small,
                    backgroundColor = Color.Transparent,
                    contentColor = ProtonTheme.colors.textNorm,
                    border = BorderStroke(1.dp, ProtonTheme.colors.separatorNorm),
                    elevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(ProtonDimens.SmallSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.default_org_logo),
                            modifier = Modifier.size(ProtonDimens.DefaultIconSizeLogo),
                            contentDescription = null,
                        )
                        Text(
                            text = email ?: "",
                            modifier = Modifier.padding(ProtonDimens.SmallSpacing),
                            style = ProtonTypography.Default.defaultSmallNorm,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
private fun DeviceSecretScreenErrorPreview() {
    ProtonTheme {
        DeviceSecretScreen(
            state = Error(
                email = "user@domain.com",
                message = "An error occurs. Please try again."
            )
        )
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
private fun DeviceSecretScreenLoadingPreview() {
    ProtonTheme {
        DeviceSecretScreen(
            state = Loading(
                email = "user@domain.com"
            )
        )
    }
}
