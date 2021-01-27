/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserKey
import javax.inject.Inject

/**
 * Try to unlock the primary [UserKey] with the given password.
 *
 * On UnlockResult.Success, the passphrase, derived from password, is stored and the User keys ready to be used.
 */
class UnlockUserPrimaryKey @Inject constructor(
    private val userManager: UserManager,
    private val sessionProvider: SessionProvider,
) {
    /**
     * Try to unlock the user with the given password.
     */
    suspend operator fun invoke(
        sessionId: SessionId,
        password: ByteArray
    ): UserManager.UnlockResult {
        val userId = sessionProvider.getUserId(sessionId)
        checkNotNull(userId) { "Cannot get userId from sessionId = $sessionId" }
        return userManager.unlockWithPassword(userId, PlainByteArray(password))
    }
}
