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

package me.proton.core.auth.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

@Serializable
data class LoginResponse(
    @SerialName("AccessToken")
    val accessToken: String,
    @SerialName("TokenType")
    val tokenType: String,
    @SerialName("Scopes")
    val scopes: List<String>,
    @SerialName("UID")
    val sessionId: String,
    @SerialName("UserID")
    val userId: String,
    @SerialName("RefreshToken")
    val refreshToken: String,
    @SerialName("EventID")
    val eventId: String,
    @SerialName("ServerProof")
    val serverProof: String,
    @SerialName("LocalID")
    val localId: Int,
    @SerialName("PasswordMode")
    val passwordMode: Int,
    @SerialName("2FA")
    val secondFactorInfo: SecondFactorInfoResponse,
    @SerialName("TemporaryPassword")
    val temporaryPassword: Int,
) {
    fun toSessionInfo(username: String): SessionInfo = SessionInfo(
        username = username,
        accessToken = accessToken,
        tokenType = tokenType,
        scopes = scopes,
        sessionId = SessionId(sessionId),
        userId = UserId(userId),
        refreshToken = refreshToken,
        eventId = eventId,
        serverProof = serverProof,
        localId = localId,
        passwordMode = passwordMode,
        secondFactor = secondFactorInfo.toSecondFactor(),
        temporaryPassword = temporaryPassword.toBooleanOrFalse()
    )
}

private fun Int.toBooleanOrFalse(): Boolean = this == 1
