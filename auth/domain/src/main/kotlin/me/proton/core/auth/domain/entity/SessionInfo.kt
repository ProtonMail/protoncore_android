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
    val sessionId: String,
    val userId: String,
    val refreshToken: String,
    val eventId: String,
    val serverProof: String,
    val localId: Int,
    val passwordMode: Int,
    val secondFactor: SecondFactor?,
    val loginPassword: ByteArray? = null
) {
    val isSecondFactorNeeded = secondFactor?.enabled == true
    val isTwoPassModeNeeded = passwordMode == 2
}

data class SecondFactor(
    val enabled: Boolean,
    val universalTwoFactor: UniversalTwoFactor?
)

data class UniversalTwoFactor(
    val challenge: String,
    val registeredKeys: List<UniversalTwoFactorKey>
)

data class UniversalTwoFactorKey(
    val version: String,
    val keyHandle: String
)
