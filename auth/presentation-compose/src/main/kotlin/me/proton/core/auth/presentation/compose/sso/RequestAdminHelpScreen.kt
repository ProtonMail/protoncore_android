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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalShapes
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultSmallWeak

private val CompanyLogoSize = 32.dp

@Composable
public fun RequestAdminHelpScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    viewModel: RequestAdminHelpViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RequestAdminHelpScreen(
        modifier = modifier,
        onBackClicked = onBackClicked,
        onContinueClicked = { viewModel.submit(RequestAdminHelpAction.Submit()) },
        onErrorMessage = onErrorMessage,
        onSuccess = onSuccess,
        state = state
    )
}

@Composable
public fun RequestAdminHelpScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {},
    onErrorMessage: (String?) -> Unit = {},
    onSuccess: () -> Unit = {},
    state: RequestAdminHelpState
) {
    LaunchedEffect(state) {
        when (state) {
            is RequestAdminHelpState.Error -> onErrorMessage(state.cause.message)
            is RequestAdminHelpState.AdminHelpHelpRequested -> onSuccess()
            else -> Unit
        }
    }

    RequestAdminHelpScaffold(
        modifier = modifier,
        onCloseClicked = onBackClicked,
        onContinueClicked = onContinueClicked,
        isLoading = state is RequestAdminHelpState.Loading,
        organizationAdminEmail = state.data.organizationAdminEmail,
        organizationIcon = state.data.organizationIcon
    )
}

@Composable
private fun RequestAdminHelpScaffold(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {},
    isLoading: Boolean,
    organizationAdminEmail: String? = null,
    organizationIcon: Any? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onCloseClicked) {
                        Icon(
                            painterResource(id = R.drawable.ic_proton_arrow_back),
                            contentDescription = stringResource(id = R.string.auth_login_close)
                        )
                    }
                },
                backgroundColor = LocalColors.current.backgroundNorm
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                Text(
                    text = stringResource(id = R.string.auth_login_ask_admin_for_access),
                    style = ProtonTypography.Default.headline
                )

                Spacer(modifier = Modifier.height(ProtonDimens.MediumSpacing))

                AdminEmailRow(
                    modifier = Modifier.fillMaxWidth(),
                    organizationIcon = organizationIcon,
                    organizationAdminEmail = organizationAdminEmail
                )

                Text(
                    modifier = Modifier.padding(top = ProtonDimens.MediumSpacing),
                    text = stringResource(id = R.string.auth_login_ask_admin_for_access_note),
                    style = ProtonTypography.Default.defaultSmallWeak
                )

                ProtonSolidButton(
                    contained = false,
                    loading = isLoading,
                    onClick = { onContinueClicked() },
                    modifier = Modifier
                        .padding(top = ProtonDimens.MediumSpacing)
                        .height(ProtonDimens.DefaultButtonMinHeight)
                ) {
                    Text(text = stringResource(R.string.auth_login_continue))
                }
            }
        }
    }
}

@Composable
private fun AdminEmailRow(
    modifier: Modifier = Modifier,
    organizationIcon: Any?,
    organizationAdminEmail: String?,
) {
    Card(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        contentColor = ProtonTheme.colors.textNorm,
        border = BorderStroke(1.dp, ProtonTheme.colors.separatorNorm),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(ProtonDimens.SmallSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val defaultLogo = painterResource(R.drawable.default_org_logo)
            AsyncImage(
                model = organizationIcon,
                error = defaultLogo,
                fallback = defaultLogo,
                placeholder = defaultLogo,
                modifier = Modifier
                    .size(CompanyLogoSize)
                    .clip(LocalShapes.current.medium),
                contentDescription = null,
            )
            Text(
                text = organizationAdminEmail ?: "",
                modifier = Modifier.padding(ProtonDimens.SmallSpacing),
                style = ProtonTypography.Default.defaultSmallNorm,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
internal fun AskAdminAccessScreenPreview() {
    ProtonTheme {
        RequestAdminHelpScreen(
            state = RequestAdminHelpState.Idle(
                RequestAdminHelpData(
                    organizationAdminEmail = "admin@domain.com"
                )
            )
        )
    }
}
