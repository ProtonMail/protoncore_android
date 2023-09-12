/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.presentation.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.entity.DynamicUser
import javax.inject.Inject

class ObserveUserId @Inject constructor(
    private val accountManager: AccountManager,
) {
    private val mutableUser = MutableStateFlow<DynamicUser>(DynamicUser.Unspecified)

    operator fun invoke(): Flow<UserId?> = mutableUser.flatMapLatest { user ->
        when (user) {
            is DynamicUser.Unspecified -> emptyFlow()
            is DynamicUser.None -> flowOf(null)
            is DynamicUser.Primary -> accountManager.getPrimaryUserId()
            is DynamicUser.ByUserId -> accountManager.getAccount(user.userId).mapLatest { it?.userId }
        }
    }

    suspend fun setUser(user: DynamicUser) {
        mutableUser.emit(user)
    }
}
