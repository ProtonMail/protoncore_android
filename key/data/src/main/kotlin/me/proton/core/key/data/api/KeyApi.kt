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

package me.proton.core.key.data.api

import me.proton.core.key.data.api.request.SetupAddressKeyRequest
import me.proton.core.key.data.api.request.SetupKeysRequest
import me.proton.core.key.data.api.response.KeySaltsResponse
import me.proton.core.key.data.api.response.PublicAddressKeysResponse
import me.proton.core.key.data.api.response.SetupAddressKeyResponse
import me.proton.core.key.data.api.response.SetupKeysResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface KeyApi : BaseRetrofitApi {

    @GET("keys/salts")
    suspend fun getSalts(): KeySaltsResponse

    @GET("keys")
    suspend fun getPublicAddressKeys(@Query("Email") email: String): PublicAddressKeysResponse

    @POST("keys/address")
    suspend fun createAddressKey(@Body request: SetupAddressKeyRequest): SetupAddressKeyResponse

    @POST("keys")
    suspend fun createAddressKeyOld(@Body request: SetupAddressKeyRequest): SetupAddressKeyResponse

    @POST("keys/setup")
    suspend fun setupAddressKeys(@Body request: SetupKeysRequest): SetupKeysResponse
}
