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

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentBody
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.entity.PaymentToken
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.Subscription
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus

typealias PlanQuantity = Map<String, Int> // the plan name along with the quantity number

interface PaymentsRepository {

    // region payment tokens
    /**
     * Unauthenticated.
     * Creates a new payment token which will be used later for a new subscription.
     * Before that there can be a token validation step.
     */
    suspend fun createPaymentTokenNewPayPal(
        sessionUserId: SessionUserId? = null,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType.PayPal
    ): PaymentToken.CreatePaymentTokenResult

    /**
     * Unauthenticated.
     * Creates a new payment token which will be used later for a new subscription.
     * Before that there can be a token validation step.
     */
    suspend fun createPaymentTokenNewCreditCard(
        sessionUserId: SessionUserId? = null,
        amount: Long,
        currency: Currency,
        paymentType: PaymentType.CreditCard
    ): PaymentToken.CreatePaymentTokenResult

    /**
     * Unauthenticated.
     * Creates a new payment token which will be used later for a new subscription.
     * Before that there can be a token validation step.
     */
    suspend fun createPaymentTokenExistingPaymentMethod(
        sessionUserId: SessionUserId? = null,
        amount: Long,
        currency: Currency,
        paymentMethodId: String
    ): PaymentToken.CreatePaymentTokenResult

    /**
     * Unauthenticated.
     * Checks the status of the payment token. Certain actions could be only executed for particular statuses. This is
     * why knowing the status is important.
     */
    suspend fun getPaymentTokenStatus(
        sessionUserId: SessionUserId?,
        paymentToken: String
    ): PaymentToken.PaymentTokenStatusResult
    // endregion

    // region payment methods
    /**
     * Authenticated.
     * Returns the already saved payment methods for a user.
     * Can only be used for already logged in users and not during signup.
     */
    suspend fun getAvailablePaymentMethods(sessionUserId: SessionUserId): List<PaymentMethod>
    // endregion

    // region subscription
    /**
     * Unauthenticated.
     * It checks given a particular plans and cycles how much a user should pay.
     * It also takes into an account any special coupon or gift codes.
     * Should be called upon a user selected any plan, duration and entered a code.
     */
    suspend fun validateSubscription(
        sessionUserId: SessionUserId?,
        codes: List<String>? = null,
        plans: PlanQuantity,
        currency: Currency,
        cycle: SubscriptionCycle
    ): SubscriptionStatus

    /**
     * Authenticated.
     * Returns current active subscription.
     */
    suspend fun getSubscription(sessionUserId: SessionUserId): Subscription?

    /**
     * Authenticated.
     * Creates new or updates current subscription. Not for usage during sign up.
     * Used only for upgrade after sign up.
     */
    suspend fun createOrUpdateSubscription(
        sessionUserId: SessionUserId,
        amount: Long,
        currency: Currency,
        payment: PaymentBody?,
        codes: List<String>? = null,
        plans: PlanQuantity,
        cycle: SubscriptionCycle
    ): Subscription
    // endregion

    /**
     * These are the global values for all platforms indicating what payment methods are being supported.
     */
    suspend fun getPaymentStatus(sessionUserId: SessionUserId?): PaymentStatus
}
