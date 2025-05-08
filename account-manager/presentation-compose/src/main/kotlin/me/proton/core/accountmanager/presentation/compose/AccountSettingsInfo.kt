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

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewModel
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewState
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.component.RowWithComposables
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.telemetry.domain.entity.TelemetryPriority.Immediate
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.compose.LocalProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.compose.MeasureOnScreenClosed
import me.proton.core.telemetry.presentation.compose.MeasureOnScreenDisplayed
import me.proton.core.telemetry.presentation.compose.rememberClickedMeasureOperation

@Composable
fun AccountSettingsInfo(
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier,
    initialCount: Int = 2,
    signUpButtonGone: Boolean = false,
    signInButtonGone: Boolean = false,
    signOutButtonGone: Boolean = false,
    viewModel: AccountSettingsViewModel? = hiltViewModelOrNull(),
) {
    val state = when (viewModel) {
        null -> AccountSettingsViewState.Null
        else -> rememberAsState(viewModel.state, viewModel.initialState).value
    }

    CompositionLocalProvider(
        LocalProductMetricsDelegateOwner provides viewModel?.let { ProductMetricsDelegateOwner(it) }
    ) {
        AccountSettingsInfo(
            onSignUpClicked = onSignUpClicked,
            onSignInClicked = onSignInClicked,
            onAccountClicked = onAccountClicked,
            onSignOutClicked = onSignOutClicked,
            modifier = modifier,
            initialCount = initialCount,
            signUpButtonGone = signUpButtonGone,
            signInButtonGone = signInButtonGone,
            signOutButtonGone = signOutButtonGone,
            state = state
        )
    }
}

@Composable
fun AccountSettingsInfo(
    onSignUpClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onAccountClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier,
    initialCount: Int = 2,
    signUpButtonGone: Boolean = false,
    signInButtonGone: Boolean = false,
    signOutButtonGone: Boolean = false,
    state: AccountSettingsViewState,
) {
    MeasureOnScreenDisplayed("fe.info_account.displayed", priority = Immediate)
    MeasureOnScreenClosed("user.info_account.closed", priority = Immediate)

    val signUpClickedOp = rememberClickedMeasureOperation("user.info_account.clicked", "sign_up", Immediate)
    val signInClickedOp = rememberClickedMeasureOperation("user.info_account.clicked", "sign_in", Immediate)

    fun signUpClicked() {
        signUpClickedOp.measure()
        onSignUpClicked()
    }
    fun signInClicked() {
        signInClickedOp.measure()
        onSignInClicked()
    }

    when (state) {
        is AccountSettingsViewState.CredentialLess -> AccountSettingsCredentialLess(
            modifier = modifier,
            onCreateAccountClicked = { signUpClicked() },
            onSignInClicked = { signInClicked() },
            signUpButtonGone = signUpButtonGone,
            signInButtonGone = signInButtonGone,
        )

        is AccountSettingsViewState.LoggedIn -> AccountSettingsLoggedIn(
            modifier = modifier,
            onAccountClicked = onAccountClicked,
            onSignOutClicked = onSignOutClicked,
            state = state,
            initialCount = initialCount,
            signOutButtonGone = signOutButtonGone,
        )

        is AccountSettingsViewState.Hidden -> return
    }
}

@Composable
fun AccountSettingsCredentialLess(
    modifier: Modifier = Modifier,
    onCreateAccountClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    signUpButtonGone: Boolean = false,
    signInButtonGone: Boolean = false,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = ProtonTheme.colors.backgroundSecondary,
            contentColor = ProtonTheme.colors.textNorm,
        ),
        modifier = modifier.padding(
            horizontal = ProtonDimens.DefaultSpacing,
            vertical = ProtonDimens.DefaultSpacing
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ProtonDimens.DefaultSpacing,
                vertical = ProtonDimens.DefaultSpacing
            )
        ) {
            ProductIcons()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
            InfoText()
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
            val buttonModifier = Modifier
                .heightIn(min = 48.dp)
                .align(alignment = Alignment.CenterHorizontally)
                .fillMaxWidth()
            if (!signUpButtonGone) {
                CreateAccountButton(
                    onClick = onCreateAccountClicked,
                    modifier = buttonModifier
                )
            }
            if (!signInButtonGone) {
                SignInButton(
                    onClick = onSignInClicked,
                    modifier = buttonModifier
                )
            }
        }
    }
}

@Composable
fun AccountSettingsLoggedIn(
    modifier: Modifier = Modifier,
    onAccountClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    state: AccountSettingsViewState.LoggedIn,
    initialCount: Int = 2,
    signOutButtonGone: Boolean = false,
) {
    Column(
        modifier = modifier
    ) {
        RowWithComposables(
            modifier = modifier.padding(
                vertical = ProtonDimens.DefaultSpacing,
                horizontal = ProtonDimens.DefaultSpacing
            ),
            leadingComposable = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(ProtonDimens.MediumLargeSpacing)
                        .clip(RoundedCornerShape(ProtonDimens.SmallSpacing))
                        .background(ProtonTheme.colors.brandNorm)
                ) {
                    Text(
                        text = state.initials?.take(initialCount) ?: "",
                        style = ProtonTheme.typography.defaultNorm,
                        color = Color.White,
                    )
                }
            },
            title = state.displayName ?: "",
            subtitle = state.email,
            onClick = onAccountClicked,
        )
        if (!signOutButtonGone) {
            RowWithIcon(
                icon = R.drawable.ic_proton_arrow_in_to_rectangle,
                title = stringResource(id = R.string.auth_sign_out),
                onClick = onSignOutClicked
            )
        }
    }
}

@Composable
private fun ProductIcons(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSizeLogo),
            painter = painterResource(id = R.drawable.ic_logo_pass_no_bg),
        )
        Spacer(Modifier.size(ProtonDimens.ExtraSmallSpacing))
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSizeLogo),
            painter = painterResource(id = R.drawable.ic_logo_mail_no_bg),
        )
        Spacer(Modifier.size(ProtonDimens.ExtraSmallSpacing))
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSizeLogo),
            painter = painterResource(id = R.drawable.ic_logo_calendar_no_bg),
        )
        Spacer(Modifier.size(ProtonDimens.ExtraSmallSpacing))
        Image(
            contentDescription = null,
            modifier = Modifier.size(ProtonDimens.DefaultIconSizeLogo),
            painter = painterResource(id = R.drawable.ic_logo_drive_no_bg),
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
            style = ProtonTheme.typography.body1Medium,
            text = stringResource(id = R.string.auth_credentialless_settings_title)
        )
        Spacer(modifier = Modifier.height(ProtonDimens.ExtraSmallSpacing))
        Text(
            color = ProtonTheme.colors.textWeak,
            style = ProtonTheme.typography.body2Regular,
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
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.auth_create_account),
            // TODO: Remove after CP-7603.
            color = Color.White
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
            text = stringResource(id = R.string.auth_sign_in),
            // TODO: Remove after CP-7603.
            color = ProtonTheme.colors.textAccent
        )
    }
}

@Composable
private fun RowWithIcon(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    onClick: () -> Unit = { }
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
            .padding(
                vertical = ProtonDimens.DefaultSpacing,
                horizontal = ProtonDimens.DefaultSpacing
            )
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewAccountSettingsInfo() {
    ProtonTheme {
        AccountSettingsInfo(
            onSignUpClicked = {},
            onSignInClicked = {},
            onAccountClicked = {},
            onSignOutClicked = {},
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewAccountSettingsCredentialLess() {
    ProtonTheme {
        AccountSettingsCredentialLess(
            onCreateAccountClicked = {},
            onSignInClicked = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewAccountSettingsLogged() {
    ProtonTheme {
        AccountSettingsLoggedIn(
            onAccountClicked = {},
            onSignOutClicked = { },
            state = AccountSettingsViewState.Null
        )
    }
}
