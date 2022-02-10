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

package me.proton.core.humanverification.data.api

import me.proton.core.humanverification.data.api.request.CreationTokenValidityRequest
import me.proton.core.humanverification.data.api.request.VerificationRequest
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserVerificationApi : BaseRetrofitApi {

    @POST("v4/users/code")
    suspend fun sendVerificationCode(@Body verificationCodeRequest: VerificationRequest)

    @PUT("v4/users/check")
    suspend fun checkCreationTokenValidity(@Body creationTokenValidityRequest: CreationTokenValidityRequest)
}
