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

package me.proton.core.plan.data.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.payment.domain.MAX_PLAN_QUANTITY
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.plan.domain.usecase.PerformSubscribe
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.isNullOrCredentialLess
import java.util.Optional
import javax.inject.Inject

public class PerformSubscribeImpl @Inject constructor(
    private val acknowledgeGooglePlayPurchase: Optional<AcknowledgeGooglePlayPurchase>,
    private val plansRepository: PlansRepository,
    private val humanVerificationManager: HumanVerificationManager,
    private val clientIdProvider: ClientIdProvider,
    private val userManager: UserManager,
) : PerformSubscribe {
    /**
     * @param codes optional an array of [String] coupon or gift codes used for discounts.
     * @param paymentToken optional??? payment token.
     */
    public override suspend operator fun invoke(
        userId: UserId,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        planNames: List<String>,
        codes: List<String>?,
        paymentToken: ProtonPaymentToken?,
        subscriptionManagement: SubscriptionManagement
    ): Subscription {
        require(amount >= 0)
        require(planNames.isNotEmpty())
        require(paymentToken != null || amount <= 0) {
            "Payment Token must be supplied when the amount is bigger than zero. Otherwise it should be null."
        }
        require(!userId.isNullOrCredentialLess(userManager)) {
            "Cannot subscribe with a credential-less user."
        }
        val subscription = plansRepository.createOrUpdateSubscription(
            sessionUserId = userId,
            amount = amount,
            currency = currency,
            payment = if (amount == 0L) null else PaymentTokenEntity(paymentToken!!),
            codes = codes,
            plans = planNames.associateWith { MAX_PLAN_QUANTITY },
            cycle = cycle,
            subscriptionManagement = subscriptionManagement
        )

        if (paymentToken != null) {
            // Clear any previous payment token (unauthenticated session cookie HV details).
            // HV payment token is previously added by BillingCommonViewModel.
            val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
            humanVerificationManager.clearDetails(clientId)

            if (subscriptionManagement == SubscriptionManagement.GOOGLE_MANAGED &&
                acknowledgeGooglePlayPurchase.isPresent
            ) {
                acknowledgeGooglePlayPurchase.get().invoke(paymentToken)
            }
        }

        return subscription
    }
}
