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

import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId

@Serializable
sealed class Session(
    open val sessionId: SessionId,
    open val accessToken: String,
    open val refreshToken: String,
    open val scopes: List<String>,
) {
    fun isValid() = listOf(
        sessionId.id,
        accessToken,
        refreshToken
    ).all { it.isNotBlank() }

    data class Authenticated(
        val userId: UserId,
        override val sessionId: SessionId,
        override val accessToken: String,
        override val refreshToken: String,
        override val scopes: List<String>,
    ) : Session(sessionId, accessToken, refreshToken, scopes) {
        constructor(userId: UserId, session: Session) : this(
            userId,
            session.sessionId,
            session.accessToken,
            session.refreshToken,
            session.scopes
        )
    }

    data class Unauthenticated(
        override val sessionId: SessionId,
        override val accessToken: String,
        override val refreshToken: String,
        override val scopes: List<String>,
    ) : Session(sessionId, accessToken, refreshToken, scopes) {
        constructor(session: Session) : this(
            session.sessionId,
            session.accessToken,
            session.refreshToken,
            session.scopes
        )
    }
}

fun Session.toStringLog() = when (this) {
    is Session.Authenticated -> "authenticated"
    is Session.Unauthenticated -> "unauthenticated"
} + " id=${sessionId.id.take(PREFIX_LENGTH)}" +
        " a=${accessToken.take(PREFIX_LENGTH)}" +
        " r=${refreshToken.take(PREFIX_LENGTH)}" +
        " s=$scopes"

internal const val PREFIX_LENGTH = 5