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
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.plan.domain.usecase.PerformSubscribe
import java.util.Optional
import javax.inject.Inject

class PerformSubscribeImpl @Inject constructor(
    private val acknowledgeGooglePlayPurchase: Optional<AcknowledgeGooglePlayPurchase>,
    private val plansRepository: PlansRepository,
    private val humanVerificationManager: HumanVerificationManager,
    private val clientIdProvider: ClientIdProvider
) : PerformSubscribe {

    override suspend operator fun invoke(
        cycle: SubscriptionCycle,
        paymentToken: ProtonPaymentToken?,
        planNames: List<String>,
        userId: UserId
    ): Subscription {
        requireNotNull(paymentToken)

        val subscription = plansRepository.createOrUpdateSubscription(
            sessionUserId = userId,
            payment = PaymentTokenEntity(paymentToken),
            plans = planNames.associateWith { MAX_PLAN_QUANTITY },
            cycle = cycle
        )

        val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
        humanVerificationManager.clearDetails(clientId)

        if (acknowledgeGooglePlayPurchase.isPresent) {
            acknowledgeGooglePlayPurchase.get().invoke(paymentToken)
        }

        return subscription
    }
}
