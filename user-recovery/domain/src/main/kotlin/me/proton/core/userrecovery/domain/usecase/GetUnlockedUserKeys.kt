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

package me.proton.core.userrecovery.domain.usecase

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.unlockOrNull
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

/**
 * Returns a list of [UnlockedKey]s, that should be included when [generating recovery file][GetRecoveryFile].
 */
class GetUnlockedUserKeys @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userManager: UserManager
) {
    suspend operator fun invoke(userId: UserId): List<UnlockedKey> {
        val user = userManager.getUser(userId)
        val activeKeys = user.keys.filter { it.active ?: false }
        val privateKeys = activeKeys.map { it.privateKey }
        return privateKeys.mapNotNull { it.unlockOrNull(cryptoContext)?.unlockedKey }
    }
}