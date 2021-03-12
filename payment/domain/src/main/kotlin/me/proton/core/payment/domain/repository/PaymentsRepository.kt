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

package me.proton.core.payment.domain.repository

import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentBody
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus

interface PaymentsRepository {

    // region payment tokens
    /**
     * Unauthenticated.
     * Creates a new payment token which will be used later for a new subscription.
     * Before that there can be a token validation step.
     */
    suspend fun createPaymentToken(
        sessionId: SessionId? = null,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType? = null,
        paymentMethodId: String? = null
    ): PaymentToken.CreatePaymentTokenResult

    /**
     * Unauthenticated.
     * Checks the status of the payment token. Certain actions could be only executed for particular statuses. This is
     * why knowing the status is important.
     */
    suspend fun getPaymentTokenStatus(
        sessionId: SessionId?,
        paymentToken: String
    ): PaymentToken.PaymentTokenStatusResult
    // endregion

    // region payment methods
    /**
     * Authenticated.
     * Returns the already saved payment methods for a user.
     * Can only be used for already logged in users and not during signup.
     */
    suspend fun getAvailablePaymentMethods(sessionId: SessionId): List<PaymentMethod>
    // endregion

    // region subscription
    /**
     * Unauthenticated.
     * It checks given a particular plans and cycles how much a user should pay.
     * It also takes into an account any special coupon or gift codes.
     * Should be called upon a user selected any plan, duration and entered a code.
     */
    suspend fun validateSubscription(
        sessionId: SessionId?,
        codes: List<String>? = null,
        planIds: List<String>,
        currency: Currency,
        cycle: SubscriptionCycle
    ): SubscriptionStatus

    /**
     * Authenticated.
     * Returns current active subscription.
     */
    suspend fun getSubscription(sessionId: SessionId): Subscription

    /**
     * Authenticated.
     * Creates new or updates current subscription. Not for usage during sign up.
     * Used only for upgrade after sign up.
     */
    suspend fun createOrUpdateSubscription(
        sessionId: SessionId,
        amount: Long,
        currency: Currency,
        payment: PaymentBody?,
        codes: List<String>? = null,
        planIds: Map<String, Int>,
        cycle: SubscriptionCycle
    ): Subscription
    // endregion
}
