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

package me.proton.core.paymentiap.data.repository

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.paymentiap.domain.BillingClientFactory
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import me.proton.core.util.kotlin.DispatcherProvider
import me.proton.core.util.kotlin.coroutine.result
import javax.inject.Inject

public class GoogleBillingRepositoryImpl @Inject internal constructor(
    connectedBillingClientFactory: ConnectedBillingClientFactory,
    dispatcherProvider: DispatcherProvider,
) : GoogleBillingRepository {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcherProvider.Main)

    private val _purchaseUpdated = MutableSharedFlow<Pair<BillingResult, List<Purchase>?>>(extraBufferCapacity = 10)
    public override val purchaseUpdated: Flow<Pair<BillingResult, List<Purchase>?>> = _purchaseUpdated

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchaseList ->
        scope.launch {
            _purchaseUpdated.emit(billingResult to purchaseList)
        }
    }

    private val connectedBillingClient = connectedBillingClientFactory(purchasesUpdatedListener)

    override suspend fun acknowledgePurchase(purchaseToken: GooglePurchaseToken) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken.value)
            .build()

        val result = connectedBillingClient.withClient { it.acknowledgePurchase(params) }
        result.checkOk()
    }

    override fun destroy() {
        scope.cancel()
        connectedBillingClient.destroy()
    }

    override suspend fun getProductDetails(googlePlayPlanName: String): ProductDetails? {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(googlePlayPlanName)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
        val result = connectedBillingClient.withClient { it.queryProductDetails(params) }
        result.billingResult.checkOk()

        return result.productDetailsList?.firstOrNull()
    }

    override suspend fun launchBillingFlow(activity: Activity, billingFlowParams: BillingFlowParams) {
        connectedBillingClient.withClient { it.launchBillingFlow(activity, billingFlowParams) }
    }

    override suspend fun querySubscriptionPurchases(): List<Purchase> = result("querySubscriptionPurchases") {
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        val result = connectedBillingClient.withClient { it.queryPurchasesAsync(params) }
        result.billingResult.checkOk()
        result.purchasesList
    }

    private fun BillingResult.checkOk() {
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            throw BillingClientError(responseCode, debugMessage)
        }
    }
}

@AssistedFactory
internal interface ConnectedBillingClientFactory {
    operator fun invoke(purchasesUpdatedListener: PurchasesUpdatedListener): ConnectedBillingClient
}

/** Manages access to [BillingClient], ensuring we are connected, before calling any of its methods. */
internal class ConnectedBillingClient @AssistedInject constructor(
    billingClientFactory: BillingClientFactory,
    @Assisted private val purchasesUpdatedListener: PurchasesUpdatedListener
) : BillingClientStateListener {
    private val billingClient = billingClientFactory(purchasesUpdatedListener)
    private val connectionState = MutableStateFlow<BillingClientConnectionState>(BillingClientConnectionState.Idle)

    fun destroy() {
        billingClient.endConnection()
        connectionState.value = BillingClientConnectionState.Destroyed
    }

    suspend fun <T> withClient(body: suspend (BillingClient) -> T): T {
        waitForConnection()
        return body(billingClient)
    }

    override fun onBillingServiceDisconnected() {
        connectionState.value = BillingClientConnectionState.Error(
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            "Service disconnected."
        )
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            connectionState.value = BillingClientConnectionState.Connected
        } else {
            connectionState.value =
                BillingClientConnectionState.Error(billingResult.responseCode, billingResult.debugMessage)
        }
    }

    private fun connect() {
        if (connectionState.value.isConnectingOrConnected()) return
        connectionState.value = BillingClientConnectionState.Connecting
        billingClient.startConnection(this)
    }

    private suspend fun waitForConnection() {
        val currentConnectionState = connectionState.value
        check(currentConnectionState != BillingClientConnectionState.Destroyed) {
            "Billing client has already been destroyed."
        }
        connect()
        connectionState
            .onEach {
                if (it is BillingClientConnectionState.Error) {
                    throw BillingClientError(it.responseCode, it.debugMessage)
                }
            }
            .first { it == BillingClientConnectionState.Connected }
    }

    private sealed class BillingClientConnectionState {
        object Idle : BillingClientConnectionState()
        object Connecting : BillingClientConnectionState()
        object Connected : BillingClientConnectionState()
        object Destroyed : BillingClientConnectionState()
        data class Error(val responseCode: Int?, val debugMessage: String?) : BillingClientConnectionState()

        fun isConnectingOrConnected(): Boolean = this is Connecting || this is Connected
    }
}
