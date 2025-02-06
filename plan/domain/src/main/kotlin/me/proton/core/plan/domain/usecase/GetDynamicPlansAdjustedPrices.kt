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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.LogTag
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanInstance
import me.proton.core.plan.domain.entity.DynamicPlanPrice
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.util.kotlin.CoreLogger
import java.util.Optional
import javax.inject.Inject

class GetDynamicPlansAdjustedPrices @Inject constructor(
    private val plansRepository: PlansRepository,
    private val appStore: AppStore,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getStorePrice: Optional<GetStorePrice>
) {
    suspend operator fun invoke(userId: UserId?): DynamicPlans {
        val dynamicPlans = plansRepository.getDynamicPlans(userId, appStore)
        if (!getStorePrice.isPresent) return dynamicPlans
        if (!getAvailablePaymentProviders(userId).contains(PaymentProvider.GoogleInAppPurchase)) return dynamicPlans

        return dynamicPlans.copy(plans = dynamicPlans.plans.mapNotNull { plan ->
            getPlanWithGooglePrices(plan)
        })
    }

    private suspend fun getPlanWithGooglePrices(plan: DynamicPlan): DynamicPlan? {
        if (plan.instances.isEmpty()) return plan // Nothing to adjust (free plan).

        if (plan.instances.values.any { it.price.isEmpty() }) {
            CoreLogger.e(LogTag.PRICE_ERROR, "Plan ${plan.name} prices empty error.")
        }

        val instances = plan.instances.values.mapNotNull { instance ->
            getPlanInstanceWithGooglePrices(instance)
        }

        return plan
            .takeIf { instances.isNotEmpty() }
            ?.copy(instances = instances.associateBy { it.cycle })
    }

    /**
     * @return A plan instance with prices from Google Play, or `null` if the Google Play prices could not be fetched.
     */
    private suspend fun getPlanInstanceWithGooglePrices(instance: DynamicPlanInstance): DynamicPlanInstance? {
        val productId = instance.vendors[AppStore.GooglePlay]?.productId
        val prices = productId?.let { getStorePrice.get().invoke(ProductId(it)) }
        return when (prices) {
            null -> null
            else -> instance.copy(
                price = mapOf(
                    prices.currency to DynamicPlanPrice(
                        id = productId,
                        currency = prices.currency,
                        current = prices.priceAmountCents,
                        default = prices.defaultPriceAmountCents,
                    )
                )
            )
        }
    }
}
