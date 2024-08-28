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

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.domain.entity.UserId

public object DeviceSecretScreen {
    public const val KEY_USERID: String = "UserId"
    public fun SavedStateHandle.getUserId(): UserId = UserId(get<String>(KEY_USERID)!!)
}

@Composable
public fun DeviceSecretScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    viewModel: DeviceSecretViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DeviceSecretScreen(
        modifier = modifier,
        onClose = onClose,
        onCloseClicked = { viewModel.submit(DeviceSecretAction.Close) },
        state = state
    )
}

@Composable
public fun DeviceSecretScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onCloseClicked: () -> Unit = {},
    state: DeviceSecretViewState
) {
    LaunchedEffect(state) {
        when (state) {
            DeviceSecretViewState.Close -> onClose()
            DeviceSecretViewState.Idle -> Unit
        }
    }

    DeviceSecretScaffold(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
    )
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
public fun DeviceSecretScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
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
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->

    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Small screen height", heightDp = SMALL_SCREEN_HEIGHT)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.PIXEL_C)
@Preview(name = "Horizontal", widthDp = 800, heightDp = 360)
@Composable
internal fun DeviceSecretScaffoldPreview() {
    ProtonTheme {
        DeviceSecretScaffold(
            onCloseClicked = {},
        )
    }
}
