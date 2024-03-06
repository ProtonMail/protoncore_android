/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.usersettings.domain

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.arch.mapSuccessValueOrNull
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.usecase.ObserveUserSettings
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersSettingsHandler @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val accountManager: AccountManager,
    private val observeUserSettings: ObserveUserSettings,
) {
    fun <T> onUsersSettingsChanged(
        merge: suspend (List<UserSettings?>) -> T,
        block: suspend (T) -> Unit,
    ) = accountManager.getAccounts(AccountState.Ready).map { accounts ->
        accounts.map { account -> observeUserSettings(account.userId) }
    }.flatMapLatest { usersSettingsFlow ->
        combine(usersSettingsFlow) { usersSettings ->
            merge(usersSettings.toList())
        }
    }
        .onEach { block(it) }
        .catch { CoreLogger.e(LogTag.DEFAULT, it) }
        .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
}
