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

package me.proton.core.accountmanager.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import me.proton.core.accountmanager.domain.onSessionState

class AccountManagerObserver(
    private val scope: CoroutineScope,
    private val accountManager: AccountManager
) {

    internal fun addAccountStateListener(state: AccountState, initialState: Boolean, block: suspend (Account) -> Unit) {
        accountManager.onAccountState(state, initialState).onEach {
            // Launch a new Job to prevent listeners from creating a deadlock if they change state within the callback.
            scope.launch { block(it) }
        }.launchIn(scope)
    }

    internal fun addSessionStateListener(state: SessionState, initialState: Boolean, block: suspend (Account) -> Unit) {
        accountManager.onSessionState(state, initialState = initialState).onEach {
            // Launch a new Job to prevent listeners from creating a deadlock if they change state within the callback.
            scope.launch { block(it) }
        }.launchIn(scope)
    }
}

fun AccountManager.observe(scope: CoroutineScope) =
    AccountManagerObserver(scope, this)

fun AccountManagerObserver.onAccountTwoPassModeNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.TwoPassModeNeeded, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountTwoPassModeFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.TwoPassModeFailed, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountReady(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.Ready, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountDisabled(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.Disabled, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountRemoved(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.Removed, initialState, block)
    return this
}

fun AccountManagerObserver.onSessionHumanVerificationNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addSessionStateListener(SessionState.HumanVerificationNeeded, initialState, block)
    return this
}

fun AccountManagerObserver.onSessionHumanVerificationFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addSessionStateListener(SessionState.HumanVerificationFailed, initialState, block)
    return this
}

fun AccountManagerObserver.onSessionSecondFactorNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addSessionStateListener(SessionState.SecondFactorNeeded, initialState, block)
    return this
}

fun AccountManagerObserver.onSessionSecondFactorFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addSessionStateListener(SessionState.SecondFactorFailed, initialState, block)
    return this
}

fun AccountManagerObserver.onSessionAuthenticated(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addSessionStateListener(SessionState.Authenticated, initialState, block)
    return this
}

fun AccountManagerObserver.onSessionForceLogout(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addSessionStateListener(SessionState.ForceLogout, initialState, block)
    return this
}
