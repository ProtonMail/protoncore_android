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

import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.runWithObservability
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

/**
 * Returns the current available payment methods that the user has already saved in the API.
 * Only upgrade for existing logged in user.
 * Could not be used during sign-up.
 */
public class GetAvailablePaymentMethods @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val observabilityManager: ObservabilityManager
) {
    public suspend operator fun invoke(
        userId: UserId,
        metricData: ((Result<List<PaymentMethod>>) -> ObservabilityData)? = null
    ): List<PaymentMethod> =
        paymentsRepository.runWithObservability(observabilityManager, metricData) {
            getAvailablePaymentMethods(userId)
        }
}
