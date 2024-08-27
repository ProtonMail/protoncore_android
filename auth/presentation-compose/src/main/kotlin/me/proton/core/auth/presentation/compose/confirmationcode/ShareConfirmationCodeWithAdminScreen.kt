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
public fun ShareConfirmationCodeWithAdminScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onErrorMessage: (String?) -> Unit,
    viewModel: ShareConfirmationCodeWithAdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShareConfirmationCodeWithAdminScreen(
        modifier = modifier,
        onClose = onClose,
        onCloseClicked = { viewModel.submit(ShareConfirmationCodeAction.Close) },
        onErrorMessage = onErrorMessage,
        onCancelClicked = { viewModel.submit(it) },
        onUseBackUpClicked = { viewModel.submit(it) },
        state = state
    )
}

@Composable
public fun ShareConfirmationCodeWithAdminScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit,
    onCancelClicked: (ShareConfirmationCodeAction.Cancel) -> Unit = {},
    onUseBackUpClicked: (ShareConfirmationCodeAction.UseBackUpPassword) -> Unit = {},
    state: ShareConfirmationCodeWithAdminState
) {
    when (state) {
        is ShareConfirmationCodeWithAdminState.DataLoaded -> {
            ShareConfirmationCodeWithAdminScreen(
                modifier = modifier,
                onCloseClicked = onCloseClicked,
                onCancelClicked = onCancelClicked,
                onUseBackUpClicked = onUseBackUpClicked,
                username = state.username,
                confirmationCode = state.confirmationCode.toCharArray().asList()
            )
        }
        is ShareConfirmationCodeWithAdminState.Loading -> {
            ShareConfirmationCodeWithAdminScreen(
                modifier = modifier,
                onCloseClicked = onCloseClicked,
                onCancelClicked = onCancelClicked,
                onUseBackUpClicked = onUseBackUpClicked
            )
        }

        is ShareConfirmationCodeWithAdminState.Error -> LaunchedEffect(state) { onErrorMessage(state.message) }
        is ShareConfirmationCodeWithAdminState.Cancel,
        is ShareConfirmationCodeWithAdminState.Close -> LaunchedEffect(state) { onClose() }
    }

}

@Composable
public fun ShareConfirmationCodeWithAdminScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onCancelClicked: (ShareConfirmationCodeAction.Cancel) -> Unit = {},
    onUseBackUpClicked: (ShareConfirmationCodeAction.UseBackUpPassword) -> Unit = {},
    username: String? = null,
    confirmationCode: List<Char>? = null
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
                    text = stringResource(id = R.string.auth_login_share_confirmation_code_with_admin),
                    style = ProtonTypography.Default.headline
                )

                if (username != null) {
                    Text(
                        modifier = Modifier
                            .padding(top = ProtonDimens.MediumSpacing),
                        text = stringResource(
                            id = R.string.auth_login_share_confirmation_code_with_admin_subtitle,
                            username
                        ),
                        style = ProtonTypography.Default.defaultSmallWeak
                    )
                }

                Spacer(Modifier.size(ProtonDimens.DefaultSpacing))

                ConfirmationDigits(digits = confirmationCode)

                ProtonSolidButton(
                    contained = false,
                    onClick = { onUseBackUpClicked(ShareConfirmationCodeAction.UseBackUpPassword) },
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight)
                ) {
                    Text(text = stringResource(R.string.auth_login_use_backup_password))
                }

                ProtonTextButton(
                    contained = false,
                    onClick = { onCancelClicked(ShareConfirmationCodeAction.Cancel) },
                    modifier = Modifier
                        .padding(vertical = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight),
                ) {
                    Text(text = stringResource(R.string.auth_login_cancel))
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
internal fun ShareConfirmationCodeWithAdminScreenPreview() {
    ProtonTheme {
        ShareConfirmationCodeWithAdminScreen(
            confirmationCode = listOf('6', '4', 'S', '3'),
            username = "test@protonmail.com"
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
internal fun ShareConfirmationCodeWithAdminLoadingScreenPreview() {
    ProtonTheme {
        ShareConfirmationCodeWithAdminScreen(
            confirmationCode = listOf('6', '4', 'S', '3'),
            username = "test@protonmail.com"
        )
    }
}
