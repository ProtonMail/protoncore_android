/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.payment.domain.usecase

import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

/**
 * Use case that validates a subscription before it is made for a given plans.
 * Will return the amount than needs to be charged along with other details.
 * Can be used for upgrade and for signups as well.
 */
class ValidateSubscriptionPlan @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {
    suspend operator fun invoke(
        sessionId: SessionId?,
        codes: List<String>? = null,
        planIds: List<String>,
        currency: Currency,
        cycle: SubscriptionCycle
    ): SubscriptionStatus {
        require(planIds.isNotEmpty())
        return paymentsRepository.validateSubscription(sessionId, codes, planIds, currency, cycle)
    }
}
