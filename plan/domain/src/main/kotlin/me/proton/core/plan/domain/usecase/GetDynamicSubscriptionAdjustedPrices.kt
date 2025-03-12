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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.domain.repository.PlansRepository
import java.util.Optional
import javax.inject.Inject

/**
 * Gets current active dynamic subscription a user has.
 * Authorized. This means that it could only be used for upgrades. New accounts created during sign ups logically do not
 * have existing subscriptions.
 */
@Suppress("ReturnCount")
class GetDynamicSubscriptionAdjustedPrices @Inject constructor(
    private val plansRepository: PlansRepository,
    private val getStorePrice: Optional<GetStorePrice>,
    private val getProductIdForCurrentSubscription: GetProductIdForCurrentSubscription
) {
    suspend operator fun invoke(userId: UserId): DynamicSubscription {
        val dynamicSubscription = plansRepository.getDynamicSubscriptions(userId).first()
        if (dynamicSubscription.external == SubscriptionManagement.PROTON_MANAGED) return dynamicSubscription
        if (dynamicSubscription.cycleMonths == null) return dynamicSubscription
        if (!getStorePrice.isPresent) return dynamicSubscription

        val productId = getProductIdForCurrentSubscription(userId)
            ?: return dynamicSubscription.copy(amount = null, currency = null)

        val storePrice = getStorePrice.get().invoke(productId)
            ?: return dynamicSubscription.copy(amount = null, currency = null)

        return dynamicSubscription.copy(
            amount = storePrice.priceAmountCents.toLong(),
            renewAmount = storePrice.defaultPriceAmountCents?.toLong(),
            currency = storePrice.currency
        )
    }
}
