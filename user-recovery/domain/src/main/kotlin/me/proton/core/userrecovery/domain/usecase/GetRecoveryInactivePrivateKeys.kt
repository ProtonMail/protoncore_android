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
import javax.inject.Inject

class GetRecoveryInactivePrivateKeys @Inject constructor(
    private val userManager: UserManager,
    private val cryptoContext: CryptoContext
) {
    suspend operator fun invoke(
        userId: UserId,
        keys: List<PrivateKey>,
    ): List<PrivateKey> {
        val user = userManager.getUser(userId)
        val inactive = user.keys.filter { it.active?.not() ?: false }
        val fingerprint = inactive.associateBy { it.privateKey.fingerprint(cryptoContext) }
        val recoverable = keys.filter { it.fingerprint(cryptoContext) in fingerprint }
        return recoverable.filter { it.canUnlock(cryptoContext) }
    }
}
