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

package me.proton.core.featureflag.data.remote

import me.proton.core.featureflag.data.remote.request.PutFeatureFlagBody
import me.proton.core.featureflag.data.remote.response.GetFeaturesResponse
import me.proton.core.featureflag.data.remote.response.GetUnleashTogglesResponse
import me.proton.core.featureflag.data.remote.response.PutFeatureResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal interface FeaturesApi : BaseRetrofitApi {

    @GET("feature/v2/frontend")
    suspend fun getUnleashToggles(): GetUnleashTogglesResponse

    /**
     * @param code can be a single featureId or a comma-separated list of featureIds (eg. "feature1,feature2,[...]")
     */
    @GET("core/v4/features")
    suspend fun getFeatureFlags(
        @Query("Code") code: String,
        @Query("Type") type: String = "boolean"
    ): GetFeaturesResponse

    @PUT("core/v4/features/{id}/value")
    suspend fun putFeatureFlag(
        @Path("id") featureId: String,
        @Body putFeatureFlagBody: PutFeatureFlagBody
    ): PutFeatureResponse

}
