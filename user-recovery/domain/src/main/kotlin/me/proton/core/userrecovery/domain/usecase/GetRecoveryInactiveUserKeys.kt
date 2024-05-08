/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.domain.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.fingerprint
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserKey
import javax.inject.Inject

class GetRecoveryInactiveUserKeys @Inject constructor(
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext
) {
    suspend operator fun invoke(
        userId: UserId,
        keys: List<PrivateKey>,
    ): List<UserKey> {
        val user = userManager.getUser(userId)
        val inactiveMap = user.keys.associateBy { it.privateKey.fingerprint(cryptoContext) }
            .filter { it.value.active?.not() ?: false }
        val recoverableMap = keys.associateBy { it.fingerprint(cryptoContext) }
            .filter { it.key in inactiveMap }
            .filter { it.value.canUnlock(cryptoContext) }
        return recoverableMap.mapNotNull {
            // Get UserKey and replace recovered PrivateKey.
            inactiveMap[it.key]?.copy(privateKey = it.value)
        }
    }
}
