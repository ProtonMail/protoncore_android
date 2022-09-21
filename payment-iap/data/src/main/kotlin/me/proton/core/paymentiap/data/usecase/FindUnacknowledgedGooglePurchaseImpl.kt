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
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.paymentiap.domain.entity.wrap
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import javax.inject.Inject
import javax.inject.Provider

public class FindUnacknowledgedGooglePurchaseImpl @Inject constructor(
    private val billingRepositoryProvider: Provider<GoogleBillingRepository>,
) : FindUnacknowledgedGooglePurchase {
    /**
     * Checks if there is an unredeemed Google purchase.
     * To redeem the purchase, the caller will need to:
     * - create a corresponding payment token (if not yet created),
     * - assign the subscription (if not already),
     * - acknowledge the Google purchase (usually done as part of assigning the subscription).
     *
     * @param productId The Google product ID. If present, a potential unredeemed purchase will have to match it.
     * @param userId If a potential unredeemed purchase has been marked
     *  with a non-null [accountId][com.android.billingclient.api.AccountIdentifiers.getObfuscatedAccountId],
     *  then the given [userId] will have to match it.
     *
     *  @see me.proton.core.payment.domain.usecase.CreatePaymentTokenWithGoogleIAP
     *  @see me.proton.core.payment.domain.usecase.PerformSubscribe
     *  @see me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
     */
    public override suspend operator fun invoke(productId: String?, userId: UserId?): GooglePurchase? {
        return billingRepositoryProvider.get().use { repository ->
            repository.querySubscriptionPurchases().find { purchase ->
                purchase.isPurchasedButNotAcknowledged() &&
                    purchase.isMatchingUser(userId) &&
                    purchase.containsProduct(productId)
            }?.wrap()
        }
    }
}

private fun Purchase.containsProduct(productId: String?): Boolean {
    return if (productId == null) {
        true
    } else {
        products.contains(productId)
    }
}

private fun Purchase.isMatchingUser(userId: UserId?): Boolean {
    val accountId = accountIdentifiers?.obfuscatedAccountId
    return if (accountId.isNullOrEmpty()) {
        // If the accountId stored inside a Google purchase is null,
        // we can redeem against any user.
        true
    } else {
        // If the accountId stored inside a Google purchase is not null,
        // we should only redeem for the same user.
        accountId == userId?.id
    }
}

private fun Purchase.isPurchasedButNotAcknowledged(): Boolean =
    purchaseState == Purchase.PurchaseState.PURCHASED && isAcknowledged.not()
