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

@file:OptIn(ExperimentalMaterialApi::class)

package me.proton.core.auth.presentation.compose.sso

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.auth.domain.entity.AuthDeviceId
import me.proton.core.auth.domain.entity.AuthDevicePlatform
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.Confirm
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.Reject
import me.proton.core.auth.presentation.compose.sso.MemberApprovalAction.SetInput
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Closed
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Confirmed
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Confirming
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Error
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Idle
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Loading
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Rejected
import me.proton.core.auth.presentation.compose.sso.MemberApprovalState.Rejecting
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
public fun MemberApprovalScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: MemberApprovalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MemberApprovalScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onErrorMessage = onErrorMessage,
        onInputChanged = { viewModel.submit(it) },
        onConfirmClicked = { viewModel.submit(it) },
        onRejectClicked = { viewModel.submit(it) },
        onSuccess = onSuccess,
        state = state
    )
}

@Composable
public fun MemberApprovalScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onInputChanged: (SetInput) -> Unit = {},
    onConfirmClicked: (Confirm) -> Unit = {},
    onRejectClicked: (Reject) -> Unit = {},
    onSuccess: () -> Unit = {},
    state: MemberApprovalState
) {
    LaunchedEffect(state) {
        when (state) {
            is Closed -> onSuccess()
            is Confirmed -> onSuccess()
            is Rejected -> onSuccess()
            is Error -> onErrorMessage(state.message)
            else -> Unit
        }
    }
    MemberApprovalScaffold(
        modifier = modifier,
        onInputChanged = onInputChanged,
        onCloseClicked = onCloseClicked,
        onConfirmClicked = onConfirmClicked,
        onRejectClicked = onRejectClicked,
        confirmationButtonClickable = state.data.hasValidCode(),
        isLoading = state is Loading,
        isConfirming = state is Confirming,
        isRejecting = state is Rejecting,
        data = state.data
    )
}

@Composable
public fun MemberApprovalScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit,
    onInputChanged: (SetInput) -> Unit = {},
    onConfirmClicked: (Confirm) -> Unit,
    onRejectClicked: (Reject) -> Unit,
    confirmationButtonClickable: Boolean,
    isLoading: Boolean,
    isConfirming: Boolean,
    isRejecting: Boolean,
    data: MemberApprovalData
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
                onInputChanged = onInputChanged,
                onConfirmClicked = onConfirmClicked,
                onRejectClicked = onRejectClicked,
                isConfirmButtonEnabled = confirmationButtonClickable,
                isLoading = isLoading,
                isConfirming = isConfirming,
                isRejecting = isRejecting,
                data = data,
            )
        }
    }
}

@Composable
private fun ConfirmationCodeInputScreen(
    onInputChanged: (SetInput) -> Unit = {},
    onConfirmClicked: (Confirm) -> Unit = {},
    onRejectClicked: (Reject) -> Unit = {},
    isConfirmButtonEnabled: Boolean = false,
    isLoading: Boolean = false,
    isConfirming: Boolean = false,
    isRejecting: Boolean = false,
    data: MemberApprovalData,
) {
    var confirmationCode by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<AuthDeviceData?>(null) }
    when (selected?.deviceId) {
        null -> selected = data.pendingDevices.firstOrNull()
        !in data.pendingDevices.map { it.deviceId } -> selected = data.pendingDevices.firstOrNull()
    }

    Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
        Text(
            text = stringResource(id = R.string.auth_login_signin_requested),
            style = ProtonTypography.Default.headline
        )

        ExposedDropdownMenuBox(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            expanded = expanded && !isLoading,
            onExpandedChange = {}
        ) {
            Card(
                modifier = Modifier.clickable { expanded = !expanded },
                contentColor = ProtonTheme.colors.textNorm,
                elevation = 0.dp
            ) {
                AuthDeviceListItem(
                    device = selected,
                    lastActivityVisible = false,
                    trailing = {
                        if (data.pendingDevices.size > 1) {
                            TrailingIcon(expanded = expanded)
                        }
                    }
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                data.pendingDevices.forEach { device ->
                    DropdownMenuItem(
                        onClick = {
                            expanded = false
                            selected = device
                            onInputChanged(SetInput(device.deviceId, confirmationCode))
                        },
                        contentPadding = PaddingValues(0.dp, 0.dp)
                    ) {
                        AuthDeviceListItem(device = device)
                    }
                }
            }
        }

        Text(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(
                R.string.auth_login_signin_requested_subtitle,
                data.email ?: "..."
            ),
            style = ProtonTypography.Default.defaultSmallWeak
        )

        ConfirmationDigitTextField(
            value = confirmationCode,
            onValueChange = { code ->
                confirmationCode = code.uppercase().take(4)
                selected?.let { onInputChanged(SetInput(it.deviceId, confirmationCode)) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = ProtonDimens.DefaultSpacing)
                .testTag(CONFIRMATION_CODE_FIELD_TAG),
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Text
            ),
            enabled = !isLoading && !isConfirming && !isRejecting,
            singleLine = true,
        )

        Text(
            modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
            text = stringResource(id = R.string.auth_login_signin_requested_note),
            style = ProtonTypography.Default.defaultSmallWeak
        )

        ProtonSolidButton(
            contained = false,
            onClick = { selected?.let { onConfirmClicked(Confirm(it.deviceId, data.deviceSecret)) } },
            enabled = isConfirmButtonEnabled && !isLoading && !isConfirming && !isRejecting,
            loading = isConfirming,
            modifier = Modifier
                .padding(top = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight)
        ) {
            Text(text = stringResource(R.string.auth_login_yes_it_was_me))
        }

        ProtonTextButton(
            contained = false,
            onClick = { selected?.let { onRejectClicked(Reject(it.deviceId)) } },
            enabled = !isLoading && !isConfirming && !isRejecting,
            loading = isRejecting,
            modifier = Modifier
                .padding(vertical = ProtonDimens.MediumSpacing)
                .height(ProtonDimens.DefaultButtonMinHeight),
        ) {
            Text(text = stringResource(R.string.auth_login_no_it_wasnt_me))
        }
    }
}

@Preview(name = "Light mode", showBackground = true)
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ConfirmationCodeInputScreenPreview() {
    ProtonTheme {
        ConfirmationCodeInputScreen(
            data = MemberApprovalData(
                email = "user@domain.com",
                pendingDevices = listOf(
                    AuthDeviceData(
                        deviceId = AuthDeviceId("1"),
                        name = "Google Pixel 8",
                        localizedClientName = "Proton for Android",
                        platform = AuthDevicePlatform.Android,
                        lastActivityTime = 0,
                        lastActivityReadable = "10 minutes"
                    ),
                    AuthDeviceData(
                        deviceId = AuthDeviceId("2"),
                        name = "Google Pixel 9",
                        localizedClientName = "Proton for Android",
                        platform = AuthDevicePlatform.Android,
                        lastActivityTime = 0,
                        lastActivityReadable = "14 minutes"
                    )
                )
            )
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
internal fun MemberApprovalScreenPreview() {
    ProtonTheme {
        MemberApprovalScreen(
            state = Idle(
                data = MemberApprovalData(
                    email = "user@domain.com",
                    pendingDevices = listOf(
                        AuthDeviceData(
                            deviceId = AuthDeviceId("id"),
                            name = "Google Pixel 8",
                            localizedClientName = "Proton for Android",
                            platform = AuthDevicePlatform.Android,
                            lastActivityTime = 0,
                            lastActivityReadable = "10 minutes"
                        )
                    )
                )
            )
        )
    }
}
