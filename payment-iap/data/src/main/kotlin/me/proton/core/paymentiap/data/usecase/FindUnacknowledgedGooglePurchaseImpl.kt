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

package me.proton.core.paymentiap.data.usecase

import com.android.billingclient.api.Purchase
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import javax.inject.Inject
import javax.inject.Provider

public class FindUnacknowledgedGooglePurchaseImpl @Inject constructor(
    private val billingRepositoryProvider: Provider<GoogleBillingRepository>,
) : FindUnacknowledgedGooglePurchase {
    public override suspend operator fun invoke(): List<GooglePurchase> {
        return billingRepositoryProvider.get().use { repository ->
            repository.querySubscriptionPurchases()
                .filter { it.isPurchasedButNotAcknowledged() }
                .sortedByDescending { it.purchaseTime }
                .map { it.wrap() }
        }
    }

    override suspend fun byCustomer(customerId: String): GooglePurchase? {
        return invoke().find { purchase ->
            purchase.customerId == customerId
        }
    }

    override suspend fun byProduct(productId: String): GooglePurchase? {
        return invoke().find { purchase ->
            purchase.productIds.contains(productId)
        }
    }
}

private fun Purchase.isPurchasedButNotAcknowledged(): Boolean =
    purchaseState == Purchase.PurchaseState.PURCHASED && isAcknowledged.not()
