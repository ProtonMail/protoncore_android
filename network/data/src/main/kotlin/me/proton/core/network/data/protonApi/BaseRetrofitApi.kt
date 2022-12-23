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
package me.proton.core.network.data.protonApi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.network.data.mapper.parseDetails
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.TimeoutOverride
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Tag

interface BaseRetrofitApi {

    @POST("auth/v4/sessions")
    suspend fun requestToken(@Body request: RequestTokenRequest): TokenResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): TokenResponse

    @GET("tests/ping")
    suspend fun ping(@Tag timeoutOverride: TimeoutOverride)
}

@Serializable
data class ProtonErrorData(
    @SerialName("Code")
    val code: Int,
    @SerialName("Error")
    val error: String,
    @SerialName("ErrorDescription")
    val errorDescription: String? = null,
    @SerialName("Details")
    val details: Details? = null
) {
    val apiResultData get() = ApiResult.Error.ProtonData(code, error).parseDetails(code, details)
}

/**
 * This class should hold all possible Details entries that the Android client is intrested in the future.
 */
@Serializable
data class Details(
    @SerialName("HumanVerificationMethods")
    val verificationMethods: List<String>? = null,
    @SerialName("HumanVerificationToken")
    val verificationToken: String? = null,
    @SerialName("MissingScopes")
    val missingScopes: List<String>? = null
)
