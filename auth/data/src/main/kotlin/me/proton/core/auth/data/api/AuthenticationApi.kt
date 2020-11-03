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
import me.proton.core.auth.data.entity.request.AddressKeySetupRequest
import me.proton.core.auth.data.entity.request.AddressSetupRequest
import me.proton.core.auth.data.entity.request.SetupKeysRequest
import me.proton.core.auth.data.entity.response.AddressKeySetupResponse
import me.proton.core.auth.data.entity.response.AddressSetupResponse
import me.proton.core.auth.data.entity.response.AddressesResponse
import me.proton.core.auth.data.entity.response.AvailableDomainsResponse
import me.proton.core.auth.data.entity.response.ModulusResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.network.domain.TimeoutOverride
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Tag
import retrofit2.http.PUT
import retrofit2.http.Query

interface AuthenticationApi : BaseRetrofitApi {

    @POST("auth/info")
    suspend fun getLoginInfo(@Body request: LoginInfoRequest): LoginInfoResponse

    @POST("auth")
    suspend fun performLogin(@Body request: LoginRequest): LoginResponse

    @POST("auth/2fa")
    suspend fun performSecondFactor(@Body request: SecondFactorRequest): SecondFactorResponse

    @DELETE("auth")
    suspend fun revokeSession(@Tag timeout: TimeoutOverride): GenericResponse

    @GET("users")
    suspend fun getUser(): UserResponse

    @GET("keys/salts")
    suspend fun getSalts(): KeySaltsResponse

    @GET("users/available")
    suspend fun usernameAvailable(@Query("Name") username: String): GenericResponse

    @GET("domains/available")
    suspend fun getAvailableDomains(): AvailableDomainsResponse

    @GET("addresses")
    suspend fun getAddresses(): AddressesResponse

    @PUT("settings/username")
    suspend fun setUsername(@Query("Username") username: String): GenericResponse

    @POST("addresses/setup")
    suspend fun createAddress(@Body request: AddressSetupRequest): AddressSetupResponse

    @POST("keys/address")
    suspend fun createAddressKey(@Body request: AddressKeySetupRequest): AddressKeySetupResponse

    @POST("keys")
    suspend fun createAddressKeyOld(@Body request: AddressKeySetupRequest): AddressKeySetupResponse

    @POST("auth/modulus")
    suspend fun randomModulus(): ModulusResponse

    @POST("keys/setup")
    suspend fun setupAddressKeys(@Body request: SetupKeysRequest): UserResponse
}
