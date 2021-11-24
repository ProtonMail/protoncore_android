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
import me.proton.core.user.data.api.request.UnlockPasswordRequest
import me.proton.core.user.data.api.request.UnlockRequest
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

    @POST("v4/users")
    suspend fun createUser(@Body userRequest: CreateUserRequest): UsersResponse

    @POST("v4/users/external")
    suspend fun createExternalUser(@Body userRequest: CreateExternalUserRequest): UsersResponse

    @PUT("core/v4/users/lock")
    suspend fun removeLockedAndPasswordScopes(): GenericResponse

    @PUT("core/v4/users/unlock")
    suspend fun unlockUser(@Body unlockRequest: UnlockRequest): GenericResponse

    @PUT("core/v4/user/password")
    suspend fun unlockUser(@Body unlockRequest: UnlockPasswordRequest): GenericResponse
}
