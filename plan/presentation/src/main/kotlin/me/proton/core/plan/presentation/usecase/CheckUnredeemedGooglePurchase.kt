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

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetCurrentSubscription
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.usecase.GetPlans
import java.util.Optional
import javax.inject.Inject

/** Checks if there is an unredeemed Google purchase for a logged in user. */
internal class CheckUnredeemedGooglePurchase @Inject constructor(
    private val findUnacknowledgedGooglePurchase: Optional<FindUnacknowledgedGooglePurchase>,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getCurrentSubscription: GetCurrentSubscription,
    private val getPlans: GetPlans
) {
    @Suppress("ReturnCount")
    suspend operator fun invoke(userId: UserId): Pair<GooglePurchase, Plan>? {
        if (!findUnacknowledgedGooglePurchase.isPresent) return null
        if (PaymentProvider.GoogleInAppPurchase !in getAvailablePaymentProviders()) return null

        // TODO For now, we don't support redeeming, if a user is already on a paid plan (CP-4583).
        if (getCurrentSubscription(userId) != null) return null

        return findUnacknowledgedGooglePurchase.get().invoke(productId = null, userId)?.let { googlePurchase ->
            googlePurchase.findCorrespondingPlan(userId)?.let { plan -> googlePurchase to plan }
        }
    }

    private suspend fun GooglePurchase.findCorrespondingPlan(userId: UserId): Plan? {
        return getPlans(userId).find { plan ->
            productIds.all { id ->
                plan.vendorNames.find { it.name == id } != null
            }
        }
    }
}
