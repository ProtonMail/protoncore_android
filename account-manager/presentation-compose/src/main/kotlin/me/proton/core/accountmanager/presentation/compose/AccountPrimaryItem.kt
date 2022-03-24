/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.presentation.view.AccountPrimaryView
import me.proton.core.accountmanager.presentation.viewmodel.AccountSwitcherViewModel
import me.proton.core.domain.entity.UserId

@Composable
fun AccountPrimaryItem(
    onSignIn: (UserId?) -> Unit,
    onSignOut: (UserId) -> Unit,
    onRemove: (UserId) -> Unit,
    onSwitch: (UserId) -> Unit,
    modifier: Modifier = Modifier,
    viewState: AccountPrimaryState = rememberAccountPrimaryState(),
    viewModel: AccountSwitcherViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel) {
        viewModel.onAction().onEach {
            when (it) {
                is AccountSwitcherViewModel.Action.Add -> onSignIn(null)
                is AccountSwitcherViewModel.Action.SignIn -> onSignIn(it.account.userId)
                is AccountSwitcherViewModel.Action.SignOut -> onSignOut(it.account.userId)
                is AccountSwitcherViewModel.Action.Remove -> onRemove(it.account.userId)
                is AccountSwitcherViewModel.Action.SetPrimary -> onSwitch(it.account.userId)
            }
        }.launchIn(this)
    }

    AndroidView(
        factory = { context ->
            AccountPrimaryView(context).also { view ->
                view.isDialogEnabled = viewState.isDialogEnabled
                view.setViewModel(viewModel)
                view.setOnViewClicked {
                    if (viewState.isDialogEnabled) {
                        viewState.showDialog()
                    }
                }
                view.setOnDialogShown { viewState.isDialogShowing = true }
                view.setOnDialogDismissed { viewState.isDialogShowing = false }
            }
        },
        modifier = modifier,
        update = { view ->
            view.isDialogEnabled = viewState.isDialogEnabled
            if (viewState.isDialogShowing) {
                view.showDialog()
            } else {
                view.dismissDialog()
            }
        }
    )
}
