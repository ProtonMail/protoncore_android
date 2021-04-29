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

package me.proton.core.network.domain.session

import java.util.Locale

sealed class ClientId {
    data class AccountSessionId(val sessionId: SessionId) : ClientId()
    data class NetworkCookieSessionId(val sessionId: CookieSessionId) : ClientId()

    fun id(): String = when (this) {
        is AccountSessionId -> sessionId.id
        is NetworkCookieSessionId -> sessionId.id
    }

    companion object {
        /**
         * Creates new instance of the ClientId.
         * If both [SessionId] and [CookieSessionId] are not null, priority has the [SessionId]
         */
        fun newClientId(sessionId: SessionId?, cookieSessionId: String?): ClientId? {
            if (sessionId != null) {
                return AccountSessionId(sessionId)
            }
            if (cookieSessionId != null) {
                return NetworkCookieSessionId(CookieSessionId(cookieSessionId))
            }
            return null
        }
    }
}

fun ClientId.getType(): ClientIdType =
    when(this) {
        is ClientId.AccountSessionId -> ClientIdType.SESSION
        is ClientId.NetworkCookieSessionId -> ClientIdType.COOKIE
    }

enum class ClientIdType(val value: String) {
    SESSION("session"), COOKIE("cookie");

    companion object {
        val map = values().associateBy { it.value }
        fun getByValue(value: String) = map[value.toLowerCase(Locale.ROOT)] ?: COOKIE
    }
}
