/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.auth.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.Session.Authenticated
import me.proton.core.network.domain.session.Session.Unauthenticated
import me.proton.core.network.domain.session.SessionId

@Serializable
data class SessionResponse(
    @SerialName("AccessToken")
    val accessToken: String,
    @SerialName("RefreshToken")
    val refreshToken: String,
    @SerialName("TokenType")
    val tokenType: String,
    @SerialName("Scopes")
    val scopes: List<String>,
    @SerialName("UID")
    val sessionId: String
) {
    fun toSession(userId: UserId?): Session = when (userId) {
        null -> Unauthenticated(SessionId(sessionId), accessToken, refreshToken, scopes)
        else -> Authenticated(userId, SessionId(sessionId), accessToken, refreshToken, scopes)
    }
}
