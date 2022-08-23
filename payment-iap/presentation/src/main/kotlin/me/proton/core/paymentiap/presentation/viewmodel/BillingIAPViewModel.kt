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

package me.proton.core.paymentiap.presentation.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.paymentiap.presentation.LogTag.DEFAULT
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
internal class BillingIAPViewModel @Inject constructor(
    billingClientBuilder: BillingClient.Builder
) : ProtonViewModel(), PurchasesUpdatedListener {

    private val _billingIAPState = MutableStateFlow<State>(State.Initializing)
    val billingIAPState: StateFlow<State> = _billingIAPState.asStateFlow()

    sealed class State {
        object Initializing : State()
        object Initialized : State()
        object QueryingProductDetails : State()
        object PurchaseStarted : State()

        sealed class Success : State() {
            data class GoogleProductDetails(
                val amount: Long,
                val currency: String,
                val formattedPriceAndCurrency: String,
                val billingResult: BillingResult
            ) : Success()

            object PurchaseFlowLaunched : State()

            data class PurchaseSuccess(
                val responseCode: Int,
                val productId: String,
                val purchaseToken: String,
                val orderID: String
            ) : Success()
        }

        sealed class Error : State() {
            object BillingClientUnavailable : State()
            object BillingClientDisconnected : State()
            sealed class ProductDetailsError : State() {
                object ResponseCode : ProductDetailsError()
                object Price : ProductDetailsError()
                object ProductMismatch : ProductDetailsError()
                data class Message(val error: String? = null) : ProductDetailsError()
            }

            sealed class ProductPurchaseError : State() {
                object UserCancelled : ProductPurchaseError()
                object ItemAlreadyOwned : ProductDetailsError()
                data class Message(
                    val error: Int? = null,
                    @BillingClient.BillingResponseCode val code: Int
                ) : ProductPurchaseError()
            }
        }
    }

    private lateinit var googlePlanName: String
    private lateinit var selectedProduct: ProductDetails
    private var billingClient: BillingClient = billingClientBuilder.setListener(this).build()

    private val billingClientStateListener = object : BillingClientStateListener {

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            val state = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                viewModelScope.launch(Dispatchers.IO) {
                    queryProductDetails()
                }
                State.Initialized
            } else {
                State.Error.BillingClientUnavailable
            }
            _billingIAPState.value = state
        }

        override fun onBillingServiceDisconnected() {
            _billingIAPState.value = State.Error.BillingClientDisconnected
            billingClient.startConnection(this)
        }
    }

    private fun initialize() {
        _billingIAPState.value = State.Initializing
        billingClient.startConnection(billingClientStateListener)
    }

    fun queryProductDetails(planName: String) {
        googlePlanName = planName
        initialize()
    }

    private fun queryProductDetails() = flow {
        emit(State.QueryingProductDetails)
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(googlePlanName)
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val productList = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        val result = billingClient.queryProductDetails(productList)
        if (result.billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            emit(State.Error.ProductDetailsError.ResponseCode)
        } else {
            val productDetailsList = result.productDetailsList
            if (productDetailsList != null) {
                selectedProduct = productDetailsList.first()
            } else {
                emit(State.Error.ProductDetailsError.ProductMismatch)
                return@flow
            }
            val price = result.getProductPrice()
            if (price == null) {
                emit(State.Error.ProductDetailsError.Price)
            } else {
                emit(
                    State.Success.GoogleProductDetails(
                        amount = price.priceAmountMicros,
                        currency = price.priceCurrencyCode,
                        formattedPriceAndCurrency = price.formattedPrice,
                        result.billingResult
                    )
                )
            }
        }
    }.catch { error ->
        _billingIAPState.tryEmit(State.Error.ProductDetailsError.Message(error.message))
    }.onEach { subscriptionState ->
        _billingIAPState.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    // Launch Purchase flow
    fun launchBillingFlow(userId: String?, activity: FragmentActivity): Job = flow {
        if (!billingClient.isReady) {
            CoreLogger.i(DEFAULT, "launchBillingFlow: BillingClient is not ready")
        }

        require(this@BillingIAPViewModel::selectedProduct.isInitialized) {
            "Product must be set before launching the billing flow."
        }
        val offer = requireNotNull(selectedProduct.subscriptionOfferDetails?.firstOrNull())
        emit(State.PurchaseStarted)
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(selectedProduct)
                .setOfferToken(offer.offerToken)
                .build()
        )
        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)

        if (userId != null) {
            billingFlowParamsBuilder.setObfuscatedAccountId(userId)
        }

        billingClient.launchBillingFlow(activity, billingFlowParamsBuilder.build())
        emit(State.Success.PurchaseFlowLaunched)
    }.catch { error ->
        _billingIAPState.tryEmit(State.Error.ProductDetailsError.Message(error.message))
    }.onEach { subscriptionState ->
        _billingIAPState.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    override fun onCleared() {
        billingClient.endConnection()
    }

    private fun ProductDetailsResult.getProductPrice(): ProductDetails.PricingPhase? {
        val offerDetails = productDetailsList?.getOrNull(0)?.subscriptionOfferDetails
        return offerDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                // handle the purchases
                for (purchase in purchases) {
                    acknowledgePurchases(purchase)
                }
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingIAPState.value = State.Error.ProductPurchaseError.UserCancelled
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingIAPState.value = State.Error.ProductPurchaseError.ItemAlreadyOwned
            }
            else -> {
                _billingIAPState.value = State.Error.ProductPurchaseError.Message(code = billingResult.responseCode)
            }
        }
    }

    // Perform new subscription purchases' acknowledgement client side.
    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                // TODO: maybe this acknowledge purchase should happen after successful subscription creation on our BE?
                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        _billingIAPState.value = State.Success.PurchaseSuccess(
                            responseCode = billingResult.responseCode,
                            productId = selectedProduct.productId,
                            purchaseToken = it.purchaseToken,
                            orderID = it.orderId
                        )
                    }
                }
            }
        }
    }
}
