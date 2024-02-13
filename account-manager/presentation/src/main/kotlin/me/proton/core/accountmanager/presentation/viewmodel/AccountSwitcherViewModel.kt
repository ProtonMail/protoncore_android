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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
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
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.getInitials
import me.proton.core.util.kotlin.takeIfNotBlank
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountSwitcherViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val requiredAccountType: AccountType = AccountType.Internal
) : ViewModel() {

    sealed class Action {
        object Add : Action()
        data class SignIn(val account: Account) : Action()
        data class SignOut(val account: Account) : Action()
        data class SetPrimary(val account: Account) : Action()
        data class Remove(val account: Account) : Action()
    }

    private val onActionMutable = MutableSharedFlow<Action>(extraBufferCapacity = 1)

    private val accountItems = accountManager.getAccounts()
        .flatMapLatest { accounts -> combine(accounts.map { it.getAccountItem() }) { it.toList() } }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val primaryAccount = accountManager.getPrimaryAccount()
        .flatMapLatest { account -> account?.getAccountItem() ?: flowOf(null) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val accounts = primaryAccount.combine(accountItems) { primary, accounts -> primary to accounts }
        .mapLatest { (primary, accounts) ->
            accounts.mapNotNull {
                when {
                    primary?.userId == it.userId -> AccountListItem.Account.Primary(primary)
                    it.state == AccountState.Ready -> AccountListItem.Account.Ready(it)
                    it.state == AccountState.Disabled -> AccountListItem.Account.Disabled(it)
                    else -> null
                }
            }
        }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private fun Account.getAccountItem(): Flow<AccountItem> =
        userManager.observeUser(userId)
            .mapLatest { user -> getAccountItem(user) }

    private fun Account.getAccountItem(user: User?): AccountItem {
        val initials = user?.displayName?.takeIfNotBlank() ?: user?.email?.takeIfNotBlank() ?: username
        return AccountItem(
            userId = userId,
            initials = user?.getInitials(initialsCount = 1) ?: "?",
            name = user?.displayName?.takeIfNotBlank() ?: username ?: "unknown",
            email = user?.email?.takeIfNotBlank() ?: email,
            state = state
        )
    }

    private suspend fun getAccountOrNull(userId: UserId): Account? =
        accountManager.getAccount(userId).firstOrNull()

    fun add() = viewModelScope.launch {
        onActionMutable.emit(Action.Add)
    }

    fun signOut(userId: UserId) = viewModelScope.launch {
        getAccountOrNull(userId)?.let {
            onActionMutable.emit(Action.SignOut(it))
        }
    }

    fun switch(userId: UserId) = viewModelScope.launch {
        getAccountOrNull(userId)?.let {
            when {
                it.isDisabled() -> onActionMutable.emit(Action.SignIn(it))
                it.isReady() -> onActionMutable.emit(Action.SetPrimary(it))
            }
        }
    }

    fun remove(userId: UserId) = viewModelScope.launch {
        getAccountOrNull(userId)?.let {
            onActionMutable.emit(Action.Remove(it))
        }
    }

    fun onAction() = onActionMutable.asSharedFlow()

    fun onDefaultAction(authOrchestrator: AuthOrchestrator) = onAction().onEach {
        when (it) {
            is Action.Add -> authOrchestrator.startLoginWorkflow(requiredAccountType)
            is Action.SignIn -> authOrchestrator.startLoginWorkflow(requiredAccountType, username = it.account.username)
            is Action.SignOut -> accountManager.disableAccount(it.account.userId)
            is Action.Remove -> accountManager.removeAccount(it.account.userId)
            is Action.SetPrimary -> accountManager.setAsPrimary(it.account.userId)
        }
    }
}
