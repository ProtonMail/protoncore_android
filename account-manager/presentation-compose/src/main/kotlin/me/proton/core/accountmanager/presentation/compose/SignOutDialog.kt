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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.proton.core.compose.component.ProtonAlertDialog
import me.proton.core.compose.component.ProtonAlertDialogButton
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultStrongNorm

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SignOutDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onDisableAccount: () -> Unit,
    onRemoveAccount: () -> Unit,
) {
    var removeAccount by remember { mutableStateOf(false) }
    var signingOut by remember { mutableStateOf(false) }

    ProtonAlertDialog(
        modifier = modifier,
        title = stringResource(R.string.account_signout_dialog_title),
        text = {
            Column {
                ProtonAlertDialogText(R.string.account_signout_dialog_text)
                Spacer(Modifier.size(ProtonDimens.DefaultSpacing))
                Row(modifier = Modifier.clickable { removeAccount = !removeAccount }) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                        Checkbox(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            checked = removeAccount,
                            onCheckedChange = { removeAccount = !removeAccount }
                        )
                    }
                    Spacer(Modifier.size(ProtonDimens.DefaultSpacing))
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = stringResource(R.string.account_signout_dialog_delete_information),
                        style = ProtonTheme.typography.defaultStrongNorm,
                    )
                }
            }
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            ProtonAlertDialogButton(
                titleResId = R.string.account_signout_dialog_action_signout,
                loading = signingOut
            ) {
                signingOut = true
                if (removeAccount) onRemoveAccount() else onDisableAccount()
            }
        },
        dismissButton = {
            ProtonAlertDialogButton(R.string.account_signout_dialog_action_cancel) {
                onDismiss()
            }
        }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DeviceRecoveryDialogPreview() {
    ProtonTheme {
        SignOutDialog(
            onDismiss = {},
            onRemoveAccount = {},
            onDisableAccount = {}
        )
    }
}
