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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewModel
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
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.compose.LocalProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.compose.MeasureOnScreenClosed
import me.proton.core.telemetry.presentation.compose.MeasureOnScreenDisplayed
import me.proton.core.telemetry.presentation.compose.MeasureOnViewClicked

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
    loggedInContent: (@Composable (UserId) -> Unit)? = null,
    nonLoggedInContent: (@Composable (UserId?) -> Unit)? = null,
    viewModel: AccountSettingsViewModel? = hiltViewModelOrNull(),
) {
    val state = when (viewModel) {
        null -> AccountSettingsViewState.LoggedIn(
            userId = UserId("userId"),
            initials = "DU",
            displayName = "Display Name",
            email = "example@domain.com",
        )

        else -> rememberAsState(viewModel.state, viewModel.initialState).value
    }

    var isSignUpClicked by remember { mutableStateOf(false) }
    var isSignInClicked by remember { mutableStateOf(false) }

    val delegate = if (viewModel != null) ProductMetricsDelegateOwner(viewModel) else LocalProductMetricsDelegateOwner.current
    CompositionLocalProvider(
        LocalProductMetricsDelegateOwner provides delegate
    ) {
        MeasureOnScreenDisplayed("fe.info_account.displayed")
        MeasureOnScreenClosed("user.info_account.closed")

        when (state) {
            is AccountSettingsViewState.CredentialLess -> {
                when (nonLoggedInContent) {
                    null -> AccountSettingsCredentialLess(
                        modifier = modifier,
                        onCreateAccountClicked = {
                            onSignUpClicked()
                            isSignUpClicked = true
                        },
                        onSignInClicked = {
                            onSignInClicked()
                            isSignInClicked = true
                        },
                        signUpButtonGone = signUpButtonGone,
                        signInButtonGone = signInButtonGone,
                    )

                    else -> nonLoggedInContent(state.userId)
                }
            }

            is AccountSettingsViewState.LoggedIn -> {
                when (loggedInContent) {
                    null -> AccountSettingsLoggedIn(
                        modifier = modifier,
                        onAccountClicked = onAccountClicked,
                        onSignOutClicked = onSignOutClicked,
                        state = state,
                        initialCount = initialCount,
                        signOutButtonGone = signOutButtonGone,
                    )

                    else -> loggedInContent(state.userId)
                }
            }

            is AccountSettingsViewState.Hidden -> return@CompositionLocalProvider
        }

        if (isSignUpClicked) {
            MeasureOnViewClicked(event = "user.info_account.clicked", productDimensions = mapOf("item" to "sign_up"))
            isSignUpClicked = false

        }
        if (isSignInClicked) {
            MeasureOnViewClicked(event = "user.info_account.clicked", productDimensions = mapOf("item" to "sign_in"))
            isSignInClicked = false
        }
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
            if (!signUpButtonGone) {
                CreateAccountButton(
                    onClick = onCreateAccountClicked,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .fillMaxWidth()
                )
            }
            if (!signInButtonGone) {
                SignInButton(
                    onClick = onSignInClicked,
                    modifier = Modifier
                        .align(alignment = Alignment.CenterHorizontally)
                        .fillMaxWidth()
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
                        style = ProtonTheme.typography.defaultNorm
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
    )
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
            style = ProtonTheme.typography.defaultSmallStrongUnspecified,
            text = stringResource(id = R.string.auth_credentialless_settings_title)
        )
        Spacer(modifier = Modifier.height(ProtonDimens.ExtraSmallSpacing))
        Text(
            color = ProtonTheme.colors.textWeak,
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
    onClick: () -> Unit = { },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                vertical = ProtonDimens.DefaultSpacing,
                horizontal = ProtonDimens.DefaultSpacing
            ),
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
            state = AccountSettingsViewState.LoggedIn(
                UserId("test-user-id"),
                "SN",
                "Display Name",
                "email@proton.com"
            )
        )
    }
}
