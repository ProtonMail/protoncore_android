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
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingLaunchBillingTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingProductQueryTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingPurchaseTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingQuerySubscriptionsTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingUnredeemedTotalV1
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.common.GiapStatus
import me.proton.core.observability.domain.runWithObservability
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import me.proton.core.paymentiap.domain.toGiapStatus
import me.proton.core.paymentiap.presentation.LogTag
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
internal class BillingIAPViewModel @Inject constructor(
    private val billingRepository: GoogleBillingRepository,
    private val findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase,
    private val observabilityManager: ObservabilityManager
) : ProtonViewModel() {

    private val _billingIAPState = MutableStateFlow<State>(State.Initializing)
    val billingIAPState: StateFlow<State> = _billingIAPState.asStateFlow()

    sealed class State {
        object Initializing : State()
        object QueryingProductDetails : State()
        object PurchaseStarted : State()
        data class UnredeemedPurchase(val purchase: GooglePurchase) : State()

        sealed class Success : State() {
            data class GoogleProductDetails(
                val amount: Long,
                val currency: String,
                val formattedPriceAndCurrency: String,
            ) : Success()

            object PurchaseFlowLaunched : State()

            data class PurchaseSuccess(
                val productId: String,
                val purchaseToken: GooglePurchaseToken,
                val orderID: String,
                val customerId: String
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

    override fun onCleared() {
        billingRepository.destroy()
    }

    fun queryProductDetails(googlePlanName: String) = viewModelScope.launch {
        flow {
            emit(State.QueryingProductDetails)

            val productDetails = runWithObservability(
                observabilityManager,
                { it.observeProductDetailsResult(googlePlanName) }
            ) {
                billingRepository.getProductDetails(googlePlanName)
            }
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

    fun makePurchase(activity: FragmentActivity, customerId: String): Job = flow {
        require(this@BillingIAPViewModel::selectedProduct.isInitialized) {
            "Product must be set before making the purchase."
        }

        val unredeemedPurchase = findUnacknowledgedGooglePurchase.byProduct(
            selectedProduct.productId,
            querySubscriptionsMetricData = { result ->
                result.toGiapStatus()?.let { CheckoutGiapBillingQuerySubscriptionsTotal(it) }
            }
        )
        if (unredeemedPurchase != null) {
            observabilityManager.enqueue(CheckoutGiapBillingUnredeemedTotalV1())
            emit(State.UnredeemedPurchase(unredeemedPurchase))
        } else {
            launchBillingFlow(activity, customerId)
        }
    }.catch { error ->
        _billingIAPState.tryEmit(State.Error.ProductDetailsError.Message(error.message))
    }.onEach { subscriptionState ->
        _billingIAPState.tryEmit(subscriptionState)
    }.launchIn(viewModelScope)

    fun redeemExistingPurchase(purchase: GooglePurchase) = viewModelScope.launch {
        onSubscriptionPurchased(purchase.unwrap())
    }

    /** Processes the result of a product query (i.e. observability, logs). */
    private fun Result<ProductDetails?>.observeProductDetailsResult(googlePlanName: String): ObservabilityData? {
        val status = toGiapStatus()
        if (status == GiapStatus.notFound) {
            CoreLogger.e(LogTag.DEFAULT, GoogleProductNotFound(googlePlanName))
        }
        return status?.let { CheckoutGiapBillingProductQueryTotal(it) }
    }

    /** Launches Google billing flow. */
    private suspend fun FlowCollector<State>.launchBillingFlow(
        activity: FragmentActivity,
        customerId: String
    ) {
        val offer = requireNotNull(selectedProduct.subscriptionOfferDetails?.firstOrNull())
        emit(State.PurchaseStarted)
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(selectedProduct)
                .setOfferToken(offer.offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(customerId)
            .build()

        billingRepository.runWithObservability(
            observabilityManager,
            { result -> result.toGiapStatus()?.let { CheckoutGiapBillingLaunchBillingTotal(it) } }
        ) {
            launchBillingFlow(activity, billingFlowParams)
        }
        emit(State.Success.PurchaseFlowLaunched)
    }

    private fun listenForPurchases() = billingRepository.purchaseUpdated.onEach {
        onPurchasesUpdated(it.first, it.second)
    }.launchIn(viewModelScope)

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                val purchase = purchases.firstOrNull { purchase ->
                    purchase.products.contains(selectedProduct.productId)
                }
                if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    onSubscriptionPurchased(purchase)
                }
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                _billingIAPState.value = State.Error.ProductPurchaseError.UserCancelled
            }
            billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _billingIAPState.value = State.Error.ProductPurchaseError.ItemAlreadyOwned
            }
            else -> {
                _billingIAPState.value =
                    State.Error.ProductPurchaseError.Message(code = billingResult.responseCode)
            }
        }

        billingResult.toGiapStatus()?.let {
            observabilityManager.enqueue(CheckoutGiapBillingPurchaseTotal(it))
        }
    }

    private fun onSubscriptionPurchased(purchase: Purchase) {
        _billingIAPState.value = State.Success.PurchaseSuccess(
            productId = selectedProduct.productId,
            purchaseToken = GooglePurchaseToken(purchase.purchaseToken),
            orderID = purchase.orderId,
            customerId = requireNotNull(purchase.accountIdentifiers?.obfuscatedAccountId)
        )
    }
}

private fun ProductDetails.getProductPrice(): ProductDetails.PricingPhase? =
    subscriptionOfferDetails?.getOrNull(0)?.pricingPhases?.pricingPhaseList?.getOrNull(0)

private class GoogleProductNotFound(googlePlanName: String) :
    Throwable("Google product not found: $googlePlanName.")
