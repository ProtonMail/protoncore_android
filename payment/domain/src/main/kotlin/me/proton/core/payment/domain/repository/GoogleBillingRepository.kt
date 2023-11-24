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

package me.proton.core.payment.domain.repository

import kotlinx.coroutines.flow.Flow
import me.proton.core.payment.domain.entity.GoogleBillingFlowParams
import me.proton.core.payment.domain.entity.GoogleBillingResult
import me.proton.core.payment.domain.entity.GoogleProductDetails
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProductId

/** Repository for interacting with Google Play Billing Client.
 * Make sure to [destroy] it once you're done. You can use the [use] function to do that automatically.
 */
public interface GoogleBillingRepository<A: Any> : AutoCloseable {
    public val purchaseUpdated: Flow<Pair<GoogleBillingResult, List<GooglePurchase>?>>

    /**
     * @param purchaseToken A token from [GooglePurchase.purchaseToken].
     * @throws BillingClientError
     */
    public suspend fun acknowledgePurchase(purchaseToken: GooglePurchaseToken)

    /** Closes the connection to Google Billing client.
     * After this method is called, it's not possible to interact with this instance anymore.
     */
    public fun destroy()

    /**
     * @throws BillingClientError
     */
    public suspend fun getProductsDetails(googlePlayPlanNames: List<ProductId>): List<GoogleProductDetails>?

    /**
     * @throws BillingClientError
     */
    public suspend fun launchBillingFlow(activity: A, billingFlowParams: GoogleBillingFlowParams)

    /** Query for active subscriptions.
     * @throws BillingClientError
     */
    public suspend fun querySubscriptionPurchases(): List<GooglePurchase>

    override fun close() {
        destroy()
    }
}

/** Error response from the Google Billing client.
 * @property responseCode Response code from the Google Billing client.
 *  Possible values are marked by `com.android.billingclient.api.BillingClient.BillingResponseCode`.
 *  If [responseCode] is `null`, it's likely because the device
 *  is using a custom Android ROM with unofficial/fake play services.
 * @property debugMessage The debug message returned by the Google Billing client.
 */
public data class BillingClientError(
    public val responseCode: Int?,
    public val debugMessage: String?
) : Throwable("responseCode: $responseCode message: $debugMessage")
