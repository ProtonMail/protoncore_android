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

package me.proton.core.keytransparency.data.remote

import me.proton.core.keytransparency.data.remote.request.UploadVerifiedEpochRequest
import me.proton.core.keytransparency.data.remote.response.EpochResponse
import me.proton.core.keytransparency.data.remote.response.EpochsResponse
import me.proton.core.keytransparency.data.remote.response.ProofPairResponse
import me.proton.core.keytransparency.data.remote.response.VerifiedEpochResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

internal interface KeyTransparencyApi : BaseRetrofitApi {

    @GET("kt/v1/epochs/{epochID}/proof/{email}")
    suspend fun getProof(@Path("epochID") epochID: Int, @Path("email") email: String): ProofPairResponse

    @GET("kt/v1/epochs/{epochID}")
    suspend fun getEpoch(@Path("epochID") epochID: Int): EpochResponse

    @GET("kt/v1/epochs")
    suspend fun getLastEpoch(): EpochsResponse

    @GET("kt/v1/verifiedepoch/{addressID}")
    suspend fun getVerifiedEpoch(@Path("addressID") addressID: String): VerifiedEpochResponse

    @PUT("kt/v1/verifiedepoch/{addressID}")
    suspend fun uploadVerifiedEpoch(
        @Path("addressID") addressID: String,
        @Body verifiedEpochRequest: UploadVerifiedEpochRequest
    ): GenericResponse
}
