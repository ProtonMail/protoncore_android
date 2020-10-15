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

import me.proton.core.auth.data.entity.KeySaltsResponse
import me.proton.core.auth.data.entity.LoginInfoRequest
import me.proton.core.auth.data.entity.LoginInfoResponse
import me.proton.core.auth.data.entity.LoginRequest
import me.proton.core.auth.data.entity.LoginResponse
import me.proton.core.auth.data.entity.SecondFactorRequest
import me.proton.core.auth.data.entity.SecondFactorResponse
import me.proton.core.auth.data.entity.UserResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthenticationApi : BaseRetrofitApi {

    @POST("auth/info")
    suspend fun getLoginInfo(@Body request: LoginInfoRequest): LoginInfoResponse

    @POST("auth")
    suspend fun performLogin(@Body request: LoginRequest): LoginResponse

    @POST("auth/2fa")
    suspend fun performSecondFactor(@Body request: SecondFactorRequest): SecondFactorResponse

    @DELETE("auth")
    suspend fun revokeSession(): GenericResponse

    @GET("users")
    suspend fun getUser(): UserResponse

    @GET("keys/salts")
    suspend fun getSalts(): KeySaltsResponse
}
