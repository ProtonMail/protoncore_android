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

package me.proton.core.network.domain.session

import me.proton.core.domain.entity.UserId

interface SessionProvider {
    /**
     * Get [Session], if exist, by [sessionId].
     */
    suspend fun getSession(sessionId: SessionId): Session?

    /**
     * Get all [Session].
     */
    suspend fun getSessions(): List<Session>

    /**
     * Get [SessionId], if exist, by [userId].
     */
    suspend fun getSessionId(userId: UserId?): SessionId?

    /**
     * Get [UserId], if exist, by [sessionId].
     */
    suspend fun getUserId(sessionId: SessionId): UserId?
}

/**
 * Get resolved [Session] by [sessionId], falling back to unauthenticated [Session], if exist.
 */
suspend fun SessionProvider.getResolvedSession(
    sessionId: SessionId?
): ResolvedSession = when (sessionId) {
    null -> when (val unAuthSessionId = getSessionId(userId = null)) {
        null -> ResolvedSession.NotFound.Unauthenticated
        else -> when (val session = getSession(unAuthSessionId)) {
            null -> ResolvedSession.NotFound.Unauthenticated
            else -> ResolvedSession.Found.Unauthenticated(session)
        }
    }
    else -> when (val session = getSession(sessionId)) {
        null -> ResolvedSession.NotFound.Authenticated
        else -> when (getSessionId(userId = null)) {
            session.sessionId -> ResolvedSession.Found.Unauthenticated(session)
            else -> ResolvedSession.Found.Authenticated(session)
        }
    }
}

/**
 * Resolved Session with authentication distinction.
 *
 * @see [getResolvedSession]
 */
sealed class ResolvedSession {
    sealed class Found(open val session: Session) : ResolvedSession() {
        data class Unauthenticated(override val session: Session) : Found(session)
        data class Authenticated(override val session: Session) : Found(session)
    }

    sealed class NotFound : ResolvedSession() {
        object Unauthenticated : NotFound()
        object Authenticated : NotFound()
    }
}
