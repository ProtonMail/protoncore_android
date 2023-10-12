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

package me.proton.core.plan.presentation.usecase

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.LogTag
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.usecase.GetPlans
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchaseStatus
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchase
import me.proton.core.util.kotlin.CoreLogger
import java.util.Optional
import javax.inject.Inject

/** Checks if there is an unredeemed Google purchase for a logged in user. */
internal class CheckUnredeemedGooglePurchase @Inject constructor(
    private val findUnacknowledgedGooglePurchaseOptional: Optional<FindUnacknowledgedGooglePurchase>,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getCurrentSubscription: GetCurrentSubscription,
    private val getPlans: GetPlans
) {
    /** Returns the latest unredeemed Google purchase for a given [user][userId].
     * May return `null` if it was not possible to fetch some data (network error).
     */
    suspend operator fun invoke(userId: UserId): UnredeemedGooglePurchase? {
        return try {
            perform(userId)
        } catch (_: Throwable) {
            null
        }
    }

    @Suppress("ReturnCount")
    private suspend fun perform(userId: UserId): UnredeemedGooglePurchase? {
        val findUnacknowledgedGooglePurchase = findUnacknowledgedGooglePurchaseOptional.getOrNull() ?: return null
        if (PaymentProvider.GoogleInAppPurchase !in getAvailablePaymentProviders()) return null

        val subscription = getCurrentSubscription(userId)
        val subscriptionCustomerId = subscription?.customerId
        val googlePurchases = if (subscriptionCustomerId != null) {
            listOfNotNull(findUnacknowledgedGooglePurchase.byCustomer(subscriptionCustomerId))
        } else {
            findUnacknowledgedGooglePurchase()
        }
        val googlePurchase = googlePurchases.firstOrNull() ?: return null
        val purchasedPlan = googlePurchase.findCorrespondingPlan(userId) ?: return null

        val status = if (subscription == null) {
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

    private suspend fun GooglePurchase.findCorrespondingPlan(userId: UserId): Plan? {
        return getPlans(userId).find { plan ->
            productIds.all { id ->
                plan.vendors[AppStore.GooglePlay]?.names?.values?.contains(id) == true
            }
        }
    }

    private fun Subscription.matchesGooglePurchase(googlePurchase: GooglePurchase, purchasedPlan: Plan): Boolean {
        return external == SubscriptionManagement.GOOGLE_MANAGED &&
            customerId == googlePurchase.customerId &&
            purchasedPlan.name in plans.map { it.name }
    }

    private fun <T : Any> Optional<T>.getOrNull(): T? = if (isPresent) get() else null
}
