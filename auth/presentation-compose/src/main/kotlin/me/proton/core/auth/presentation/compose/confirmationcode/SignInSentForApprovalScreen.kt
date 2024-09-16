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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
public fun SignInSentForApprovalScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onEnterBackupPasswordClicked: () -> Unit = {},
    onAskAdminHelpClicked: () -> Unit = {},
    viewModel: SignInSentForApprovalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SignInSentForApprovalScreen(
        modifier = modifier,
        onClose = onCloseClicked,
        onCloseClicked = { viewModel.submit(SignInSentForApprovalAction.Close) },
        onErrorMessage = onErrorMessage,
        onUseBackUpClicked = onEnterBackupPasswordClicked,
        onAskAdminClicked = onAskAdminHelpClicked,
        state = state
    )
}

@Composable
public fun SignInSentForApprovalScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onUseBackUpClicked: () -> Unit,
    onAskAdminClicked: () -> Unit,
    state: SignInSentForApprovalState
) {
    LaunchedEffect(state) {
        when (state) {
            is SignInSentForApprovalState.Close -> onClose()
            is SignInSentForApprovalState.Error -> onErrorMessage(state.message)
            else -> Unit
        }
    }

    val data = remember(state) { state as? SignInSentForApprovalState.DataLoaded }
    SignInSentForApprovalScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onUseBackUpClicked = onUseBackUpClicked,
        onAskAdminClicked = onAskAdminClicked,
        confirmationCode = data?.confirmationCode?.toCharArray()?.asList(),
        availableDevices = data?.availableDevices
    )
}

@Composable
public fun SignInSentForApprovalScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit,
    onUseBackUpClicked: () -> Unit,
    onAskAdminClicked: () -> Unit,
    confirmationCode: List<Char>? = null,
    availableDevices: List<AvailableDeviceUIModel>? = null
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(id = R.string.auth_login_approve_signin_another_device),
                    style = ProtonTypography.Default.headline
                )

                Text(
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
                    text = stringResource(id = R.string.auth_login_approve_signin_another_device_subtitle),
                    style = ProtonTypography.Default.defaultSmallWeak
                )

                Spacer(Modifier.size(ProtonDimens.DefaultSpacing))

                ConfirmationDigits(digits = confirmationCode)

                AvailableDevicesList(
                    modifier = Modifier.weight(1f, fill = false),
                    devices = availableDevices
                )

                Spacer(Modifier.size(ProtonDimens.DefaultSpacing))

                ProtonSolidButton(
                    contained = false,
                    onClick = { onUseBackUpClicked() },
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight)
                ) {
                    Text(text = stringResource(R.string.auth_login_use_backup_password))
                }

                ProtonTextButton(
                    contained = false,
                    onClick = { onAskAdminClicked() },
                    modifier = Modifier
                        .padding(vertical = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight),
                ) {
                    Text(text = stringResource(R.string.auth_login_ask_admin_for_help))
                }
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
internal fun ApproveSignInScreenPreview() {
    ProtonTheme {
        SignInSentForApprovalScreen(
            confirmationCode = listOf('6', '4', 'S', '3'),
            availableDevices = listOf(
                AvailableDeviceUIModel(
                    id = "id1",
                    authDeviceName = "MacOS",
                    localizedClientName = "Proton Mail Chrome",
                    lastActivityTime = 1724945205966,
                    clientType = ClientType.Web
                ),
                AvailableDeviceUIModel(
                    id = "id2",
                    authDeviceName = "Google Pixel 7a",
                    localizedClientName = "Proton Mail Android",
                    lastActivityTime = 1724945205966,
                    clientType = ClientType.Android
                ),
                AvailableDeviceUIModel(
                    id = "id3",
                    authDeviceName = "Google Pixel 8",
                    localizedClientName = "Proton Mail Android",
                    lastActivityTime = 1724945205966,
                    clientType = ClientType.Android
                ),
                AvailableDeviceUIModel(
                    id = "id4",
                    authDeviceName = "Google Pixel 8 Pro",
                    localizedClientName = "Proton Mail Android",
                    lastActivityTime = 1724945205966,
                    clientType = ClientType.Android
                ),
                AvailableDeviceUIModel(
                    id = "id5",
                    authDeviceName = "Google Pixel 9 Pro",
                    localizedClientName = "Proton Mail Android",
                    lastActivityTime = 1724945205966,
                    clientType = ClientType.Android
                ),
                AvailableDeviceUIModel(
                    id = "id6",
                    authDeviceName = "Google Pixel 10 Pro",
                    localizedClientName = "Proton Mail Android",
                    lastActivityTime = 1724945205966,
                    clientType = ClientType.Android
                )
            ),
            onUseBackUpClicked = {},
            onAskAdminClicked = {},
            onCloseClicked = {}
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
internal fun ApproveSignInScreenLoadingPreview() {
    ProtonTheme {
        SignInSentForApprovalScreen(
            onCloseClicked = {},
            onUseBackUpClicked = {},
            onAskAdminClicked = {},
            confirmationCode = null,
            availableDevices = null
        )
    }
}
