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
import me.proton.core.auth.domain.entity.RawSessionForkPayload
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.entity.SessionForkUserCode
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId

interface AuthRepository {

    /**
     * Get Authentication Info for SRP or SSO account.
     *
     * @return [AuthInfo], either [AuthInfo.Srp] or [AuthInfo.Sso].
     *
     * @see [getAuthInfoSrp]
     * @see [getAuthInfoSso]
     */
    suspend fun getAuthInfoAuto(
        sessionId: SessionId?,
        username: String
    ): AuthInfo

    /**
     * Get Authentication Info for SRP account.
     */
    suspend fun getAuthInfoSrp(
        sessionId: SessionId?,
        username: String
    ): AuthInfo.Srp

    /**
     * Get Authentication Info for SSO account.
     */
    suspend fun getAuthInfoSso(
        sessionId: SessionId?,
        email: String,
    ): AuthInfo.Sso

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
     * Perform SSO Login to create a session (accessToken, refreshToken, sessionId, ...).
     */
    suspend fun performLoginSso(
        email: String,
        token: String,
    ): SessionInfo

    /**
     * Perform credentialless Login to create a session (accessToken, refreshToken, sessionId, ...).
     */
    suspend fun performLoginLess(
        frames: List<ChallengeFrameDetails> = emptyList()
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
        sessionId: SessionId,
        revokeAuthDevice: Boolean
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
     * Request a new [Session], remotely.
     */
    suspend fun requestSession(): ApiResult<Session>

    /**
     * Refresh a [Session], remotely.
     */
    suspend fun refreshSession(session: Session): ApiResult<Session>

    /**
     * Validate recovery email.
     */
    suspend fun validateEmail(email: String): Boolean

    /**
     * Validate recovery phone.
     */
    suspend fun validatePhone(phone: String): Boolean

    /**
     * Fork a session and return a selector.
     */
    suspend fun forkSession(
        sessionId: SessionId,
        payload: String?,
        childClientId: String,
        independent: Long,
        userCode: String? = null,
    ): String

    suspend fun getSessionForks(sessionId: SessionId?): Pair<SessionForkSelector, SessionForkUserCode>

    suspend fun getForkedSession(
        selector: SessionForkSelector
    ): Pair<RawSessionForkPayload?, Session.Authenticated>
}
