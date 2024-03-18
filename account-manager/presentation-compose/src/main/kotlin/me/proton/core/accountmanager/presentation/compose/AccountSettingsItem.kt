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

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewModel
import me.proton.core.accountmanager.presentation.compose.viewmodel.AccountSettingsViewState
import me.proton.core.compose.component.ProtonSettingsItem
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.viewmodel.hiltViewModelOrNull

@Composable
fun AccountSettingsItem(
    modifier: Modifier = Modifier,
    viewModel: AccountSettingsViewModel? = hiltViewModelOrNull(),
    onClick: () -> Unit
) {
    val state = when (viewModel) {
        null -> AccountSettingsViewState.Null
        else -> viewModel.state.collectAsStateWithLifecycle().value
    }
    AccountSettingsItem(modifier, state, onClick)
}

@Composable
fun AccountSettingsItem(
    modifier: Modifier = Modifier,
    state: AccountSettingsViewState,
    onClick: () -> Unit
) {
    when (state) {
        is AccountSettingsViewState.Hidden -> Unit
        is AccountSettingsViewState.CredentialLess -> Unit
        is AccountSettingsViewState.LoggedIn -> AccountSettingsItem(
            modifier = modifier,
            header = state.displayName ?: stringResource(R.string.account_settings_item_header),
            hint = state.email,
            onClick = onClick
        )
    }
}

@Composable
fun AccountSettingsItem(
    modifier: Modifier = Modifier,
    header: String,
    hint: String?,
    onClick: () -> Unit
) {
    ProtonSettingsItem(
        modifier = modifier,
        name = header,
        hint = hint,
        onClick = onClick
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewAccountPrimarySettingsItem() {
    ProtonTheme {
        AccountSettingsItem(
            header = "Eric Norbert",
            hint = "eric.norbert@proton.me",
            onClick = {}
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = false)
@Composable
internal fun PreviewAccountPrimarySettingsItemNoEmail() {
    ProtonTheme {
        AccountSettingsItem(
            header = "Eric Norbert",
            hint = null,
            onClick = {}
        )
    }
}
