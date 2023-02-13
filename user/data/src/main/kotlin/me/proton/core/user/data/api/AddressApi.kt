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
import me.proton.core.user.data.api.request.UpdateAddressRequest
import me.proton.core.user.data.api.request.UpdateOrderRequest
import me.proton.core.user.data.api.response.CreateAddressResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AddressApi : BaseRetrofitApi {

    @GET("core/v4/addresses")
    suspend fun getAddresses(): AddressesResponse

    @GET("core/v4/addresses/{id}")
    suspend fun getAddress(@Path("id") id: String): SingleAddressResponse

    @POST("core/v4/addresses/setup")
    suspend fun createAddress(@Body request: CreateAddressRequest): CreateAddressResponse

    @PUT("core/v4/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body request: UpdateAddressRequest)

    @PUT("core/v4/addresses/order")
    suspend fun updateOrder(@Body request: UpdateOrderRequest)
}
