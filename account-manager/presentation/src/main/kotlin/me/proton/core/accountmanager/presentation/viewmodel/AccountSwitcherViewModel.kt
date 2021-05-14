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

package me.proton.core.accountmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.isDisabled
import me.proton.core.account.domain.entity.isReady
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.accountmanager.presentation.entity.AccountItem
import me.proton.core.accountmanager.presentation.entity.AccountListItem
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.displayNameSplit
import java.util.Locale
import javax.inject.Inject

class AccountSwitcherViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val authOrchestrator: AuthOrchestrator,
    private val requiredAccountType: AccountType = AccountType.Internal
) : ViewModel() {

    val primaryAccount = accountManager.getPrimaryAccount()
        .mapLatest { account -> account?.getAccountItem() }
        .shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    val accounts = primaryAccount.combine(accountManager.getAccounts()) { primary, accounts -> primary to accounts }
        .mapLatest { (primary, accounts) ->
            accounts.mapNotNull {
                when {
                    primary?.userId == it.userId -> AccountListItem.Account.Primary(primary)
                    it.state == AccountState.Ready -> AccountListItem.Account.Ready(it.getAccountItem())
                    it.state == AccountState.Disabled -> AccountListItem.Account.Disabled(it.getAccountItem())
                    else -> null
                }
            }
        }

    private suspend fun Account.getAccountItem(): AccountItem {
        val user = runCatching { userManager.getUser(userId) }.getOrNull()

        val split = user?.displayNameSplit
        val letters = if (split?.firstName.isNullOrBlank() || split?.lastName.isNullOrBlank())
            username.take(2)
        else
            split?.firstName?.take(1) + split?.lastName?.take(1)

        return AccountItem(
            userId = userId,
            initials = letters.toUpperCase(Locale.getDefault()),
            name = user?.displayName ?: username,
            email = user?.email ?: email,
            state = state
        )
    }

    fun login() = viewModelScope.launch {
        authOrchestrator.startLoginWorkflow(
            requiredAccountType = requiredAccountType,
        )
    }

    fun logout(userId: UserId) = viewModelScope.launch {
        accountManager.disableAccount(userId)
    }

    fun switch(userId: UserId) = viewModelScope.launch {
        val account = accountManager.getAccount(userId).firstOrNull() ?: return@launch
        when {
            account.isDisabled() -> authOrchestrator.startLoginWorkflow(
                requiredAccountType = requiredAccountType,
                username = account.username
            )
            account.isReady() -> accountManager.setAsPrimary(userId)
        }
    }

    fun remove(userId: UserId) = viewModelScope.launch {
        accountManager.removeAccount(userId)
    }
}
