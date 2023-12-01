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

package me.proton.core.paymentiap.presentation.usecase

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.first
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingPurchaseTotal
import me.proton.core.observability.domain.metrics.toPurchaseGiapStatus
import me.proton.core.payment.domain.entity.GoogleBillingFlowParams
import me.proton.core.payment.domain.entity.GoogleBillingResult
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.domain.toGiapStatus
import javax.inject.Inject
import javax.inject.Provider

public class LaunchGiapBillingFlowImpl @Inject constructor(
    private val googleBillingRepository: Provider<GoogleBillingRepository<Activity>>,
    private val observabilityManager: ObservabilityManager
) : LaunchGiapBillingFlow<Activity> {
    /**
     * @throws BillingClientError
     */
    override suspend fun invoke(
        activity: Activity,
        googleProductId: ProductId,
        params: GoogleBillingFlowParams
    ): LaunchGiapBillingFlow.Result {
        return googleBillingRepository.get().use { repository ->
            repository.launchBillingFlow {
                it.launchBilling(activity, params)
            }
            val (googleBillingResult, purchases) = repository.purchaseUpdated.first()
            onPurchasesUpdated(googleBillingResult, googleProductId, purchases)
        }
    }

    private fun onPurchasesUpdated(
        googleBillingResult: GoogleBillingResult,
        googleProductId: ProductId,
        purchases: List<GooglePurchase>?
    ): LaunchGiapBillingFlow.Result {
        val billingResult = googleBillingResult.unwrap()
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull { purchase ->
                    purchase.productIds.any { it == googleProductId }
                }?.unwrap()
                if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    onSubscriptionPurchased(googleProductId, purchase).also {
                        enqueuePurchaseResult(billingResult, it)
                    }
                } else {
                    enqueuePurchaseResult(billingResult, null)
                    LaunchGiapBillingFlow.Result.Error.PurchaseNotFound
                }
            }
            else -> {
                enqueuePurchaseResult(billingResult, null)
                throw BillingClientError(billingResult.responseCode, billingResult.debugMessage)
            }
        }
    }

    private fun onSubscriptionPurchased(
        googleProductId: ProductId,
        purchase: Purchase
    ): LaunchGiapBillingFlow.Result {
        val purchaseCustomerId = requireNotNull(purchase.accountIdentifiers?.obfuscatedAccountId) {
            "The purchase should contain a customer ID."
        }
        requireNotNull(purchase.products.firstOrNull { it == googleProductId.id }) {
            "The purchase should contain a product ID."
        }

        return if (purchaseCustomerId.isBlank()) {
            LaunchGiapBillingFlow.Result.Error.EmptyCustomerId
        } else {
            LaunchGiapBillingFlow.Result.PurchaseSuccess(purchase.wrap())
        }
    }

    private fun enqueuePurchaseResult(
        billingResult: BillingResult,
        result: LaunchGiapBillingFlow.Result?
    ) {
        val event = if (result == LaunchGiapBillingFlow.Result.Error.EmptyCustomerId) {
            CheckoutGiapBillingPurchaseTotal(CheckoutGiapBillingPurchaseTotal.PurchaseStatus.incorrectCustomerId)
        } else {
            CheckoutGiapBillingPurchaseTotal(billingResult.toGiapStatus().toPurchaseGiapStatus())
        }
        observabilityManager.enqueue(event)
    }
}
