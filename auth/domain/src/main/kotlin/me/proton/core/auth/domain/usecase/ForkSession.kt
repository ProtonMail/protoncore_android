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

package me.proton.core.auth.domain.usecase

import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.toInt
import javax.inject.Inject

typealias Selector = String

class ForkSession @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionProvider: SessionProvider,
) {

    /**
     * @param userId The user to fork the session for.
     * @param payload Base64 encoded and encrypted payload to communicate to the child session.
     *  (for example [me.proton.core.auth.domain.usecase.fork.GetEncryptedPassphrasePayload]).
     * @param childClientId Expected ClientID of the child (e.g. "android-mail").
     * @param independent If true, the forked session is preserved when a parent or sibling session is logged out.
     * @param userCode If not null, it will be used as the selector. Case-insensitive.
     */
    suspend operator fun invoke(
        userId: UserId,
        payload: String?,
        childClientId: String,
        independent: Boolean,
        userCode: String? = null,
    ): Selector {
        val sessionId = sessionProvider.getSessionId(userId)
        check(sessionId != null) { "Session id not found (${userId.id})" }
        return authRepository.forkSession(
            sessionId = sessionId,
            payload = payload,
            childClientId = childClientId,
            independent = independent.toInt().toLong(),
            userCode = userCode,
        )
    }
}
