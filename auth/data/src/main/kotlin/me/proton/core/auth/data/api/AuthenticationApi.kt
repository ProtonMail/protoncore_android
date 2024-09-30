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

package me.proton.core.auth.data.api

import me.proton.core.auth.data.api.request.AuthInfoRequest
import me.proton.core.auth.data.api.request.EmailValidationRequest
import me.proton.core.auth.data.api.request.ForkSessionRequest
import me.proton.core.auth.data.api.request.LoginLessRequest
import me.proton.core.auth.data.api.request.LoginRequest
import me.proton.core.auth.data.api.request.LoginSsoRequest
import me.proton.core.auth.data.api.request.PhoneValidationRequest
import me.proton.core.auth.data.api.request.RefreshSessionRequest
import me.proton.core.auth.data.api.request.RequestSessionRequest
import me.proton.core.auth.data.api.request.SecondFactorRequest
import me.proton.core.auth.data.api.response.AuthInfoResponse
import me.proton.core.auth.data.api.response.ForkSessionResponse
import me.proton.core.auth.data.api.response.LoginResponse
import me.proton.core.auth.data.api.response.ModulusResponse
import me.proton.core.auth.data.api.response.ScopesResponse
import me.proton.core.auth.data.api.response.SecondFactorResponse
import me.proton.core.auth.data.api.response.SessionResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.TimeoutOverride
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Tag

interface AuthenticationApi : BaseRetrofitApi {

    @POST("auth/v4/info")
    suspend fun getAuthInfo(@Body request: AuthInfoRequest): AuthInfoResponse

    @GET("auth/v4/modulus")
    suspend fun getRandomModulus(): ModulusResponse

    @GET("auth/v4/scopes")
    suspend fun getScopes(): ScopesResponse

    @POST("auth/v4")
    suspend fun performLogin(@Body request: LoginRequest): LoginResponse

    @POST("auth/v4")
    suspend fun performLoginSso(@Body request: LoginSsoRequest): LoginResponse

    @POST("auth/v4/credentialless")
    suspend fun performLoginLess(@Body request: LoginLessRequest): LoginResponse

    @POST("auth/v4/2fa")
    suspend fun performSecondFactor(@Body request: SecondFactorRequest): SecondFactorResponse

    @DELETE("auth/v4")
    suspend fun revokeSession(@Tag timeout: TimeoutOverride, @Query("AuthDevice") revokeAuthDevice: Int): GenericResponse

    @POST("auth/v4/sessions")
    suspend fun requestSession(@Body request: RequestSessionRequest): SessionResponse

    @POST("auth/v4/refresh")
    suspend fun refreshSession(@Body body: RefreshSessionRequest): SessionResponse

    @POST("core/v4/validate/email")
    suspend fun validateEmail(@Body request: EmailValidationRequest): GenericResponse

    @POST("core/v4/validate/phone")
    suspend fun validatePhone(@Body request: PhoneValidationRequest): GenericResponse

    @POST("auth/v4/sessions/forks")
    suspend fun forkSession(@Body request: ForkSessionRequest): ForkSessionResponse
}
