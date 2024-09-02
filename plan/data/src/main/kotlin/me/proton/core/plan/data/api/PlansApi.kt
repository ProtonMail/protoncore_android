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
import me.proton.core.plan.data.api.request.CheckSubscription
import me.proton.core.plan.data.api.request.CreateSubscription
import me.proton.core.plan.data.api.response.CheckSubscriptionResponse
import me.proton.core.plan.data.api.response.DefaultPlanResponse
import me.proton.core.plan.data.api.response.DynamicPlansResponse
import me.proton.core.plan.data.api.response.DynamicSubscriptionsResponse
import me.proton.core.plan.data.api.response.PlansResponse
import me.proton.core.plan.data.api.response.SubscriptionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

internal interface PlansApi : BaseRetrofitApi {
    /**
     * Returns a list of dynamic plans available at the moment.
     *
     * @param appVendor The app vendor for the app (e.g. "google" or "fdroid").
     */
    @GET("payments/v5/plans")
    suspend fun getDynamicPlans(@Query("Vendor") appVendor: String): DynamicPlansResponse

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

    /**
     * Creates new or updates the current active subscription.
     * Authorized.
     */
    @POST("payments/v4/subscription")
    suspend fun createUpdateSubscription(@Body body: CreateSubscription): SubscriptionResponse

    @POST("payments/v5/subscription")
    suspend fun createUpdateSubscriptionV5(@Body body: CreateSubscription): SubscriptionResponse

    /**
     * Returns current active subscription.
     * Authorized.
     */
    @GET("payments/v4/subscription")
    suspend fun getCurrentSubscription(): SubscriptionResponse

    @GET("payments/v5/subscription")
    suspend fun getDynamicSubscriptions(): DynamicSubscriptionsResponse

    /**
     * It checks given a particular plans and cycles how much a user should pay.
     * It also takes into an account any special coupon or gift codes.
     * Should be called upon a user selected any plan, duration and entered a code.
     * Unauthorized.
     */
    @POST("payments/v4/subscription/check")
    suspend fun validateSubscription(@Body body: CheckSubscription): CheckSubscriptionResponse

    @POST("payments/v5/subscription/check")
    suspend fun validateSubscriptionV5(@Body body: CheckSubscription): CheckSubscriptionResponse
}
