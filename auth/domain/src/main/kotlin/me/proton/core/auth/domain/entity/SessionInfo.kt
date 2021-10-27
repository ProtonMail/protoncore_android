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

package me.proton.core.auth.domain.entity

import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionId

/**
 * Holds Login/Session data.
 */
data class SessionInfo(
    val username: String,
    val accessToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val scope: String,
    val scopes: List<String>,
    val sessionId: SessionId,
    val userId: UserId,
    val refreshToken: String,
    val eventId: String,
    val serverProof: String,
    val localId: Int,
    val passwordMode: Int,
    val secondFactor: SecondFactor?
) {
    val isSecondFactorNeeded = secondFactor is SecondFactor.Enabled
    val isTwoPassModeNeeded = passwordMode == 2
}

sealed class SecondFactor {
    data class Enabled(val supportedMethods: Set<SecondFactorMethod>) : SecondFactor()
    object Disabled : SecondFactor()
}

enum class SecondFactorMethod {
    Totp, Authenticator
}
