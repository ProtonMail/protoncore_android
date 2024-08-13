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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.component.HyperlinkText
import me.proton.core.compose.component.ProtonSettingsTopBar
import me.proton.core.compose.component.ProtonSolidButton
import me.proton.core.compose.theme.LocalTypography
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.usersettings.presentation.compose.R

@Composable
fun SecurityKeysScreen(
    modifier: Modifier = Modifier,
    onManageSecurityKeysClicked: () -> Unit,
    onAddSecurityKeyClicked: () -> Unit,
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
            SecurityKeysList(
                modifier = Modifier.padding(paddingValues),
                onManageSecurityKeysClicked = onManageSecurityKeysClicked,
                onAddSecurityKeyClicked = onAddSecurityKeyClicked,
            )
        }
    )
}

@Composable
fun SecurityKeysEmptyListHeader() {
    LearnMoreText(text = R.string.settings_security_keys_empty_list)
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
}

@Composable
fun SecurityKeysListHeader() {
    Text(
        text = stringResource(id = R.string.settings_security_keys_registered),
        style = LocalTypography.current.body2Regular
    )
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
}

@Composable
fun SecurityKeysListFooter(
    onManageSecurityKeysClicked: () -> Unit
) {
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_large_plus)))
    LearnMoreText(text = R.string.settings_manage_security_keys_info)
    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.gap_medium_plus)))
    ProtonSolidButton(
        contained = false,
        onClick = onManageSecurityKeysClicked,
        modifier = Modifier
            .padding(top = ProtonDimens.MediumSpacing)
            .height(ProtonDimens.DefaultButtonMinHeight)
    ) {
        Text(text = stringResource(R.string.settings_manage_security_keys))
    }
}

@Composable
fun SecurityKeysEmptyListFooter(
    onAddSecurityKeyClicked: () -> Unit
) {
    ProtonSolidButton(
        contained = false,
        onClick = onAddSecurityKeyClicked,
        modifier = Modifier
            .padding(top = ProtonDimens.MediumSpacing)
            .height(ProtonDimens.DefaultButtonMinHeight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.settings_add_security_key))
            Icon(
                painter = painterResource(id = R.drawable.ic_proton_arrow_out_square),
                contentDescription = null
            )
        }
    }
}

@Composable
fun LearnMoreText(
    @StringRes text: Int,
) {
    HyperlinkText(
        fullText = stringResource(
            id = text,
            stringResource(id = R.string.settings_security_keys_learn_more)
        ),
        hyperLinks = mutableMapOf(stringResource(id = R.string.settings_security_keys_learn_more) to stringResource(id = R.string.security_keys_learn_more_link)),
        textStyle = LocalTypography.current.body2Regular
    )
}

@Preview
@Composable
private fun SecurityKeysScreenPreview() {
    ProtonTheme {
        SecurityKeysScreen(
            onManageSecurityKeysClicked = {},
            onAddSecurityKeyClicked = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun SecurityKeysFooterPreview() {
    ProtonTheme {
        SecurityKeysListFooter(onManageSecurityKeysClicked = {})
    }
}

@Preview
@Composable
private fun SecurityKeysEmptyListFooterPreview() {
    ProtonTheme {
        SecurityKeysEmptyListFooter(onAddSecurityKeyClicked = {})
    }
}

@Preview
@Composable
private fun SecurityKeysHeaderPreview() {
    ProtonTheme {
        SecurityKeysListHeader()
    }
}

@Preview
@Composable
private fun SecurityKeysEmptyListHeaderPreview() {
    ProtonTheme {
        SecurityKeysEmptyListHeader()
    }
}
