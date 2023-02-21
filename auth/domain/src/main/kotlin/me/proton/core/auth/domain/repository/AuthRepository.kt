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

package me.proton.core.auth.domain.repository

import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.network.domain.session.SessionId

interface AuthRepository {

    /**
     * Get Authentication Info (e.g. needed during login or password/locked scope).
     */
    suspend fun getAuthInfo(
        sessionId: SessionId?,
        username: String,
    ): AuthInfo

    /**
     * Perform Login to create a session (accessToken, refreshToken, sessionId, ...).
     */
    suspend fun performLogin(
        username: String,
        srpProofs: SrpProofs,
        srpSession: String,
        frames: List<ChallengeFrameDetails>
    ): SessionInfo

    /**
     * Perform Two Factor for the Login process for a given [SessionId].
     */
    suspend fun performSecondFactor(
        sessionId: SessionId,
        secondFactorProof: SecondFactorProof
    ): ScopeInfo

    /**
     * Revoke session for a given [SessionId].
     */
    suspend fun revokeSession(
        sessionId: SessionId
    ): Boolean

    /**
     * Asks API to generate new random modulus.
     */
    suspend fun randomModulus(
        sessionId: SessionId?
    ): Modulus

    /**
     * Get session scopes.
     */
    suspend fun getScopes(
        sessionId: SessionId?
    ): List<String>

    /**
     * Validate recovery email.
     */
    suspend fun validateEmail(email: String): Boolean

    /**
     * Validate recovery phone.
     */
    suspend fun validatePhone(phone: String): Boolean
}
