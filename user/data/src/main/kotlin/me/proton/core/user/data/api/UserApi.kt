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

package me.proton.core.user.data.api

import me.proton.core.key.data.api.response.UsersResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.user.data.api.request.CreateExternalUserRequest
import me.proton.core.user.data.api.request.CreateUserRequest
import me.proton.core.user.data.api.request.CreationTokenValidityRequest
import me.proton.core.user.data.api.request.VerificationRequest
import me.proton.core.user.data.api.response.DirectSignupEnabledResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UserApi : BaseRetrofitApi {

    @GET("users")
    suspend fun getUsers(): UsersResponse

    @GET("users/available")
    suspend fun usernameAvailable(@Query("Name") username: String): GenericResponse

    @GET("users/direct")
    suspend fun directSignupEnabled(): DirectSignupEnabledResponse

    @POST("v4/users")
    suspend fun createUser(@Body userRequest: CreateUserRequest): UsersResponse

    @POST("v4/users/external")
    suspend fun createExternalUser(@Body userRequest: CreateExternalUserRequest): UsersResponse

    @POST("v4/users/code")
    suspend fun sendVerificationCode(@Body verificationCodeRequest: VerificationRequest)

    @PUT("v4/users/check")
    suspend fun checkCreationTokenValidity(@Body creationTokenValidityRequest: CreationTokenValidityRequest)
}
