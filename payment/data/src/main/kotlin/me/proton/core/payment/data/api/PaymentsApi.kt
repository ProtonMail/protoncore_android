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

package me.proton.core.payment.data.api

import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.payment.data.api.request.CheckSubscription
import me.proton.core.payment.data.api.request.CreatePaymentToken
import me.proton.core.payment.data.api.response.CheckSubscriptionResponse
import me.proton.core.payment.data.api.request.CreateSubscription
import me.proton.core.payment.data.api.response.CreatePaymentTokenResponse
import me.proton.core.payment.data.api.response.PaymentMethodsResponse
import me.proton.core.payment.data.api.response.PaymentTokenStatusResponse
import me.proton.core.payment.data.api.response.SubscriptionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface PaymentsApi : BaseRetrofitApi {

    /**
     * Unauthorized.
     * Create a new payment token from user's payment information. Unauthenticated route and could be used during
     * account creation as well as a regular Authenticated for plan upgrade for logged in users.
     */
    @POST("payments/tokens")
    suspend fun createPaymentToken(@Body body: CreatePaymentToken): CreatePaymentTokenResponse

    /**
     * Unauthorized.
     * Could be used during account creation as well as a regular Authenticated for plan upgrade for logged in users.
     * Check payment token status (usually with polling).
     */
    @GET("payments/tokens/{token}")
    suspend fun getPaymentTokenStatus(@Path("token") token: String): PaymentTokenStatusResponse

    /**
     * Authorized.
     * Returns existing already saved payment methods.
     */
    @GET("payments/methods")
    suspend fun getPaymentMethods(): PaymentMethodsResponse

    /**
     * Authorized.
     * Returns current active subscription.
     */
    @GET("payments/subscription")
    suspend fun getCurrentSubscription(): SubscriptionResponse

    /**
     * Authorized.
     * Creates new or updates the current active subscription.
     */
    @POST("payments/subscription")
    suspend fun createUpdateSubscription(@Body body: CreateSubscription): SubscriptionResponse

    /**
     * Unauthorized.
     * It checks given a particular plans and cycles how much a user should pay.
     * It also takes into an account any special coupon or gift codes.
     * Should be called upon a user selected any plan, duration and entered a code.
     */
    @POST("payments/subscription/check")
    suspend fun validateSubscription(@Body body: CheckSubscription): CheckSubscriptionResponse
}
