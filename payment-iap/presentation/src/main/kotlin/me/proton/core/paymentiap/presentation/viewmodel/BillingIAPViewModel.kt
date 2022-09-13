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
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class BillingIAPViewModel @Inject constructor(
    private val billingRepository: GoogleBillingRepository
) : ProtonViewModel() {

    private val _billingIAPState = MutableStateFlow<State>(State.Initializing)
    val billingIAPState: StateFlow<State> = _billingIAPState.asStateFlow()

    sealed class State {
        object Initializing : State()
        object QueryingProductDetails : State()
        object PurchaseStarted : State()

        sealed class Success : State() {
            data class GoogleProductDetails(
                val amount: Long,
                val currency: String,
                val formattedPriceAndCurrency: String,
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

    private lateinit var selectedProduct: ProductDetails

    init {
        listenForPurchases()
    }

    fun queryProductDetails(googlePlanName: String) = viewModelScope.launch {
        flow {
            emit(State.QueryingProductDetails)
            val productDetails = billingRepository.getProductDetails(googlePlanName)
            if (productDetails == null) {
                emit(State.Error.ProductDetailsError.ProductMismatch)
                return@flow
            }

            selectedProduct = productDetails

            val price = productDetails.getProductPrice()
            if (price == null) {
                emit(State.Error.ProductDetailsError.Price)
            } else {
                emit(
                    State.Success.GoogleProductDetails(
                        amount = price.priceAmountMicros,
                        currency = price.priceCurrencyCode,
                        formattedPriceAndCurrency = price.formattedPrice,
                    )
                )
            }
        }.catch { throwable ->
            val error = when (throwable) {
                is BillingClientError -> {
                    when (throwable.responseCode) {
                        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> State.Error.BillingClientUnavailable
                        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> State.Error.BillingClientDisconnected
                        else -> State.Error.ProductDetailsError.ResponseCode
                    }
                }
                else -> State.Error.ProductDetailsError.Message(throwable.message)
            }
            _billingIAPState.tryEmit(error)
        }.onEach { subscriptionState ->
            _billingIAPState.tryEmit(subscriptionState)
        }.launchIn(viewModelScope)
    }

    private fun listenForPurchases() = viewModelScope.launch {
        billingRepository.purchaseUpdated.collect {
            onPurchasesUpdated(it.first, it.second)
        }
    }

    // Launch Purchase flow
    fun launchBillingFlow(userId: String?, activity: FragmentActivity): Job = flow {
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

        billingRepository.launchBillingFlow(activity, billingFlowParamsBuilder.build())
        emit(State.Success.PurchaseFlowLaunched)
    }.catch { error ->
        _billingIAPState.tryEmit(State.Error.ProductDetailsError.Message(error.message))
    }.onEach { subscriptionState ->
        _billingIAPState.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    override fun onCleared() {
        billingRepository.destroy()
    }

    private fun ProductDetails.getProductPrice(): ProductDetails.PricingPhase? {
        return subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)
    }

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                val purchase = purchases.firstOrNull { purchase ->
                    purchase.products.contains(selectedProduct.productId)
                }
                if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    _billingIAPState.value = State.Success.PurchaseSuccess(
                        responseCode = billingResult.responseCode,
                        productId = selectedProduct.productId,
                        purchaseToken = purchase.purchaseToken,
                        orderID = purchase.orderId
                    )
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
}
