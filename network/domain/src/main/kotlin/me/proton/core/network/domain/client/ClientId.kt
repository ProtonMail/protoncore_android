/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.network.domain.client

import me.proton.core.network.domain.session.SessionId
import java.util.Locale

sealed class ClientId {
    data class AccountSession(val sessionId: SessionId) : ClientId()
    data class CookieSession(val sessionId: CookieSessionId) : ClientId()

    val id: String by lazy {
        when (this) {
            is AccountSession -> sessionId.id
            is CookieSession -> sessionId.id
        }
    }

    companion object {
        /**
         * Creates new instance of the ClientId.
         *
         * If both [sessionId] and [cookieSessionId] are not null, priority has the [sessionId]
         */
        fun newClientId(sessionId: SessionId?, cookieSessionId: String?): ClientId? = when {
            sessionId != null -> AccountSession(sessionId)
            cookieSessionId != null -> CookieSession(CookieSessionId(cookieSessionId))
            else -> null
        }
    }
}

data class CookieSessionId(val id: String)

fun ClientId.getType(): ClientIdType = when (this) {
    is ClientId.AccountSession -> ClientIdType.SESSION
    is ClientId.CookieSession -> ClientIdType.COOKIE
}

fun ClientIdType.getId(clientId: String): ClientId = when (this) {
    ClientIdType.SESSION -> ClientId.AccountSession(SessionId(clientId))
    ClientIdType.COOKIE -> ClientId.CookieSession(CookieSessionId(clientId))
}

enum class ClientIdType(val value: String) {
    SESSION("session"), COOKIE("cookie");

    companion object {
        val map = values().associateBy { it.value }
        fun getByValue(value: String) = map[value.lowercase(Locale.ROOT)] ?: COOKIE
    }
}
