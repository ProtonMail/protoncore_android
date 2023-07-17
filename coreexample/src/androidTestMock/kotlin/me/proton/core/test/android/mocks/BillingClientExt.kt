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

package me.proton.core.test.android.mocks

import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetails.PricingPhase
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk

internal fun BillingClient.mockBillingClientSuccess(listeners: () -> List<PurchasesUpdatedListener>) {
    val purchases = listOf(mockk<Purchase> {
        every { accountIdentifiers } returns mockk {
            every { obfuscatedAccountId } returns "cus_google_QTqe7W-dkfX09qtIy7Mb"
        }
        every { isAcknowledged } returns true
        every { orderId } returns "order-id"
        every { packageName } returns "package-name"
        every { purchaseTime } returns 0
        every { products } returns listOf("googlemail_mail2022_12_renewing")
        every { purchaseState } returns Purchase.PurchaseState.PURCHASED
        every { purchaseToken } returns "google-purchase-token"
    })

    mockStartConnection(BillingClient.BillingResponseCode.OK)
    mockLaunchBillingFlow(
        BillingClient.BillingResponseCode.OK,
        listeners,
        BillingClient.BillingResponseCode.OK,
        purchases
    )
    mockQueryProductDetailsAsync(
        BillingClient.BillingResponseCode.OK,
        priceAmountMicros = 99_990_000,
        priceCurrencyCode = "CHF",
        formattedPrice = "99.99",
        googleProductId = "googlemail_mail2022_12_renewing"
    )
    mockQueryPurchasesAsync(
        BillingClient.BillingResponseCode.OK,
        purchases
    )
    mockAcknowledgePurchase(BillingClient.BillingResponseCode.OK)
}

internal fun BillingClient.mockAcknowledgePurchase(@BillingClient.BillingResponseCode responseCode: Int) {
    every { acknowledgePurchase(any(), any()) } answers {
        val listener = args[1] as AcknowledgePurchaseResponseListener
        listener.onAcknowledgePurchaseResponse(
            BillingResult.newBuilder().setResponseCode(responseCode).build()
        )
    }
}

internal fun BillingClient.mockStartConnection(@BillingClient.BillingResponseCode responseCode: Int) {
    every { startConnection(any()) } answers {
        val listener = args[0] as BillingClientStateListener
        listener.onBillingSetupFinished(
            BillingResult.newBuilder().setResponseCode(responseCode).build()
        )
    }
    justRun { endConnection() }
}

internal fun BillingClient.mockLaunchBillingFlow(
    @BillingClient.BillingResponseCode responseCode: Int,
    listeners: () -> List<PurchasesUpdatedListener>,
    @BillingClient.BillingResponseCode purchaseResponseCode: Int,
    purchases: List<Purchase>
) {
    every { launchBillingFlow(any(), any()) } answers {
        listeners().forEach {
            it.onPurchasesUpdated(
                BillingResult.newBuilder().setResponseCode(purchaseResponseCode).build(),
                purchases
            )
        }
        BillingResult.newBuilder().setResponseCode(responseCode).build()
    }
}

internal fun BillingClient.mockQueryProductDetailsAsync(
    @BillingClient.BillingResponseCode responseCode: Int,
    priceAmountMicros: Long,
    priceCurrencyCode: String,
    formattedPrice: String,
    googleProductId: String
) {
    val pricingPhase = mockk<PricingPhase> {
        every { this@mockk.priceAmountMicros } returns priceAmountMicros
        every { this@mockk.priceCurrencyCode } returns priceCurrencyCode
        every { this@mockk.formattedPrice } returns formattedPrice
    }
    val productDetailsList = listOf(mockk<ProductDetails>(relaxed = true) {
        every { productId } returns googleProductId
        every { subscriptionOfferDetails } returns listOf(
            mockk {
                every { offerToken } returns "offer-token"
                every { pricingPhases } returns mockk {
                    every { pricingPhaseList } returns listOf(pricingPhase)
                }
            }
        )
    })
    every { queryProductDetailsAsync(any(), any()) } answers {
        val listener = args[1] as ProductDetailsResponseListener
        listener.onProductDetailsResponse(
            BillingResult.newBuilder().setResponseCode(responseCode).build(),
            productDetailsList
        )
    }
}

internal fun BillingClient.mockQueryPurchasesAsync(
    @BillingClient.BillingResponseCode responseCode: Int,
    purchases: List<Purchase>
) {
    every { queryPurchasesAsync(any<QueryPurchasesParams>(), any()) } answers {
        val listener = args[1] as PurchasesResponseListener
        listener.onQueryPurchasesResponse(
            BillingResult.newBuilder().setResponseCode(responseCode).build(),
            purchases
        )
    }
}
