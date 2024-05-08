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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User
import javax.inject.Inject

class ObserveUsersWithoutRecoverySecret @Inject constructor(
    private val observeUserDeviceRecovery: ObserveUserDeviceRecovery
) {
    operator fun invoke(): Flow<UserId> = observeUserDeviceRecovery()
        .filter { (user, deviceRecovery) ->
            deviceRecovery == true && user.isRecoverySecretMissingForPrimaryKey()
        }.map { (user, _) ->
            user.userId
        }
}

private fun User.isRecoverySecretMissingForPrimaryKey(): Boolean = keys
    .filter { it.active == true && it.privateKey.isPrimary }
    .any { it.recoverySecret == null }
