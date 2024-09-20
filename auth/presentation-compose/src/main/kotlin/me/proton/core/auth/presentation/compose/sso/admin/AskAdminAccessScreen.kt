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

package me.proton.core.auth.presentation.compose.sso.admin

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import me.proton.core.auth.presentation.compose.R
import me.proton.core.auth.presentation.compose.SMALL_SCREEN_HEIGHT
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.RowWithComposables
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.LocalColors
import me.proton.core.compose.theme.LocalShapes
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.ProtonTypography
import me.proton.core.compose.theme.defaultSmallWeak

private val CompanyLogoSize = 32.dp
private val CompanyLogoFallbackIconSize = 32.dp

@Composable
public fun AskAdminAccessScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {},
    viewModel: AskAdminAccessViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AskAdminAccessScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onContinueClicked = onContinueClicked,
        state = state
    )
}

@Composable
public fun AskAdminAccessScreen(
    modifier: Modifier = Modifier,
    onCloseClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {},
    onError: (String?) -> Unit = {},
    onClose: () -> Unit = {},
    state: AskAdminState
) {
    when (state) {
        is AskAdminState.Error -> LaunchedEffect(state) { onError(state.cause.message) }
        is AskAdminState.Close -> LaunchedEffect(state) { onClose() }
        else -> Unit
    }
    AskAdminAccessScreen(
        modifier = modifier,
        onCloseClicked = onCloseClicked,
        onContinueClicked = onContinueClicked,
        isLoading = state is AskAdminState.Loading,
        organizationIcon = remember(state) { (state as? AskAdminState.DataLoaded)?.organizationIcon },
        organizationAdminEmail = remember(state) { (state as? AskAdminState.DataLoaded)?.organizationAdminEmail }
    )
}

@Composable
private fun AskAdminAccessScreen(
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
                    text = stringResource(id = R.string.auth_login_ask_admin_for_access),
                    style = ProtonTypography.Default.headline
                )

                Spacer(modifier = Modifier.height(ProtonDimens.DefaultSpacing))

                AdminEmailRow(
                    modifier = modifier,
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
    modifier: Modifier,
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
        RowWithComposables(
            leadingComposable = {
                ImageBox(organizationIcon = organizationIcon)
            },
            title = organizationAdminEmail ?: "",
            modifier = modifier.padding(
                vertical = ProtonDimens.ExtraSmallSpacing,
                horizontal = ProtonDimens.ExtraSmallSpacing
            )
        )
    }
}

@Composable
private fun ImageBox(
    organizationIcon: Any?
) {
    val defaultLogo = rememberAsyncImagePainter(R.drawable.ic_proton_users)
    
    Box(
        modifier = Modifier
            .clip(LocalShapes.current.large)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(organizationIcon)
                .build(),
            fallback = defaultLogo,
            placeholder = defaultLogo,
            modifier = Modifier
                .size(CompanyLogoSize)
                .run {
                    if (organizationIcon == null) {
                        background(LocalColors.current.interactionNorm)
                            .padding(max(0.dp, CompanyLogoSize - CompanyLogoFallbackIconSize) / 2)
                    } else this
                },
            contentDescription = null,
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
internal fun AskAdminAccessScreenPreview() {
    ProtonTheme {
        AskAdminAccessScreen(
            state = AskAdminState.DataLoaded("admin@test.com")
        )
    }
}
