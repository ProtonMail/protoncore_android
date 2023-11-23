/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.domain.repository

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.repository.PlanQuantity
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage
interface PlansRepository {
    /**
     * Returns a list of dynamic plans that are available for the user.
     */
    suspend fun getDynamicPlans(sessionUserId: SessionUserId?, appStore: AppStore): DynamicPlans

    /**
     * Returns from the API all plans available for the user in the moment.
     */
    suspend fun getPlans(
        sessionUserId: SessionUserId?
    ): List<Plan>

    /**
     * Returns from the API all plans available for the user in the moment.
     */
    suspend fun getPlansDefault(
        sessionUserId: SessionUserId?
    ): Plan

    // region subscription
    /**
     * Unauthenticated.
     * It checks given a particular plans and cycles how much a user should pay.
     * It also takes into an account any special coupon or gift codes.
     * Should be called upon a user selected any plan, duration and entered a code.
     */
    public suspend fun validateSubscription(
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
    public suspend fun getSubscription(sessionUserId: SessionUserId): Subscription?

    /**
     * Authenticated.
     * Returns current active dynamic subscriptions.
     */
    public suspend fun getDynamicSubscriptions(sessionUserId: SessionUserId): List<DynamicSubscription>

    /**
     * Authenticated.
     * Creates new or updates current subscription. Not for usage during sign up.
     * Used only for upgrade after sign up.
     */
    public suspend fun createOrUpdateSubscription(
        sessionUserId: SessionUserId,
        amount: Long,
        currency: Currency,
        payment: PaymentTokenEntity?,
        codes: List<String>? = null,
        plans: PlanQuantity,
        cycle: SubscriptionCycle,
        subscriptionManagement: SubscriptionManagement
    ): Subscription
    // endregion
}
