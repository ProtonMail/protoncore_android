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
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.util.kotlin.exhaustive

class AccountManagerObserver(
    scope: CoroutineScope,
    accountManager: AccountManager
) {

    private var onAccountTwoPassModeNeededListener: suspend (Account) -> Unit = {}
    private var onAccountTwoPassModeFailedListener: suspend (Account) -> Unit = {}
    private var onAccountReadyListener: suspend (Account) -> Unit = {}
    private var onAccountDisabledListener: suspend (Account) -> Unit = {}
    private var onAccountRemovedListener: suspend (Account) -> Unit = {}

    private var onSessionHumanVerificationNeededListener: suspend (Account) -> Unit = {}
    private var onSessionHumanVerificationFailedListener: suspend (Account) -> Unit = {}
    private var onSessionAuthenticatedListener: suspend (Account) -> Unit = {}
    private var onSessionForceLogoutListener: suspend (Account) -> Unit = {}

    init {
        accountManager.onAccountStateChanged().onEach {
            when (it.state) {
                AccountState.NotReady,
                AccountState.TwoPassModeSuccess -> Unit // Hide those states.
                AccountState.TwoPassModeNeeded -> onAccountTwoPassModeNeededListener.invoke(it)
                AccountState.TwoPassModeFailed -> onAccountTwoPassModeFailedListener.invoke(it)
                AccountState.Ready -> onAccountReadyListener.invoke(it)
                AccountState.Disabled -> onAccountDisabledListener.invoke(it)
                AccountState.Removed -> onAccountRemovedListener.invoke(it)
            }.exhaustive
        }.launchIn(scope)

        accountManager.onSessionStateChanged().onEach {
            when (it.sessionState) {
                null -> Unit // Nothing to do.
                SessionState.SecondFactorNeeded,
                SessionState.SecondFactorSuccess,
                SessionState.SecondFactorFailed,
                SessionState.HumanVerificationSuccess -> Unit // Hide those states.
                SessionState.HumanVerificationNeeded -> onSessionHumanVerificationNeededListener.invoke(it)
                SessionState.HumanVerificationFailed -> onSessionHumanVerificationFailedListener.invoke(it)
                SessionState.Authenticated -> onSessionAuthenticatedListener.invoke(it)
                SessionState.ForceLogout -> onSessionForceLogoutListener.invoke(it)
            }.exhaustive
        }.launchIn(scope)
    }

    internal fun setOnAccountTwoPassModeNeeded(block: suspend (Account) -> Unit) {
        onAccountTwoPassModeNeededListener = block
    }

    internal fun setOnAccountTwoPassModeFailed(block: suspend (Account) -> Unit) {
        onAccountTwoPassModeFailedListener = block
    }

    internal fun setOnAccountReady(block: suspend (Account) -> Unit) {
        onAccountReadyListener = block
    }

    internal fun setOnAccountDisabled(block: suspend (Account) -> Unit) {
        onAccountDisabledListener = block
    }

    internal fun setOnAccountRemoved(block: suspend (Account) -> Unit) {
        onAccountRemovedListener = block
    }

    internal fun setOnSessionHumanVerificationNeeded(block: suspend (Account) -> Unit) {
        onSessionHumanVerificationNeededListener = block
    }

    internal fun setOnSessionHumanVerificationFailed(block: suspend (Account) -> Unit) {
        onSessionHumanVerificationFailedListener = block
    }

    internal fun setOnSessionAuthenticated(block: suspend (Account) -> Unit) {
        onSessionAuthenticatedListener = block
    }

    internal fun setOnSessionForceLogout(block: suspend (Account) -> Unit) {
        onSessionForceLogoutListener = block
    }
}

fun AccountManager.observe(scope: CoroutineScope) =
    AccountManagerObserver(scope, this)

fun AccountManagerObserver.onAccountTwoPassModeNeeded(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnAccountTwoPassModeNeeded { block(it) }
    return this
}

fun AccountManagerObserver.onAccountTwoPassModeFailed(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnAccountTwoPassModeFailed { block(it) }
    return this
}

fun AccountManagerObserver.onAccountReady(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnAccountReady { block(it) }
    return this
}

fun AccountManagerObserver.onAccountDisabled(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnAccountDisabled { block(it) }
    return this
}

fun AccountManagerObserver.onAccountRemoved(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnAccountRemoved { block(it) }
    return this
}

fun AccountManagerObserver.onSessionHumanVerificationNeeded(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnSessionHumanVerificationNeeded { block(it) }
    return this
}

fun AccountManagerObserver.onSessionHumanVerificationFailed(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnSessionHumanVerificationFailed { block(it) }
    return this
}

fun AccountManagerObserver.onSessionAuthenticated(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnSessionAuthenticated { block(it) }
    return this
}

fun AccountManagerObserver.onSessionForceLogout(
    block: suspend (Account) -> Unit
): AccountManagerObserver {
    setOnSessionForceLogout { block(it) }
    return this
}
