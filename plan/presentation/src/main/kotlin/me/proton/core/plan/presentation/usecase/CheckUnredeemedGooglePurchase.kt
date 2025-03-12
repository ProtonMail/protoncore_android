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

package me.proton.core.plan.presentation.usecase

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.usecase.GetDynamicPlans
import me.proton.core.plan.domain.usecase.GetDynamicSubscription
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchaseStatus
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.isCredentialLess
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

/** Checks if there is an unredeemed Google purchase for a logged in user. */
internal class CheckUnredeemedGooglePurchase @Inject constructor(
    private val userManager: UserManager,
    private val findUnacknowledgedGooglePurchase: Optional<FindUnacknowledgedGooglePurchase>,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getCurrentSubscription: GetDynamicSubscription,
    private val getPlans: GetDynamicPlans
) {
    /** Returns the latest unredeemed Google purchase for a given [user][userId].
     * May return `null` if it was not possible to fetch some data (network error).
     */
    suspend operator fun invoke(userId: UserId): UnredeemedGooglePurchase? {
        return try {
            val user = userManager.getUser(userId)
            when {
                // Don't raise any unredeemed flow for credential-less user.
                user.isCredentialLess() -> null
                else -> perform(userId)
            }
        } catch (_: Throwable) {
            null
        }
    }

    @Suppress("ReturnCount")
    private suspend fun perform(userId: UserId): UnredeemedGooglePurchase? {
        val findUnacknowledgedGooglePurchase = findUnacknowledgedGooglePurchase.getOrNull() ?: return null
        if (PaymentProvider.GoogleInAppPurchase !in getAvailablePaymentProviders()) return null

        val subscription = getCurrentSubscription(userId) ?: return null
        val subscriptionCustomerId = subscription.customerId
        val googlePurchases = if (subscriptionCustomerId != null) {
            listOfNotNull(findUnacknowledgedGooglePurchase.byCustomer(subscriptionCustomerId))
        } else {
            findUnacknowledgedGooglePurchase()
        }
        val googlePurchase = googlePurchases.firstOrNull() ?: return null
        val purchasedPlan = googlePurchase.findCorrespondingPlan() ?: return null

        val status = if (subscription.name == null) {
            UnredeemedGooglePurchaseStatus.NotSubscribed
        } else if (subscription.matchesGooglePurchase(googlePurchase, purchasedPlan)) {
            UnredeemedGooglePurchaseStatus.SubscribedButNotAcknowledged
        } else {
            // Current subscription is not matching the googlePurchase's customerId,
            // or the subscription is not managed by Google.
            null
        }

        return status?.let { UnredeemedGooglePurchase(googlePurchase, purchasedPlan, it) }
    }

    private suspend fun GooglePurchase.findCorrespondingPlan(): DynamicPlan? {
        val googleProductIds = productIds.map { it.id }
        return getPlans(null).plans.find { plan ->
            plan.instances.values.find { it.vendors[AppStore.GooglePlay]?.productId in googleProductIds } != null
        }
    }

    private fun DynamicSubscription.matchesGooglePurchase(
        googlePurchase: GooglePurchase,
        purchasedPlan: DynamicPlan
    ): Boolean {
        val cycle = cycleMonths ?: return false
        val googleProductIds = googlePurchase.productIds.map { it.id }
        val planInstance = purchasedPlan.instances[cycle]
        return external == SubscriptionManagement.GOOGLE_MANAGED &&
                customerId == googlePurchase.customerId &&
                purchasedPlan.name == name &&
                planInstance?.vendors?.get(AppStore.GooglePlay)?.productId in googleProductIds
    }
}
