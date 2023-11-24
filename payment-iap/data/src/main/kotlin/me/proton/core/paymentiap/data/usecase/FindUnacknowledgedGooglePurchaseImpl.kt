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
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.paymentiap.domain.entity.unwrap
import javax.inject.Inject
import javax.inject.Provider

public class FindUnacknowledgedGooglePurchaseImpl @Inject constructor(
    private val billingRepositoryProvider: Provider<GoogleBillingRepository<Activity>>
) : FindUnacknowledgedGooglePurchase {
    public override suspend operator fun invoke(): List<GooglePurchase> {
        return runCatching {
            billingRepositoryProvider.get().use { repository ->
                repository.querySubscriptionPurchases()
                    .filter { it.unwrap().isPurchasedButNotAcknowledged() }
                    .sortedByDescending { it.unwrap().purchaseTime }
            }
        }.getOrElse {
            if (it is BillingClientError && it.responseCode in ALLOWED_BILLING_ERRORS) {
                emptyList()
            } else {
                throw it
            }
        }
    }

    override suspend fun byCustomer(customerId: String): GooglePurchase? {
        return invoke().find { purchase ->
            purchase.customerId == customerId
        }
    }

    override suspend fun byProduct(productId: ProductId): GooglePurchase? {
        return invoke().find { purchase ->
            purchase.productIds.contains(productId)
        }
    }

    internal companion object {
        private val ALLOWED_BILLING_ERRORS = arrayOf(
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
        )
    }
}

private fun Purchase.isPurchasedButNotAcknowledged(): Boolean =
    purchaseState == Purchase.PurchaseState.PURCHASED && isAcknowledged.not()
