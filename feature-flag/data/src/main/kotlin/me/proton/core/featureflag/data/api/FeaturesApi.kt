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

package me.proton.core.featureflag.data.api

import me.proton.core.featureflag.data.api.response.FeaturesApiResponse
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import retrofit2.http.GET
import retrofit2.http.Query

private const val FILTER_FEATURES_FLAGS = "Type=boolean"

interface FeaturesApi : BaseRetrofitApi {

    @GET("core/v4/features?$FILTER_FEATURES_FLAGS")
    suspend fun getFeatureFlag(
        @Query("Code") code: String
    ): FeaturesApiResponse

    /**
     * @param codes is a comma-separated list of featureIds (eg. "feature1,feature2,[...]")
     */
    @GET("core/v4/features?$FILTER_FEATURES_FLAGS")
    suspend fun getFeatureFlags(
        @Query("Code") codes: String
    ): FeaturesApiResponse
}
