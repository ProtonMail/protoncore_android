/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.data.repository

import android.content.Context
import me.proton.core.auth.data.api.AuthenticationApi
import me.proton.core.auth.data.api.request.AuthInfoRequest
import me.proton.core.auth.data.api.request.EmailValidationRequest
import me.proton.core.auth.data.api.request.LoginRequest
import me.proton.core.auth.data.api.request.LoginSsoRequest
import me.proton.core.auth.data.api.request.PhoneValidationRequest
import me.proton.core.auth.data.api.request.RefreshSessionRequest
import me.proton.core.auth.data.api.request.RequestSessionRequest
import me.proton.core.auth.data.api.request.SecondFactorRequest
import me.proton.core.auth.data.api.request.UniversalTwoFactorRequest
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.entity.AuthIntent
import me.proton.core.auth.domain.entity.Modulus
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.ValidateServerProof
import me.proton.core.challenge.data.frame.ChallengeFrame
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.challenge.domain.framePrefix
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.TimeoutOverride
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.util.kotlin.coroutine.result

class AuthRepositoryImpl(
    private val provider: ApiProvider,
    private val context: Context,
    private val product: Product,
    private val validateServerProof: ValidateServerProof
) : AuthRepository {

    override suspend fun getAuthInfoSrp(sessionId: SessionId?, username: String): AuthInfo.Srp =
        provider.get<AuthenticationApi>(sessionId).invoke {
            val request = AuthInfoRequest(username, AuthIntent.PROTON.value)
            getAuthInfo(request).toAuthInfo(username) as AuthInfo.Srp
        }.valueOrThrow

    override suspend fun getAuthInfoSso(sessionId: SessionId?, email: String): AuthInfo.Sso =
        provider.get<AuthenticationApi>(sessionId).invoke {
            val request = AuthInfoRequest(email, AuthIntent.SSO.value)
            getAuthInfo(request).toAuthInfo(email) as AuthInfo.Sso
        }.valueOrThrow

    override suspend fun randomModulus(sessionId: SessionId?): Modulus =
        provider.get<AuthenticationApi>(sessionId).invoke {
            getRandomModulus().toModulus()
        }.valueOrThrow

    override suspend fun getScopes(sessionId: SessionId?): List<String> =
        provider.get<AuthenticationApi>(sessionId).invoke {
            getScopes().scopes
        }.valueOrThrow

    override suspend fun performLogin(
        username: String,
        srpProofs: SrpProofs,
        srpSession: String,
        frames: List<ChallengeFrameDetails>
    ) = result("performLogin") {
        provider.get<AuthenticationApi>().invoke {
            val request = LoginRequest(
                username,
                srpProofs.clientEphemeral,
                srpProofs.clientProof,
                srpSession,
                getFrameMap(frames)
            )
            val response = performLogin(request)
            validateServerProof(requireNotNull(response.serverProof), srpProofs.expectedServerProof) { "login failed" }
            response.toSessionInfo(username)
        }.valueOrThrow
    }

    override suspend fun performLoginSso(
        email: String,
        token: String
    ): SessionInfo =
        provider.get<AuthenticationApi>().invoke {
        val request = LoginSsoRequest(token)
        val response = performLoginSso(request)
        response.toSessionInfo(email)
    }.valueOrThrow

    private suspend fun getFrameMap(frames: List<ChallengeFrameDetails>): Map<String, ChallengeFrame?> {
        val name = "${product.framePrefix()}-0"
        val frame = ChallengeFrame.Username.from(context, frames.getOrNull(0))
        return mapOf(name to frame)
    }

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

    override suspend fun requestSession(): ApiResult<Session> =
        provider.get<AuthenticationApi>(userId = null).invoke {
            val name = "${product.framePrefix()}-0"
            val frame = ChallengeFrame.Device.build(context)
            val response = requestSession(
                RequestSessionRequest(
                    payload = mapOf(name to frame)
                )
            )
            response.toSession(userId = null)
        }

    override suspend fun refreshSession(session: Session): ApiResult<Session> =
        provider.get<AuthenticationApi>(session.sessionId).invoke {
            val userId = (session as? Session.Authenticated)?.userId
            val response = refreshSession(
                RefreshSessionRequest(
                    uid = session.sessionId.id,
                    refreshToken = session.refreshToken
                )
            )
            response.toSession(userId = userId)
        }

    override suspend fun validateEmail(email: String): Boolean = result("validateEmail") {
        provider.get<AuthenticationApi>().invoke {
            val request = EmailValidationRequest(email)
            validateEmail(request).isSuccess()
        }.valueOrThrow
    }

    override suspend fun validatePhone(phone: String): Boolean = result("validatePhone") {
        provider.get<AuthenticationApi>().invoke {
            val request = PhoneValidationRequest(phone)
            validatePhone(request).isSuccess()
        }.valueOrThrow
    }
}
