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

package me.proton.core.payment.domain.repository

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken

public typealias PlanQuantity = Map<String, Int> // the plan name along with the quantity number

public interface PaymentsRepository {

    /**
     * Creates a new payment token which will be used later for a new subscription.
     * Before that there can be a token validation step.
     */
    public suspend fun createPaymentToken(
        sessionUserId: SessionUserId? = null,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType
    ): PaymentTokenResult.CreatePaymentTokenResult

    /**
     * Unauthenticated.
     * Checks the status of the payment token. Certain actions could be only executed for particular statuses. This is
     * why knowing the status is important.
     */
    public suspend fun getPaymentTokenStatus(
        sessionUserId: SessionUserId?,
        paymentToken: ProtonPaymentToken
    ): PaymentTokenResult.PaymentTokenStatusResult
    // endregion

    // region payment methods
    /**
     * Authenticated.
     * Returns the already saved payment methods for a user.
     * Can only be used for already logged in users and not during signup.
     */
    public suspend fun getAvailablePaymentMethods(sessionUserId: SessionUserId): List<PaymentMethod>
    // endregion

    /**
     * These are the global values for all platforms indicating what payment methods are being supported.
     */
    public suspend fun getPaymentStatus(sessionUserId: SessionUserId?, appStore: AppStore): PaymentStatus
}
