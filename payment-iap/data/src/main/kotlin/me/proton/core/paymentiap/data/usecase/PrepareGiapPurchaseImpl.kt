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

package me.proton.core.paymentiap.data.usecase

import android.app.Activity
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams
import com.android.billingclient.api.BillingFlowParams.SubscriptionUpdateParams.ReplacementMode
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.paymentiap.domain.LogTag
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.plan.domain.usecase.GetProductIdForCurrentSubscription
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject
import javax.inject.Provider

public class PrepareGiapPurchaseImpl @Inject constructor(
    private val billingRepository: Provider<GoogleBillingRepository<Activity>>,
    private val findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase,
    private val getProductIdForCurrentSubscription: GetProductIdForCurrentSubscription
) : PrepareGiapPurchase {
    /**
     * @throws BillingClientError
     */
    override suspend fun invoke(
        googleCustomerId: String,
        googleProductId: ProductId,
        userId: UserId?
    ): PrepareGiapPurchase.Result {
        val productsDetails = billingRepository.get().use {
            it.getProductsDetails(listOf(googleProductId))?.firstOrNull()?.unwrap()
        } ?: return PrepareGiapPurchase.Result.ProductDetailsNotFound

        val unredeemedPurchase =
            findUnacknowledgedGooglePurchase.byProduct(ProductId(productsDetails.productId))
        if (unredeemedPurchase != null) {
            return PrepareGiapPurchase.Result.Unredeemed(unredeemedPurchase)
        }

        val offer = requireNotNull(productsDetails.subscriptionOfferDetails?.firstOrNull()) {
            "Missing subscriptionOfferDetails for $googleProductId."
        }
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productsDetails)
                .setOfferToken(offer.offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .setObfuscatedAccountId(googleCustomerId)
            .apply {
                getExistingGooglePurchase(userId)?.let { currentGooglePurchase ->
                    setSubscriptionUpdateParams(
                        SubscriptionUpdateParams.newBuilder()
                            .setOldPurchaseToken(currentGooglePurchase.purchaseToken.value)
                            .setSubscriptionReplacementMode(ReplacementMode.WITH_TIME_PRORATION)
                            .build()
                    )
                }
            }
            .build()

        return PrepareGiapPurchase.Result.Success(billingFlowParams.wrap())
    }

    private suspend fun getExistingGooglePurchase(userId: UserId?): GooglePurchase? {
        val currentProductId = userId?.let { getProductIdForCurrentSubscription(it) } ?: return null
        val existingPurchases = billingRepository.get().use { it.querySubscriptionPurchases() }
        return existingPurchases.firstOrNull {
            currentProductId in it.productIds
        }.also {
            CoreLogger.i(LogTag.GIAP_INFO, "$TAG, Existing subscription: $it")
        }
    }

    private companion object {
        private const val TAG = "PrepareGiapPurchaseImpl"
    }
}
