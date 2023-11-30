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

package me.proton.core.paymentiap.presentation.viewmodel

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import me.proton.core.domain.entity.AppStore
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingLaunchBillingTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingProductQueryTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingPurchaseTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingQuerySubscriptionsTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingUnredeemedTotalV1
import me.proton.core.observability.domain.metrics.toPurchaseGiapStatus
import me.proton.core.payment.domain.entity.GoogleBillingResult
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.paymentiap.domain.entity.GoogleProductPrice
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.domain.firstPriceOrNull
import me.proton.core.paymentiap.domain.toGiapStatus
import me.proton.core.presentation.savedstate.state
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.util.kotlin.coroutine.launchWithResultContext
import me.proton.core.util.kotlin.takeIfNotEmpty
import javax.inject.Inject

@HiltViewModel
internal class BillingIAPViewModel @Inject constructor(
    private val billingRepository: GoogleBillingRepository<Activity>,
    private val findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase,
    override val observabilityManager: ObservabilityManager,
    savedStateHandle: SavedStateHandle
) : ProtonViewModel(), ObservabilityContext {

    private var billingInput: BillingInput? by savedStateHandle.state(null)

    private val _billingIAPState = MutableStateFlow<State>(State.Initializing)
    val billingIAPState: StateFlow<State> = _billingIAPState.asStateFlow()

    sealed class State {
        object Initializing : State()
        object QueryingProductDetails : State()
        object PurchaseStarted : State()
        data class UnredeemedPurchase(val purchase: GooglePurchase) : State()

        sealed class Success : State() {
            data class GoogleProductsDetails(
                val details: Map<ProductId, GoogleProductPrice>
            ): State()

            object PurchaseFlowLaunched : State()

            data class PurchaseSuccess(
                val productId: String,
                val purchaseToken: GooglePurchaseToken,
                val orderID: String,
                val customerId: String,
                val billingInput: BillingInput
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
                    @BillingClient.BillingResponseCode val code: Int? = null
                ) : ProductPurchaseError()

                object IncorrectCustomerId : ProductPurchaseError()
            }
        }
    }

    private lateinit var queriedProducts: Map<ProductId, ProductDetails>

    init {
        listenForPurchases()
    }

    override fun onCleared() {
        super.onCleared()
        billingRepository.destroy()
    }

    fun queryProductDetails(googlePlanName: String) =
        queryProductDetails(listOf(ProductId(googlePlanName)))

    fun queryProductDetails(googlePlanNames: List<ProductId>) = viewModelScope.launchWithResultContext {
        onResultEnqueueObservability("getProductsDetails") { CheckoutGiapBillingProductQueryTotal(toGiapStatus()) }

        flow {
            emit(State.QueryingProductDetails)

            // Yield to enable collector to consume QueryingProductDetails before billing repo error is emitted.
            yield()

            val productsDetails = billingRepository.getProductsDetails(googlePlanNames)
                ?.takeIfNotEmpty()
                ?.map { it.unwrap() }
            if (productsDetails == null) {
                emit(State.Error.ProductDetailsError.ProductMismatch)
                return@flow
            }

            queriedProducts = productsDetails.associateBy { ProductId(it.productId) }

            val havePrices = productsDetails.all { it.firstPriceOrNull() != null }
            if (!havePrices) {
                emit(State.Error.ProductDetailsError.Price)
            } else {
                emit(
                    State.Success.GoogleProductsDetails(
                        productsDetails.associate {
                            val price = requireNotNull(it.firstPriceOrNull())
                            ProductId(it.productId) to GoogleProductPrice(
                                priceAmountMicros = price.priceAmountMicros,
                                currency = price.priceCurrencyCode,
                                formattedPriceAndCurrency = price.formattedPrice,
                            )
                        }
                    )
                )
            }
        }.catch { throwable ->
            val error = when (throwable) {
                is BillingClientError -> {
                    when (throwable.responseCode) {
                        null,
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
        }.collect()
    }

    fun makePurchase(activity: FragmentActivity, input: BillingInput): Job =
        viewModelScope.launchWithResultContext {
            billingInput = input

            onResultEnqueueObservability("querySubscriptionPurchases") {
                CheckoutGiapBillingQuerySubscriptionsTotal(
                    toGiapStatus()
                )
            }
            onResultEnqueueObservability("launchBillingFlow") {
                CheckoutGiapBillingLaunchBillingTotal(
                    toGiapStatus()
                )
            }

            flow {
                val productId = input.googlePlanName
                val product = requireNotNull(queriedProducts[ProductId(productId)]) {
                    "$productId details must be fetched before making the purchase."
                }
                val unredeemedPurchase =
                    findUnacknowledgedGooglePurchase.byProduct(ProductId(product.productId))
                if (unredeemedPurchase != null) {
                    observabilityManager.enqueue(CheckoutGiapBillingUnredeemedTotalV1())
                    emit(State.UnredeemedPurchase(unredeemedPurchase))
                } else {
                    launchBillingFlow(activity, input.googleCustomerId, product)
                }
            }.catch { error ->
                _billingIAPState.tryEmit(State.Error.ProductDetailsError.Message(error.message))
            }.onEach { subscriptionState ->
                _billingIAPState.tryEmit(subscriptionState)
            }.collect()
        }

    fun redeemExistingPurchase(purchase: GooglePurchase) = viewModelScope.launch {
        val state = onSubscriptionPurchased(purchase.unwrap())
        _billingIAPState.value = state
    }

    /** Launches Google billing flow. */
    private suspend fun FlowCollector<State>.launchBillingFlow(
        activity: FragmentActivity,
        customerId: String,
        product: ProductDetails
    ) {
        val offer = requireNotNull(product.subscriptionOfferDetails?.firstOrNull())
        emit(State.PurchaseStarted)

        // Yield to enable collector to consume PurchaseStarted before billing repo error is emitted.
        yield()

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(product)
                .setOfferToken(offer.offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(customerId)
            .build()

        billingRepository.launchBillingFlow(activity, billingFlowParams.wrap())
        emit(State.Success.PurchaseFlowLaunched)
    }

    private fun listenForPurchases() = billingRepository.purchaseUpdated.onEach {
        onPurchasesUpdated(it.first, it.second)
    }.launchIn(viewModelScope)

    private fun onPurchasesUpdated(googleBillingResult: GoogleBillingResult, purchases: List<GooglePurchase>?) {
        val billingResult = googleBillingResult.unwrap()
        val state = when {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty() -> {
                val queriedProductIds = queriedProducts.keys.map { it.id }
                val purchase = purchases.firstOrNull{ purchase ->
                    purchase.productIds.any { it.id in queriedProductIds }
                }?.unwrap()
                if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    onSubscriptionPurchased(purchase)
                } else {
                    State.Error.ProductPurchaseError.Message(code = billingResult.responseCode)
                }
            }

            billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED ->
                State.Error.ProductPurchaseError.UserCancelled

            billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
                State.Error.ProductPurchaseError.ItemAlreadyOwned

            else ->
                State.Error.ProductPurchaseError.Message(code = billingResult.responseCode)
        }
        _billingIAPState.value = state
        enqueuePurchaseResult(billingResult, state)
    }

    private fun onSubscriptionPurchased(purchase: Purchase): State {
        val billingInput = requireNotNull(billingInput) {
            "BillingInput should not be null, after a subscription has been purchased."
        }
        val purchaseCustomerId = requireNotNull(purchase.accountIdentifiers?.obfuscatedAccountId) {
            "The purchase should contain a customer ID."
        }
        val productId = requireNotNull(purchase.products.firstOrNull()) {
            "The purchase should contain a product ID."
        }

        return if (purchaseCustomerId.isEmpty()) {
            State.Error.ProductPurchaseError.IncorrectCustomerId
        } else {
            State.Success.PurchaseSuccess(
                productId = productId,
                purchaseToken = GooglePurchaseToken(purchase.purchaseToken),
                orderID = purchase.orderId,
                customerId = purchaseCustomerId,
                billingInput = billingInput
            )
        }
    }

    private fun enqueuePurchaseResult(billingResult: BillingResult, state: State) {
        val event = if (state == State.Error.ProductPurchaseError.IncorrectCustomerId) {
            CheckoutGiapBillingPurchaseTotal(CheckoutGiapBillingPurchaseTotal.PurchaseStatus.incorrectCustomerId)
        } else {
            CheckoutGiapBillingPurchaseTotal(billingResult.toGiapStatus().toPurchaseGiapStatus())
        }
        observabilityManager.enqueue(event)
    }
}

private val BillingInput.googleCustomerId: String
    get() = requireNotNull(plan.vendors[AppStore.GooglePlay]?.customerId) {
        "Missing Vendor data for Google Play."
    }

private val BillingInput.googlePlanName: String
    get() = requireNotNull(plan.vendors[AppStore.GooglePlay]?.vendorPlanName) {
        "Missing Vendor data for Google Play."
    }
