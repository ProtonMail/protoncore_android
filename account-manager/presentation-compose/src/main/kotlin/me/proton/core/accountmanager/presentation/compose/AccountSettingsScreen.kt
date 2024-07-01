/*
 * Copyright (c) 2024 Proton AG
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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.component.ProtonSettingsTopBar
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun AccountSettingsScreen(
    modifier: Modifier = Modifier,
    onPasswordManagementClick: () -> Unit,
    onRecoveryEmailClick: () -> Unit,
    onSecurityKeysClick: () -> Unit,
    onBackClick: () -> Unit,
    divider: @Composable () -> Unit = { Divider() }
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProtonSettingsTopBar(
                modifier = Modifier.statusBarsPadding(),
                title = stringResource(R.string.account_settings_header),
                onBackClick = onBackClick
            )
        },
        content = { paddingValues ->
            AccountSettingsList(
                modifier = Modifier.padding(paddingValues),
                onPasswordManagementClick = onPasswordManagementClick,
                onRecoveryEmailClick = onRecoveryEmailClick,
                onSecurityKeysClick = onSecurityKeysClick,
                divider = divider
            )
        }
    )
}

@Preview
@Composable
private fun AccountSettingsScreenPreview() {
    ProtonTheme {
        AccountSettingsScreen(
            onPasswordManagementClick = {},
            onRecoveryEmailClick = {},
            onSecurityKeysClick = {},
            onBackClick = {}
        )
    }
}
