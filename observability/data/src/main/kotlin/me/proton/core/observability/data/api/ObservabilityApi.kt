/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.observability.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.data.protonApi.GenericResponse
import me.proton.core.observability.data.api.request.DataMetricsRequest
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface ObservabilityApi : BaseRetrofitApi {
    @Headers("Priority: u=6")
    @POST("data/v1/metrics")
    suspend fun postDataMetrics(data: DataMetricsRequest): GenericResponse
}
