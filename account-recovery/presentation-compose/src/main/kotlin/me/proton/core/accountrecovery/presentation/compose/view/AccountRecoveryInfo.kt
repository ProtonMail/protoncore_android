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

package me.proton.core.accountrecovery.presentation.compose.view

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.accountrecovery.presentation.compose.R
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryInfoViewModel
import me.proton.core.accountrecovery.presentation.compose.viewmodel.AccountRecoveryInfoViewState
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.user.domain.entity.UserRecovery

@Composable
fun AccountRecoveryInfo(
    modifier: Modifier = Modifier,
    viewModel: AccountRecoveryInfoViewModel? = hiltViewModelOrNull(),
    onOpenDialog: () -> Unit = { },
    quickAction: Boolean = true,
    expanded: Boolean = false,
) {
    val state = when (viewModel) {
        null -> AccountRecoveryInfoViewState.None
        else -> viewModel.state.collectAsStateWithLifecycle().value
    }

    AccountRecoveryInfo(
        modifier = modifier,
        state = state,
        onOpenDialog = onOpenDialog,
        quickAction = quickAction,
        expanded = expanded
    )
}

@Composable
fun AccountRecoveryInfo(
    modifier: Modifier = Modifier,
    state: AccountRecoveryInfoViewState,
    onOpenDialog: () -> Unit = { },
    quickAction: Boolean = true,
    expanded: Boolean = false
) {
    when (state) {
        is AccountRecoveryInfoViewState.None -> Unit
        is AccountRecoveryInfoViewState.Recovery -> AccountRecoveryInfo(
            modifier = modifier,
            state = state,
            onOpenDialog = onOpenDialog,
            quickAction = quickAction,
            expanded = expanded
        )
    }
}

@Composable
fun AccountRecoveryInfo(
    modifier: Modifier = Modifier,
    state: AccountRecoveryInfoViewState.Recovery,
    onOpenDialog: () -> Unit = { },
    quickAction: Boolean = true,
    expanded: Boolean = false
) {
    when (state.recoveryState) {
        null -> return
        UserRecovery.State.None -> return
        UserRecovery.State.Expired -> return
        UserRecovery.State.Grace -> AccountRecoveryInfoGrace(
            modifier = modifier,
            state = state,
            onOpenDialog = onOpenDialog,
            expanded = expanded
        )

        UserRecovery.State.Cancelled -> AccountRecoveryInfoCancelled(modifier, state, expanded)
        UserRecovery.State.Insecure -> AccountRecoveryInfoInsecure(
            modifier = modifier,
            state = state,
            onOpenDialog = onOpenDialog,
            quickAction = quickAction,
            expanded = expanded
        )
    }
}

@Composable
fun AccountRecoveryInfoGrace(
    modifier: Modifier = Modifier,
    state: AccountRecoveryInfoViewState.Recovery,
    onOpenDialog: () -> Unit = { },
    quickAction: Boolean = true,
    expanded: Boolean = false,
) {
    AccountRecoveryInfo(
        modifier = modifier,
        title = stringResource(R.string.account_recovery_info_grace_title),
        subtitle = stringResource(
            R.string.account_recovery_info_grace_subtitle,
            state.durationUntilEnd
        ),
        icon = R.drawable.ic_recovery_pending,
        description = stringResource(R.string.account_recovery_info_grace_description),
        onAction = onOpenDialog,
        action = stringResource(R.string.account_recovery_cancel),
        onCardClick = onOpenDialog,
        quickAction = quickAction,
        expanded = expanded
    )
}

@Composable
fun AccountRecoveryInfoCancelled(
    modifier: Modifier = Modifier,
    state: AccountRecoveryInfoViewState.Recovery,
    quickAction: Boolean = true,
    expanded: Boolean = false,
) {
    AccountRecoveryInfo(
        modifier = modifier,
        title = stringResource(R.string.account_recovery_info_cancelled_title),
        subtitle = stringResource(
            R.string.account_recovery_info_cancelled_subtitle,
            state.startDate
        ),
        icon = R.drawable.ic_recovery_cancelled,
        description = stringResource(R.string.account_recovery_info_cancelled_description),
        quickAction = quickAction,
        expanded = expanded
    )
}

@Composable
fun AccountRecoveryInfoInsecure(
    modifier: Modifier = Modifier,
    state: AccountRecoveryInfoViewState.Recovery,
    onOpenDialog: () -> Unit = { },
    quickAction: Boolean = true,
    expanded: Boolean = false,
) {
    AccountRecoveryInfo(
        modifier = modifier,
        title = stringResource(R.string.account_recovery_info_insecure_title),
        subtitle = stringResource(R.string.account_recovery_info_insecure_subtitle, state.endDate),
        icon = R.drawable.ic_recovery_unsecure,
        description = stringResource(R.string.account_recovery_info_insecure_description),
        onAction = onOpenDialog,
        action = stringResource(R.string.account_recovery_cancel),
        onCardClick = onOpenDialog,
        quickAction = quickAction,
        expanded = expanded
    )
}

@Composable
fun AccountRecoveryInfo(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    description: String,
    onAction: () -> Unit = { },
    action: String? = null,
    onCardClick: () -> Unit = { },
    quickAction: Boolean = true,
    expanded: Boolean = true,
) {
    Column(modifier = modifier) {
        Card(
            onClick = onCardClick,
            colors = CardDefaults.cardColors(
                containerColor = ProtonTheme.colors.backgroundSecondary,
                contentColor = ProtonTheme.colors.textNorm,
            )
        ) {
            Row(modifier = Modifier.padding(ProtonDimens.DefaultSpacing)) {
                Icon(
                    modifier = Modifier.size(ProtonDimens.DefaultIconWithPadding),
                    contentDescription = title,
                    painter = painterResource(icon),
                    tint = Color.Companion.Unspecified
                )
                Spacer(modifier = Modifier.padding(start = ProtonDimens.DefaultSpacing))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = ProtonTheme.typography.defaultNorm
                    )
                    Spacer(modifier = Modifier.padding(vertical = ProtonDimens.ExtraSmallSpacing))
                    Text(
                        text = subtitle,
                        textAlign = TextAlign.Left,
                        style = ProtonTheme.typography.defaultWeak
                    )
                    if (!expanded && action != null && quickAction) {
                        Spacer(modifier = Modifier.padding(vertical = ProtonDimens.SmallSpacing))
                        Text(text = action, color = ProtonTheme.colors.interactionNorm)
                    }
                }
            }
        }
        if (expanded) {
            Text(
                modifier = Modifier.padding(vertical = ProtonDimens.DefaultSpacing),
                text = description,
                style = ProtonTheme.typography.defaultWeak
            )
            if (action != null) {
                ProtonSolidButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAction,
                ) {
                    Text(
                        modifier = Modifier.padding(ProtonDimens.SmallSpacing),
                        text = action
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccountRecoveryInfoGracePreview() {
    ProtonTheme {
        AccountRecoveryInfoGrace(
            state = AccountRecoveryInfoViewState.Recovery(
                recoveryState = UserRecovery.State.Grace,
                startDate = "12 August",
                endDate = "14 August",
                durationUntilEnd = "48 hours",
            ),
            expanded = false
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AccountRecoveryInfoInsecurePreview() {
    ProtonTheme {
        AccountRecoveryInfoInsecure(
            state = AccountRecoveryInfoViewState.Recovery(
                recoveryState = UserRecovery.State.Insecure,
                startDate = "12 August",
                endDate = "14 August",
                durationUntilEnd = "48 hours",
            )
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AccountRecoveryInfoCancelledPreview() {
    ProtonTheme {
        AccountRecoveryInfoCancelled(
            state = AccountRecoveryInfoViewState.Recovery(
                recoveryState = UserRecovery.State.Cancelled,
                startDate = "12 August",
                endDate = "14 August",
                durationUntilEnd = "48 hours",
            )
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AccountRecoveryInfoCancelledNoDescriptionPreview() {
    ProtonTheme {
        AccountRecoveryInfoCancelled(
            state = AccountRecoveryInfoViewState.Recovery(
                recoveryState = UserRecovery.State.Cancelled,
                startDate = "12 August",
                endDate = "14 August",
                durationUntilEnd = "48 hours",
            ),
            expanded = false
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun AccountRecoveryInfoExpandedPreview() {
    ProtonTheme {
        AccountRecoveryInfoGrace(
            state = AccountRecoveryInfoViewState.Recovery(
                recoveryState = UserRecovery.State.Grace,
                startDate = "12 August",
                endDate = "14 August",
                durationUntilEnd = "48 hours",
            ),
            quickAction = false,
            expanded = true
        )
    }
}
