/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.userrecovery.data.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.usecase.ObserveUser
import me.proton.core.userrecovery.domain.CanUserDeviceRecover
import me.proton.core.userrecovery.domain.IsDeviceRecoveryEnabled
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import javax.inject.Inject

class ObserveUserDeviceRecovery @Inject constructor(
    private val accountManager: AccountManager,
    private val isDeviceRecoveryEnabled: IsDeviceRecoveryEnabled,
    private val canUserDeviceRecover: CanUserDeviceRecover,
    private val observeUser: ObserveUser,
    private val observeUserSettings: ObserveUserSettings,
) {
    /**
     * Observes all Ready accounts.
     * If the flag [IsDeviceRecoveryEnabled] is disabled, no users are returned.
     * @return A pair of:
     *  - a user,
     *  - and a nullable boolean with
     *      the current value of [me.proton.core.usersettings.domain.entity.UserSettings.deviceRecovery].
     */
    operator fun invoke(): Flow<Pair<User, Boolean?>> = accountManager
        .getAccounts(AccountState.Ready)
        .flatMapLatest { accounts ->
            accounts.map { account ->
                observeUser(account.userId).filterNotNull().flatMapLatest { user ->
                    observeUserSettings(user.userId).map { userSettings ->
                        Pair(user, userSettings?.deviceRecovery)
                    }.distinctUntilChanged()
                }
            }.merge()
        }
        .filter { (user, _) ->
            isDeviceRecoveryEnabled(user.userId) && canUserDeviceRecover(user.userId)
        }
}
