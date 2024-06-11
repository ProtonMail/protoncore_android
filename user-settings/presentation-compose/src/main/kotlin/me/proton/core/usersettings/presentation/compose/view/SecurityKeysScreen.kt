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

package me.proton.core.usersettings.presentation.compose.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.component.ProtonSettingsTopBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionNorm
import me.proton.core.compose.theme.defaultSmallStrongUnspecified
import me.proton.core.usersettings.presentation.compose.R

@Composable
fun SecurityKeysScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsTopBar(
                modifier = Modifier.statusBarsPadding(),
                title = stringResource(R.string.settings_security_keys_title),
                onBackClick = onBackClick
            )
        },
        content = { paddingValues ->
            SecurityKeysList(modifier = Modifier.padding(paddingValues))
        }
    )
}

@Composable
fun SecurityKeysListHeader() {
    Text(
        text = stringResource(id = R.string.settings_security_keys_registered),
        color = ProtonTheme.colors.textNorm,
        style = ProtonTheme.typography.defaultSmallStrongUnspecified,
    )
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
}

@Composable
fun SecurityKeysListFooter() {
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
    Text(
        text = stringResource(id = R.string.settings_manage_security_keys),
        color = ProtonTheme.colors.textNorm,
        style = ProtonTheme.typography.captionNorm,
    )
}

@Preview
@Composable
private fun SecurityKeysScreenPreview() {
    ProtonTheme {
        SecurityKeysScreen(
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun SecurityKeysFooterPreview() {
    ProtonTheme {
        SecurityKeysListFooter()
    }
}

@Preview
@Composable
private fun SecurityKeysHeaderPreview() {
    ProtonTheme {
        SecurityKeysListHeader()
    }
}
