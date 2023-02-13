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

package me.proton.core.payment.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.runWithObservability
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

/**
 * Creates new payment token.
 * Only for new PayPal payments methods provided with [PaymentType.PayPal].
 * For payment tokens with new Credit Card payment method @see [CreatePaymentTokenWithNewCreditCard].
 * For payment tokens with existing payment method @see [CreatePaymentTokenWithExistingPaymentMethod].
 */
public class CreatePaymentTokenWithNewPayPal @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val observabilityManager: ObservabilityManager
) {
    public suspend operator fun invoke(
        userId: UserId?,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType.PayPal,
        metricData: ((Result<PaymentTokenResult.CreatePaymentTokenResult>) -> ObservabilityData)? = null
    ): PaymentTokenResult.CreatePaymentTokenResult {
        require(amount >= 0)

        return paymentsRepository.runWithObservability(observabilityManager, metricData) {
            createPaymentTokenNewPayPal(
                userId,
                amount,
                currency,
                paymentType
            )
        }
    }
}
