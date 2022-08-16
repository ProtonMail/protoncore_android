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

import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
public class BillingIAPViewModel @Inject constructor(
    private val billingClient: BillingClient
) : ProtonViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Initializing)
    public val state: StateFlow<State> = mutableState.asStateFlow()

    public sealed class State {
        public object Initializing : State()
        public object Unavailable : State()
        public object Initialized : State()
        public object Disconnected : State()
        public object Querying : State()
        public data class ProductList(
            val details: List<ProductDetails>?,
            val billingResult: BillingResult
        ) : State()

        public data class Flowing(@BillingClient.BillingResponseCode val responseCode: Int) : State()
        public sealed class PurchaseResult : State() {
            public data class Success(val purchase: Purchase) : PurchaseResult()
            public data class Error(@BillingClient.BillingResponseCode val responseCode: Int) : PurchaseResult()
        }
    }


    private val billingClientStateListener = object : BillingClientStateListener {

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                mutableState.value = State.Initialized
            } else {
                mutableState.value = State.Unavailable
            }
        }

        override fun onBillingServiceDisconnected() {
            mutableState.value = State.Disconnected
            billingClient.startConnection(this)
        }
    }


    init {
        initialize()
    }

    private fun initialize() = viewModelScope.launch {
        mutableState.emit(State.Initializing)
        billingClient.startConnection(billingClientStateListener)
    }

    public fun queryProductDetails(planName: String) {
        viewModelScope.launch {
            mutableState.emit(State.Querying)

            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(planName)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            val productList = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

            val result = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(productList)
            }

            mutableState.emit(State.ProductList(result.productDetailsList, result.billingResult))
        }
    }
}
