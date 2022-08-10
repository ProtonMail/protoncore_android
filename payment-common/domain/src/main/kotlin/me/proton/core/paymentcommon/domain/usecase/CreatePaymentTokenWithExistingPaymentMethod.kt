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

package me.proton.core.paymentcommon.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.paymentcommon.domain.entity.Currency
import me.proton.core.paymentcommon.domain.entity.PaymentToken
import me.proton.core.paymentcommon.domain.repository.PaymentsRepository
import javax.inject.Inject

/**
 * Creates new payment payment token.
 * Only for existing payments method.
 * For payment tokens with new payment methods @see [CreatePaymentTokenWithNewCreditCard].
 */
public class CreatePaymentTokenWithExistingPaymentMethod @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {
    public suspend operator fun invoke(
        userId: UserId?,
        amount: Long,
        currency: Currency,
        paymentMethodId: String
    ): PaymentToken.CreatePaymentTokenResult {
        require(amount >= 0)
        return paymentsRepository.createPaymentTokenExistingPaymentMethod(
            sessionUserId = userId,
            amount = amount,
            currency = currency,
            paymentMethodId = paymentMethodId
        )
    }
}
