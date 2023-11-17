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

package me.proton.core.push.data.remote

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.push.data.remote.response.GetPushesResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

internal interface PushApi : BaseRetrofitApi {
    @GET("core/v4/pushes/active")
    suspend fun getAllPushes(): GetPushesResponse

    @DELETE("core/v4/pushes/{id}")
    suspend fun deletePush(@Path("id") pushId: String)
}
