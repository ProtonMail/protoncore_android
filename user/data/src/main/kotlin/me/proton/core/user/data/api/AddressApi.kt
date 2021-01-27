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

import me.proton.core.key.data.api.response.AddressesResponse
import me.proton.core.key.data.api.response.SingleAddressResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.user.data.api.request.CreateAddressRequest
import me.proton.core.user.data.api.response.CreateAddressResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AddressApi : BaseRetrofitApi {

    @GET("addresses")
    suspend fun getAddresses(): AddressesResponse

    @GET("addresses/{id}")
    suspend fun getAddress(@Path("id") id: String): SingleAddressResponse

    @POST("addresses/setup")
    suspend fun createAddress(@Body request: CreateAddressRequest): CreateAddressResponse
}
