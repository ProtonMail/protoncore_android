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

package me.proton.core.auth.presentation.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject

@HiltViewModel
class CancelCreateAccountDialogViewModel @Inject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler,
    private val accountManager: AccountManager,
    private val scopeProvider: CoroutineScopeProvider
) : ProtonViewModel() {

    fun cancelCreation() = scopeProvider.GlobalDefaultSupervisedScope.launch {
        accountManager.getAccounts(AccountState.CreateAccountNeeded).first().firstOrNull()?.let {
            accountWorkflowHandler.handleCreateAccountFailed(it.userId)
            accountManager.disableAccount(it.userId, keepSession = true)
        }
    }
}
