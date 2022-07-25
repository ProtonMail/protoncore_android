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

package me.proton.core.accountmanager.data.job

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.accountmanager.data.AccountStateHandler
import me.proton.core.accountmanager.domain.getAccounts

fun AccountStateHandler.disableInitialNotReadyAccounts() = scopeProvider.GlobalDefaultSupervisedScope.launch {
    // For all NotReady/Removed Accounts in the first/initial list.
    accountManager.getAccounts(AccountState.NotReady, AccountState.Removed).first().forEach {
        // Do not disable if SecondFactor is pending.
        if (it.sessionState != SessionState.SecondFactorNeeded) {
            accountManager.disableAccount(it.userId)
        }
    }
}
