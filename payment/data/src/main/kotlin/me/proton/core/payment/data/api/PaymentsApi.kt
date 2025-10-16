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
import me.proton.core.payment.data.api.request.CreateOmnichannelPaymentToken
import me.proton.core.payment.data.api.request.CreatePaymentToken
import me.proton.core.payment.data.api.response.CreatePaymentTokenResponse
import me.proton.core.payment.data.api.request.OmnichannelPayment
import me.proton.core.payment.data.api.request.OmnichannelPaymentDetails
import me.proton.core.payment.data.api.response.PaymentMethodsResponse
import me.proton.core.payment.data.api.response.PaymentStatusResponse
import me.proton.core.payment.data.api.response.PaymentStatusV5Response
import me.proton.core.payment.data.api.response.PaymentTokenStatusResponse
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface PaymentsApi : BaseRetrofitApi {

    /**
     * Create a new payment token from user's payment information. Unauthenticated route and could be used during
     * account creation as well as a regular Authenticated for plan upgrade for logged in users.
     * Unauthorized.
     */
    @Deprecated("Use createOmnichannelPaymentToken instead")
    @POST("payments/v4/tokens")
    suspend fun createPaymentToken(@Body body: CreatePaymentToken): CreatePaymentTokenResponse

    @Deprecated("Use createOmnichannelPaymentToken instead")
    @POST("payments/v5/tokens")
    suspend fun createPaymentTokenV5(@Body body: CreatePaymentToken): CreatePaymentTokenResponse

    /**
     * Request to tokenize a Google In App Purchase. The request body contains details about a
     * payment that will be used to create a Proton Payment Token.
     *
     * Note: This request replaces any tokenization request before it, and is the only way to
     * tokenize a purchase for an omnichannel payment flow.
     *
     * @param body structured payment details, see [OmnichannelPayment] and [OmnichannelPaymentDetails]
     * @return when successful, a response containing a [ProtonPaymentToken].
     */
    @POST("payments/v5/tokens")
    suspend fun createOmnichannelPaymentToken(@Body body: CreateOmnichannelPaymentToken): CreatePaymentTokenResponse

    /**
     * Returns the status of payment processors.
     * @param appVendor The app vendor for the app (e.g. "google" or "fdroid").
     */
    @GET("payments/v4/status/{appVendor}")
    suspend fun paymentStatus(@Path("appVendor") appVendor: String): PaymentStatusResponse

    @GET("payments/v5/status/{appVendor}")
    suspend fun paymentStatusV5(@Path("appVendor") appVendor: String): PaymentStatusV5Response

    /**
     * Could be used during account creation as well as a regular Authenticated for plan upgrade for logged in users.
     * Check payment token status (usually with polling).
     * Unauthorized.
     */
    @GET("payments/v4/tokens/{token}")
    suspend fun getPaymentTokenStatus(@Path("token") token: String): PaymentTokenStatusResponse

    @GET("payments/v5/tokens/{token}")
    suspend fun getPaymentTokenStatusV5(@Path("token") token: String): PaymentTokenStatusResponse

    /**
     * Returns existing already saved payment methods.
     * Authorized.
     */
    @GET("payments/v4/methods")
    suspend fun getPaymentMethods(): PaymentMethodsResponse

    @GET("payments/v5/methods")
    suspend fun getPaymentMethodsV5(): PaymentMethodsResponse
}
