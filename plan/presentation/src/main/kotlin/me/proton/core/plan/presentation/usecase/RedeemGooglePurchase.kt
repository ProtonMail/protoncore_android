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
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.payment.domain.usecase.PollPaymentTokenStatus
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.payment.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.PerformSubscribe
import me.proton.core.plan.presentation.entity.UnredeemedGooglePurchaseStatus
import java.util.Optional
import javax.inject.Inject

internal class RedeemGooglePurchase @Inject constructor(
    private val acknowledgeGooglePlayPurchaseOptional: Optional<AcknowledgeGooglePlayPurchase>,
    private val createPaymentToken: CreatePaymentTokenForGooglePurchase,
    private val isOmnichannelEnabled: IsOmnichannelEnabled,
    private val performSubscribe: PerformSubscribe,
    private val pollPaymentTokenStatus: PollPaymentTokenStatus,
) {
    suspend operator fun invoke(
        googlePurchase: GooglePurchase,
        purchasedPlan: DynamicPlan,
        purchaseStatus: UnredeemedGooglePurchaseStatus,
        userId: UserId
    ) {
        when (purchaseStatus) {
            UnredeemedGooglePurchaseStatus.NotSubscribed -> {
                createSubscriptionAndAcknowledge(googlePurchase, purchasedPlan, userId)
            }

            UnredeemedGooglePurchaseStatus.SubscribedButNotAcknowledged -> {
                if (acknowledgeGooglePlayPurchaseOptional.isPresent) {
                    acknowledgeGooglePlayPurchaseOptional.get().invoke(googlePurchase.purchaseToken)
                }
            }
        }
    }

    /**
     * Note: this routine incorporates the [isOmnichannelEnabled] feature flag. When conducting
     * a purchase under an omnichannel flow, we must check the approval status of the tokenized
     * purchase. Under "legacy" payment flows, this polling is not necessary nor possible. When
     * adoption is complete, this feature flag conditional wrapping will be removed.
     */
    private suspend fun createSubscriptionAndAcknowledge(
        googlePurchase: GooglePurchase,
        purchasedPlan: DynamicPlan,
        userId: UserId
    ) {
        runCatching {
            val productId = googlePurchase.productIds.first().id
            val planInstance = requireNotNull(
                purchasedPlan.instances.values.firstOrNull { instance ->
                    instance.vendors[AppStore.GooglePlay]?.productId == productId
                }
            )
            val planName = requireNotNull(purchasedPlan.name)

            val tokenResponse = createPaymentToken(
                googleProductId = googlePurchase.productIds.first(),
                purchase = googlePurchase,
                userId = userId
            )

            if (isOmnichannelEnabled(userId)) {
                pollPaymentTokenStatus(userId, tokenResponse.token)
            }

            performSubscribe(
                cycle = SubscriptionCycle.map[planInstance.cycle] ?: SubscriptionCycle.OTHER,
                planNames = listOf(planName),
                paymentToken = tokenResponse.token,
                userId = userId
            )
        }
    }
}
