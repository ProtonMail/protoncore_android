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

package me.proton.core.accountmanager.presentation.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewModel
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewModel.Companion.INITIAL_STATE
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewState
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.protonButtonColors
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultSmallStrongUnspecified
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.domain.entity.UserId

@Composable
fun AccountSettingsInfo(
    modifier: Modifier = Modifier,
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    viewModel: AccountSettingsViewModel? = hiltViewModelOrNull(),
) {
    if (viewModel == null) return
    val state by rememberAsState(flow = viewModel.state, initial = INITIAL_STATE)

    when (state) {
        is AccountSettingsViewState.CredentialLess -> {
            AccountSettingsCredentialLess(
                modifier = modifier,
                onCreateAccountClicked = onSignUpClicked,
                onSignInClicked = onSignInClicked
            )
        }

        is AccountSettingsViewState.LoggedIn -> {
            AccountSettingsLoggedIn(
                modifier = modifier,
                onAccountClicked = onAccountClicked,
                onSignOutClicked = onSignOutClicked,
                loggedIn = state as AccountSettingsViewState.LoggedIn
            )
        }

        is AccountSettingsViewState.Hidden -> return
    }
}

@Composable
fun AccountSettingsCredentialLess(
    modifier: Modifier = Modifier,
    onCreateAccountClicked: () -> Unit,
    onSignInClicked: () -> Unit
) {
    Column(
        modifier.padding(
            horizontal = dimensionResource(id = R.dimen.gap_large),
            vertical = dimensionResource(id = R.dimen.gap_medium_plus)
        )
    ) {
        ProductIcons(modifier)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
        InfoText(modifier)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
        CreateAccountButton(
            onClick = onCreateAccountClicked,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .fillMaxWidth()
        )
        SignInButton(
            onClick = onSignInClicked,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .fillMaxWidth()
        )
    }
}

@Composable
fun AccountSettingsLoggedIn(
    modifier: Modifier = Modifier,
    onAccountClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    loggedIn: AccountSettingsViewState.LoggedIn
) {
    Column(
        modifier.padding(
            horizontal = dimensionResource(id = R.dimen.gap_large),
            vertical = dimensionResource(id = R.dimen.gap_medium_plus)
        )
    ) {
        RowWithComposables(
            leadingComposable = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(ProtonDimens.MediumLargeSpacing)
                        .clip(RoundedCornerShape(ProtonDimens.SmallSpacing))
                        .background(ProtonTheme.colors.brandNorm)
                ) {
                    Text(
                        text = loggedIn.shortName ?: "",
                        style = ProtonTheme.typography.defaultNorm
                    )
                }
            },
            title = loggedIn.displayName ?: "",
            subtitle = loggedIn.email,
            onClick = onAccountClicked,
        )
        RowWithIcon(
            modifier = modifier,
            icon = me.proton.core.presentation.R.drawable.ic_proton_arrow_in_to_rectangle,
            title = stringResource(id = R.string.auth_sign_out),
            onClick = onSignOutClicked
        )
    }
}

@Composable
private fun RowWithIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    onClick: (() -> Unit)? = null
) {
    RowWithComposables(
        leadingComposable = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
            )
        },
        title = title,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun ProductIcons(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSize),
            painter = painterResource(id = R.drawable.ic_logo_mail_no_bg),
        )
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSize),
            painter = painterResource(id = R.drawable.ic_logo_drive_no_bg),
        )
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSize),
            painter = painterResource(id = R.drawable.ic_logo_vpn_no_bg),
        )
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSize),
            painter = painterResource(id = R.drawable.ic_logo_calendar_no_bg),
        )
    }
}

@Composable
private fun InfoText(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            color = ProtonTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallStrongUnspecified,
            text = stringResource(id = R.string.auth_credentialless_settings_title)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_small)))
        Text(
            color = ProtonTheme.colors.textNorm,
            style = ProtonTheme.typography.captionNorm,
            text = stringResource(id = R.string.auth_credentialless_settings_subtitle)
        )
    }
}

@Composable
private fun CreateAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonSolidButton(
        colors = ButtonDefaults.protonButtonColors(
            backgroundColor = ProtonTheme.colors.interactionNorm
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            color = ProtonTheme.colors.textNorm,
            text = stringResource(id = R.string.auth_create_account)
        )
    }
}

@Composable
private fun SignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonTextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            color = ProtonTheme.colors.textNorm,
            text = stringResource(id = R.string.auth_sign_in)
        )
    }
}

@Composable
private fun RowWithComposables(
    modifier: Modifier = Modifier,
    leadingComposable: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    var baseModifier = modifier
        .fillMaxWidth()

    if (onClick != null) {
        baseModifier = baseModifier
            .clickable(onClick = onClick)

    }
    baseModifier =
        baseModifier.padding(vertical = ProtonDimens.DefaultSpacing, horizontal = ProtonDimens.DefaultSpacing)

    Row(
        modifier = baseModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(ProtonDimens.LargeSpacing),
            contentAlignment = Alignment.Center
        ) {
            leadingComposable()
        }
        Column(
            modifier = Modifier
                .padding(start = ProtonDimens.DefaultSpacing)
                .weight(1f)
        ) {
            Text(
                text = title,
                style = ProtonTheme.typography.defaultNorm
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = ProtonTheme.typography.defaultWeak
                )
            }
        }
    }
}

@Preview
@Composable
internal fun PreviewAccountSettingsCredentialLessLight() {
    ProtonTheme() {
        AccountSettingsCredentialLess(
            onCreateAccountClicked = {},
            onSignInClicked = {}
        )
    }
}

@Preview
@Composable
internal fun PreviewAccountSettingsCredentialLessDark() {
    ProtonTheme() {
        AccountSettingsCredentialLess(
            onCreateAccountClicked = {},
            onSignInClicked = {}
        )
    }
}

@Preview
@Composable
internal fun PreviewAccountSettingsLoggedInLight() {
    ProtonTheme() {
        AccountSettingsLoggedIn(
            onAccountClicked = {},
            onSignOutClicked = { },
            loggedIn = AccountSettingsViewState.LoggedIn(
                UserId("test-user-id"),
                "SN",
                "Display Name",
                "email@proton.com"
            )
        )
    }
}

@Preview
@Composable
internal fun PreviewAccountSettingsLoggedInDark() {
    ProtonTheme(isDark = true) {
        AccountSettingsLoggedIn(
            onAccountClicked = {},
            onSignOutClicked = { },
            loggedIn = AccountSettingsViewState.LoggedIn(
                UserId("test-user-id"),
                "SN",
                "Display Name",
                "email@proton.com"
            )
        )
    }
}
