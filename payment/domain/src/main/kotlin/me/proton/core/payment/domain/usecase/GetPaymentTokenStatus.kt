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
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.repository.PaymentsRepository

import javax.inject.Inject

/**
 * Checks the payment token status.
 * A payment token in it's lifecycle can go through various states and the client should use this use case
 * (usually with polling) in order to be able to act on a status change.
 */
public class GetPaymentTokenStatus @Inject constructor(
    private val paymentsRepository: PaymentsRepository
) {
    public suspend operator fun invoke(userId: UserId?, paymentToken: String): PaymentToken.PaymentTokenStatusResult {
        require(paymentToken.isNotBlank())
        return paymentsRepository.getPaymentTokenStatus(userId, paymentToken)
    }
}
