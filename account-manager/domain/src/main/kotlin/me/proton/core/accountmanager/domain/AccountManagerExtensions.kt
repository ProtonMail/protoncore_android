/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.accountmanager.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState

/**
 * Flow of Account where [Account.state] equals [state].
 *
 * @param initialState if true (default), initial state for all accounts in this [state] will be raised on subscription.
 */
fun AccountManager.onAccountState(vararg state: AccountState, initialState: Boolean = true): Flow<Account> =
    onAccountStateChanged(initialState).filter { state.contains(it.state) }

/**
 * Flow of Account where [Account.sessionState] equals [state].
 *
 * @param initialState if true (default), initial state for all accounts in this [state] will be raised on subscription.
 */
fun AccountManager.onSessionState(vararg state: SessionState, initialState: Boolean = true): Flow<Account> =
    onSessionStateChanged(initialState).filter { state.contains(it.sessionState) }

/**
 * Flow of primary Account.
 *
 * @see [AccountManager.getPrimaryUserId]
 */
fun AccountManager.getPrimaryAccount(): Flow<Account?> =
    getPrimaryUserId().flatMapLatest { userId ->
        userId?.let { getAccount(it) } ?: flowOf(null)
    }

/**
 * Flow of [List] of Accounts filtered by [Account.state].
 */
fun AccountManager.getAccounts(vararg state: AccountState): Flow<List<Account>> =
    getAccounts().mapLatest { list -> list.filter { it.state in state } }.distinctUntilChanged()
