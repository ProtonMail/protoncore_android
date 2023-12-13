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

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.plan.domain.IsDynamicPlanAdjustedPriceEnabled
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
    private val appStore: AppStore,
    private val getStorePrice: Optional<GetStorePrice>,
    private val isDynamicPlanAdjustedPriceEnabled: IsDynamicPlanAdjustedPriceEnabled
) {
    suspend operator fun invoke(userId: UserId): DynamicSubscription {
        val dynamicSubscription = plansRepository.getDynamicSubscriptions(userId).first()
        if (dynamicSubscription.external == SubscriptionManagement.PROTON_MANAGED) return dynamicSubscription
        if (dynamicSubscription.cycleMonths == null) return dynamicSubscription
        if (!isDynamicPlanAdjustedPriceEnabled(userId)) return dynamicSubscription
        if (!getStorePrice.isPresent) return dynamicSubscription

        // we pass null for userId in the getDynamicPlans because API will omit the paid plans for a paid user
        val plans = plansRepository.getDynamicPlans(null, appStore).plans
        val plan = plans.firstOrNull { it.name == dynamicSubscription.name }
            ?: return dynamicSubscription.copy(amount = null, currency = null)

        val instance = plan.instances[dynamicSubscription.cycleMonths]
        val productId = instance?.vendors?.get(AppStore.GooglePlay)?.productId
            ?: return dynamicSubscription.copy(amount = null, currency = null)

        val storePrice = getStorePrice.get().invoke(ProductId(productId))
            ?: return dynamicSubscription.copy(amount = null, currency = null)

        return dynamicSubscription.copy(
            amount = storePrice.priceAmountCents.toLong(),
            currency = storePrice.currency,
        )
    }
}
