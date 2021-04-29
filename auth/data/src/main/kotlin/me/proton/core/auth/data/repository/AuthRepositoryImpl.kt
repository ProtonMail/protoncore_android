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

package me.proton.core.auth.data.repository

import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.data.api.request.EmailValidationRequest
import me.proton.core.auth.data.api.request.LoginInfoRequest
import me.proton.core.auth.data.api.request.LoginRequest
import me.proton.core.auth.data.api.request.PhoneValidationRequest
import me.proton.core.auth.data.api.request.SecondFactorRequest
import me.proton.core.auth.data.api.request.UniversalTwoFactorRequest
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.session.SessionId

/**
 * Implementation of the [AuthRepository].
 * Provides implementation of the all auth related API routes.
 */
class AuthRepositoryImpl(
    private val provider: ApiProvider
) : AuthRepository {

    /**
     * Fetches the login info object for a particular [username] account.
     * The results are needed for further BL decisions on the login process.
     *
     * @param username the account's username trying to make a login request.
     * @param clientSecret client/app specific string.
     *
     * @return [LoginInfo] object containing meta-data for further login process operations.
     */
    override suspend fun getLoginInfo(username: String, clientSecret: String): LoginInfo =
        provider.get<AuthenticationApi>().invoke {
            val request = LoginInfoRequest(username, clientSecret)
            getLoginInfo(request).toLoginInfo(username)
        }.valueOrThrow

    /**
     * Returns new random modulus generated from the API.
     */
    override suspend fun randomModulus(): Modulus =
        provider.get<AuthenticationApi>().invoke {
            getRandomModulus().toModulus()
        }.valueOrThrow

    /**
     * Returns session scopes.
     */
    override suspend fun getScopes(sessionId: SessionId): List<String> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            getScopes().scopes
        }.valueOrThrow

    /**
     * Performs the login request to the API to try to get a valid Access Token and Session for the Account/username.
     *
     * @param username the account's username trying to make a login request.
     * @param clientSecret client/app specific string.
     * @param clientEphemeral Base64 encoded SrpProof generated client ephemeral.
     * @param clientProof Base64 encoded SrpProof generated proof.
     * @param srpSession the SRPSession returned from the [getLoginInfo] API result.
     *
     * @return [SessionInfo] login result containing the Access and Refresh tokens and additional meta-data.
     */
    override suspend fun performLogin(
        username: String,
        clientSecret: String,
        clientEphemeral: String,
        clientProof: String,
        srpSession: String
    ): SessionInfo =
        provider.get<AuthenticationApi>().invoke {
            val request = LoginRequest(username, clientSecret, clientEphemeral, clientProof, srpSession)
            performLogin(request).toSessionInfo(username)
        }.valueOrThrow

    /**
     * Performs the second factor request for the Accounts that have second factor enabled.
     *
     * @param sessionId the session Id for the current user making this request.
     * @param secondFactorProof the [SecondFactorProof] object containing the 2FA details.
     *
     * @return [ScopeInfo] object containing full list of available scopes for the user.
     */
    override suspend fun performSecondFactor(
        sessionId: SessionId,
        secondFactorProof: SecondFactorProof
    ): ScopeInfo =
        provider.get<AuthenticationApi>(sessionId).invoke {
            val request = when (secondFactorProof) {
                is SecondFactorProof.SecondFactorCode -> SecondFactorRequest(
                    secondFactorCode = secondFactorProof.code
                )
                is SecondFactorProof.SecondFactorSignature -> SecondFactorRequest(
                    universalTwoFactorRequest = UniversalTwoFactorRequest(
                        keyHandle = secondFactorProof.keyHandle,
                        clientData = secondFactorProof.clientData,
                        signatureData = secondFactorProof.signatureData
                    )
                )
            }
            performSecondFactor(request).toScopeInfo()
        }.valueOrThrow

    /**
     * Revokes the session for the user.
     */
    override suspend fun revokeSession(sessionId: SessionId): Boolean =
        provider.get<AuthenticationApi>(sessionId).invoke(forceNoRetryOnConnectionErrors = true) {
            revokeSession(
                TimeoutOverride(
                    connectionTimeoutSeconds = 1,
                    readTimeoutSeconds = 1,
                    writeTimeoutSeconds = 1
                )
            ).isSuccess()
        }.valueOrNull ?: true // Ignore any error.
}
