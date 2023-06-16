/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.accountrecovery.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.accountrecovery.domain.toAccountRecoveryState
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

public class ObserveAccountRecoveryState @Inject constructor(
    private val userManager: UserManager
) {
    public operator fun invoke(userId: UserId, refresh: Boolean = true): Flow<AccountRecoveryState> =
        userManager.observeUser(sessionUserId = userId, refresh = refresh)
            .map { user ->
                if (user == null) AccountRecoveryState.None
                else
                    user.recovery?.state?.toAccountRecoveryState() ?: AccountRecoveryState.None
            }
}