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

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.onAccountState
import me.proton.core.accountmanager.domain.onSessionState

class AccountManagerObserver(
    internal val accountManager: AccountManager,
    internal val lifecycle: Lifecycle,
    internal val minActiveState: Lifecycle.State = Lifecycle.State.CREATED
) {

    internal val scope = lifecycle.coroutineScope

    internal fun addAccountStateListener(state: AccountState, initialState: Boolean, block: suspend (Account) -> Unit) {
        accountManager.onAccountState(state, initialState = initialState)
            .flowWithLifecycle(lifecycle, minActiveState)
            .onEach { block(it) }
            .launchIn(scope)
    }

    internal fun addSessionStateListener(state: SessionState, initialState: Boolean, block: suspend (Account) -> Unit) {
        accountManager.onSessionState(state, initialState = initialState)
            .flowWithLifecycle(lifecycle, minActiveState)
            .onEach { block(it) }
            .launchIn(scope)
    }
}

fun AccountManager.observe(
    lifecycle: Lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.CREATED
) = AccountManagerObserver(this, lifecycle, minActiveState)

fun AccountManagerObserver.onAccountMigrationNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.MigrationNeeded, initialState, block)
    return this
}

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

fun AccountManagerObserver.onAccountCreateAddressNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.CreateAddressNeeded, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountCreateAddressFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.CreateAddressFailed, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountCreateAccountNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.CreateAccountNeeded, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountCreateAccountFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.CreateAccountFailed, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountDeviceSecretNeeded(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.DeviceSecretNeeded, initialState, block)
    return this
}

fun AccountManagerObserver.onAccountDeviceSecretFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.DeviceSecretFailed, initialState, block)
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

fun AccountManagerObserver.onUserKeyCheckFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.UserKeyCheckFailed, initialState, block)
    return this
}

fun AccountManagerObserver.onUserAddressKeyCheckFailed(
    initialState: Boolean = true,
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    addAccountStateListener(AccountState.UserAddressKeyCheckFailed, initialState, block)
    return this
}
