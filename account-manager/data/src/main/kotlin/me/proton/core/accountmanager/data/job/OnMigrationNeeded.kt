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

import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.data.AccountStateHandler
import me.proton.core.accountmanager.domain.onAccountState
import me.proton.core.domain.entity.UserId

fun AccountStateHandler.onMigrationNeeded(
    block: suspend (UserId) -> Unit
) = accountManager.onAccountState(AccountState.MigrationNeeded)
    .onEach { account -> block(account.userId) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
