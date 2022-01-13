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

package me.proton.core.label.data.remote

import me.proton.core.label.data.remote.request.CreateLabelRequest
import me.proton.core.label.data.remote.request.UpdateLabelRequest
import me.proton.core.label.data.remote.response.GetLabelsResponse
import me.proton.core.label.data.remote.response.SingleLabelResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LabelApi : BaseRetrofitApi {

    @GET("core/v4/labels")
    suspend fun getLabels(@Query("Type") type: Int?): GetLabelsResponse

    @POST("core/v4/labels")
    suspend fun createLabel(@Body request: CreateLabelRequest): SingleLabelResponse

    @PUT("core/v4/labels/{id}")
    suspend fun updateLabel(@Path("id") id: String, @Body request: UpdateLabelRequest): SingleLabelResponse

    @DELETE("core/v4/labels/{id}")
    suspend fun deleteLabel(@Path("id") id: String): GenericResponse
}
