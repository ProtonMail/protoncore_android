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

package me.proton.core.auth.presentation.compose.sso

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.auth.presentation.compose.R
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

@Composable
public fun AccessDeniedScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onBackToSignInClicked: () -> Unit = {}
) {
    AccessResultScreen(
        modifier = modifier,
        onClose = onCloseClicked,
        onButtonClicked = onBackToSignInClicked,
        icon = R.drawable.ic_access_denied,
        titleText = R.string.auth_login_access_denied,
        subtitleText = R.string.auth_login_access_denied_contact_admin,
        buttonText = R.string.auth_login_back_to_sign_in
    )
}

@Composable
public fun AccessGrantedScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {}
) {
    AccessResultScreen(
        modifier = modifier,
        onClose = onCloseClicked,
        onButtonClicked = onContinueClicked,
        icon = R.drawable.ic_access_granted,
        titleText = R.string.auth_login_access_granted,
        subtitleText = R.string.auth_login_access_granted_info,
        buttonText = R.string.auth_login_continue
    )
}

@Composable
public fun AccessResultScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onButtonClicked: () -> Unit = {},
    @DrawableRes icon: Int,
    @StringRes titleText: Int,
    @StringRes subtitleText: Int,
    @StringRes buttonText: Int
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            painterResource(id = R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.presentation_back)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                Image(
                    modifier = Modifier
                        .height(64.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(icon),
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
                        .padding(top = ProtonDimens.MediumSpacing, bottom = ProtonDimens.MediumSpacing)
                )

                ProtonSolidButton(
                    contained = false,
                    onClick = { onButtonClicked() },
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight)
                ) {
                    Text(text = stringResource(buttonText))
                }
            }
        }
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AccessGrantedScreenPreview() {
    ProtonTheme {
        AccessGrantedScreen()
    }
}

@Composable
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun AccessDeniedScreenPreview() {
    ProtonTheme {
        AccessDeniedScreen()
    }
}
