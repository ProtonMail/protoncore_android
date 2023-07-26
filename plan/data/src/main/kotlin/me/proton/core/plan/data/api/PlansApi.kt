/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.plan.data.api.response.DefaultPlanResponse
import me.proton.core.plan.data.api.response.DynamicPlansResponse
import me.proton.core.plan.data.api.response.PlansResponse
import retrofit2.http.GET
import retrofit2.http.Query

internal interface PlansApi : BaseRetrofitApi {
    /**
     * Returns a list of dynamic plans available at the moment.
     * Plans can be filtered by [state] and/or [vendorName].
     */
    @GET("payments/v5/plans")
    suspend fun getDynamicPlans(
        @Query("state") state: Int? = null,
        @Query("vendorName") vendorName: String? = null
    ): DynamicPlansResponse

    /**
     * Returns from the API all plans available for the user in the moment.
     */
    @GET("payments/v4/plans")
    suspend fun getPlans(): PlansResponse

    /**
     * Returns the default plan values from the API.
     */
    @GET("payments/v4/plans/default")
    suspend fun getPlansDefault(): DefaultPlanResponse
}
