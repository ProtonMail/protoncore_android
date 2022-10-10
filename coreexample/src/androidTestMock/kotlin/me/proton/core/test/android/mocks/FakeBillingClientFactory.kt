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

@file:Suppress("DEPRECATION")

package me.proton.core.test.android.mocks

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageResponseListener
import com.android.billingclient.api.PriceChangeConfirmationListener
import com.android.billingclient.api.PriceChangeFlowParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import io.mockk.mockk
import me.proton.core.paymentiap.domain.BillingClientFactory

class FakeBillingClientFactory : BillingClientFactory {
    private val _listeners = mutableListOf<PurchasesUpdatedListener>()

    val billingClient: BillingClient = mockk()
    val listeners: List<PurchasesUpdatedListener> = _listeners

    override fun invoke(purchasesUpdatedListener: PurchasesUpdatedListener): BillingClient {
        _listeners.add(purchasesUpdatedListener)
        return FakeBillingClient(billingClient) { _listeners.remove(purchasesUpdatedListener) }
    }
}

@Suppress("OVERRIDE_DEPRECATION")
private class FakeBillingClient(
    private val mock: BillingClient,
    private val onConnectionEnd: () -> Unit
) : BillingClient() {
    override fun getConnectionState(): Int = mock.connectionState

    override fun isFeatureSupported(feature: String): BillingResult = mock.isFeatureSupported(feature)

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult =
        mock.launchBillingFlow(activity, params)

    override fun showInAppMessages(
        activity: Activity,
        params: InAppMessageParams,
        listener: InAppMessageResponseListener
    ): BillingResult = mock.showInAppMessages(activity, params, listener)

    override fun acknowledgePurchase(params: AcknowledgePurchaseParams, listener: AcknowledgePurchaseResponseListener) =
        mock.acknowledgePurchase(params, listener)

    override fun consumeAsync(params: ConsumeParams, listener: ConsumeResponseListener) =
        mock.consumeAsync(params, listener)

    override fun endConnection() {
        mock.endConnection()
        onConnectionEnd()
    }

    override fun launchPriceChangeConfirmationFlow(
        activity: Activity,
        params: PriceChangeFlowParams,
        listener: PriceChangeConfirmationListener
    ) = mock.launchPriceChangeConfirmationFlow(activity, params, listener)

    override fun queryProductDetailsAsync(params: QueryProductDetailsParams, listener: ProductDetailsResponseListener) =
        mock.queryProductDetailsAsync(params, listener)

    override fun queryPurchaseHistoryAsync(
        params: QueryPurchaseHistoryParams,
        listener: PurchaseHistoryResponseListener
    ) = mock.queryPurchaseHistoryAsync(params, listener)

    override fun queryPurchaseHistoryAsync(skuType: String, listener: PurchaseHistoryResponseListener) =
        mock.queryPurchaseHistoryAsync(skuType, listener)

    override fun queryPurchasesAsync(params: QueryPurchasesParams, listener: PurchasesResponseListener) =
        mock.queryPurchasesAsync(params, listener)

    override fun queryPurchasesAsync(skuType: String, listener: PurchasesResponseListener) =
        mock.queryPurchasesAsync(skuType, listener)

    override fun querySkuDetailsAsync(params: SkuDetailsParams, listener: SkuDetailsResponseListener) =
        mock.querySkuDetailsAsync(params, listener)

    override fun startConnection(listener: BillingClientStateListener) = mock.startConnection(listener)

    override fun isReady(): Boolean = mock.isReady
}
