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

package me.proton.core.plan.presentation.compose.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.component.protonButtonColors
import me.proton.core.compose.flow.rememberAsState
import me.proton.core.compose.theme.ProtonColors
import me.proton.core.compose.theme.ProtonDimens.DefaultIconSize
import me.proton.core.compose.theme.ProtonDimens.ExtraSmallIconSize
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.viewmodel.hiltViewModelOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.compose.viewmodel.AccountStorageState
import me.proton.core.plan.presentation.compose.viewmodel.UpgradeStorageInfoViewModel
import me.proton.core.plan.presentation.compose.viewmodel.UpgradeStorageInfoViewModel.Companion.INITIAL_STATE

/** Displays information that storage is (nearly) full.
 * Only applies to users with free plan.
 * If storage is still available, or if the user is on a paid plan,
 * this view will not display anything.
 */
@Composable
public fun UpgradeStorageInfo(
    onUpgradeClicked: (UserId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UpgradeStorageInfoViewModel? = hiltViewModelOrNull(),
    withTopDivider: Boolean = false,
    withBottomDivider: Boolean = false,
) {
    if (viewModel == null) return
    val state by rememberAsState(flow = viewModel.state, initial = INITIAL_STATE)
    val userId = (state as? AccountStorageState.HighStorageUsage)?.userId

    if (state !is AccountStorageState.Hidden) {
        Column {
            if (withTopDivider) {
                Divider(color = ProtonTheme.colors.separatorNorm)
            }
            UpgradeStorageInfo(
                onUpgradeClicked = if (userId != null) {
                    { onUpgradeClicked(userId) }
                } else {
                    { Unit }
                },
                title = state.getTitle(),
                modifier = modifier
            )
            if (withBottomDivider) {
                Divider(color = ProtonTheme.colors.separatorNorm)
            }
        }
    }
}

@Composable
public fun UpgradeStorageInfo(
    onUpgradeClicked: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(
            horizontal = dimensionResource(id = R.dimen.gap_large),
            vertical = dimensionResource(id = R.dimen.gap_medium_plus)
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StackedIcons()
        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.gap_medium_plus)))
        StorageInfoText(title)
        Spacer(modifier = Modifier.weight(1.0f))
        UpgradeButton(onClick = { onUpgradeClicked() })
    }
}

@Composable
private fun StackedIcons(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Image(
            colorFilter = ColorFilter.tint(ProtonTheme.colors.iconNorm),
            contentDescription = null,
            modifier = Modifier.size(DefaultIconSize),
            painter = painterResource(id = R.drawable.ic_proton_cloud),
        )
        Image(
            colorFilter = ColorFilter.tint(ProtonTheme.colors.notificationError),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(ExtraSmallIconSize),
            painter = painterResource(id = R.drawable.ic_proton_exclamation_circle_filled),
        )
    }
}

@Composable
private fun StorageInfoText(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            color = ProtonTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultNorm,
            text = title
        )
        Text(
            color = ProtonTheme.colors.textWeak,
            style = ProtonTheme.typography.captionNorm,
            text = stringResource(id = R.string.upgrade_storage_subtitle)
        )
    }
}

@Composable
private fun UpgradeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ProtonSolidButton(
        colors = ButtonDefaults.protonButtonColors(
            backgroundColor = ProtonTheme.colors.interactionWeakNorm
        ),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            color = ProtonTheme.colors.textInverted,
            text = stringResource(id = R.string.upgrade_storage_cta_button)
        )
    }
}

@Composable
private fun AccountStorageState.getTitle(): String = when (this) {
    is AccountStorageState.HighStorageUsage.Drive -> stringResource(
        R.string.upgrade_storage_current_drive_storage,
        percentage
    )

    is AccountStorageState.HighStorageUsage.Mail -> stringResource(
        R.string.upgrade_storage_current_mail_storage,
        percentage
    )

    is AccountStorageState.Hidden -> ""
}

// region Previews

@Preview(name = "View is hidden")
@Composable
internal fun PreviewHidden() {
    ProtonTheme(colors = ProtonColors.Light.sidebarColors!!) {
        UpgradeStorageInfo(onUpgradeClicked = {})
    }
}

@Preview(name = "Drive storage")
@Composable
internal fun PreviewHighDriveUsage() {
    ProtonTheme(colors = ProtonColors.Light.sidebarColors!!) {
        UpgradeStorageInfo(
            onUpgradeClicked = {},
            title = "Drive storage: 90% full"
        )
    }
}

@Preview(name = "Mail storage")
@Composable
internal fun PreviewHighMailUsage() {
    ProtonTheme(colors = ProtonColors.Light.sidebarColors!!) {
        UpgradeStorageInfo(
            onUpgradeClicked = {},
            title = "Drive storage: 100% full"
        )
    }
}

// endregion
