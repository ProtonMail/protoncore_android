/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.data.api

import me.proton.core.auth.data.api.request.ActivateDeviceRequest
import me.proton.core.auth.data.api.request.AssociateDeviceRequest
import me.proton.core.auth.data.api.request.CreateDeviceRequest
import me.proton.core.auth.data.api.response.AssociateDeviceResponse
import me.proton.core.auth.data.api.response.AuthDevicesResponse
import me.proton.core.auth.data.api.response.CreateDeviceResponse
import me.proton.core.auth.data.api.response.PendingMemberDevicesResponse
import me.proton.core.auth.data.api.response.UnprivatizationInfoResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthDeviceApi : BaseRetrofitApi {

    @POST("auth/v4/devices")
    suspend fun createDevice(
        @Body request: CreateDeviceRequest
    ): CreateDeviceResponse

    @POST("auth/v4/devices/{deviceId}/associate")
    suspend fun associateDevice(
        @Path("deviceId") deviceId: String,
        @Body request: AssociateDeviceRequest
    ): AssociateDeviceResponse

    @POST("auth/v4/devices/{deviceId}")
    suspend fun activateDevice(
        @Path("deviceId") deviceId: String,
        @Body request: ActivateDeviceRequest
    ): GenericResponse

    @DELETE("auth/v4/devices/{deviceId}")
    suspend fun deleteDevice(
        @Path("deviceId") deviceId: String
    ): GenericResponse

    @GET("auth/v4/devices")
    suspend fun getDevices(): AuthDevicesResponse

    @GET("core/v4/members/devices/pending")
    suspend fun getPendingMemberDevices(): PendingMemberDevicesResponse

    @PUT("auth/v4/devices/{deviceId}/reject")
    suspend fun rejectAuthDevice(
        @Path("deviceId") deviceId: String
    ): GenericResponse

    @GET("auth/v4/members/me/unprivatize")
    suspend fun getUnprivatizationInfo(): UnprivatizationInfoResponse
}
