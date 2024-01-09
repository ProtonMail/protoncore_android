/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.usersettings.data.api.request.SetUsernameRequest
import me.proton.core.usersettings.data.api.request.UpdateCrashReportsRequest
import me.proton.core.usersettings.data.api.request.UpdateLoginPasswordRequest
import me.proton.core.usersettings.data.api.request.UpdateRecoveryEmailRequest
import me.proton.core.usersettings.data.api.request.SetRecoverySecretRequest
import me.proton.core.usersettings.data.api.request.UpdateTelemetryRequest
import me.proton.core.usersettings.data.api.response.SingleUserSettingsResponse
import me.proton.core.usersettings.data.api.response.UpdateUserSettingsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

internal interface UserSettingsApi : BaseRetrofitApi {

    @PUT("core/v4/settings/username")
    suspend fun setUsername(@Body request: SetUsernameRequest): GenericResponse

    @POST("core/v4/settings/recovery/secret")
    suspend fun setRecoverySecret(@Body request: SetRecoverySecretRequest): GenericResponse

    @PUT("core/v4/settings/email")
    suspend fun updateRecoveryEmail(@Body request: UpdateRecoveryEmailRequest): UpdateUserSettingsResponse

    @GET("core/v4/settings")
    suspend fun getUserSettings(): SingleUserSettingsResponse

    @PUT("core/v4/settings/password")
    suspend fun updateLoginPassword(@Body request: UpdateLoginPasswordRequest): UpdateUserSettingsResponse

    @PUT("core/v4/settings/crashreports")
    suspend fun updateCrashReports(@Body request: UpdateCrashReportsRequest): SingleUserSettingsResponse

    @PUT("core/v4/settings/telemetry")
    suspend fun updateTelemetry(@Body request: UpdateTelemetryRequest): SingleUserSettingsResponse
}
