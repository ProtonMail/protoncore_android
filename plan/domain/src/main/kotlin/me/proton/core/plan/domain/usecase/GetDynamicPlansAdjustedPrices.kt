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

        return dynamicPlans.copy(plans = dynamicPlans.plans.map { plan ->
            if (plan.instances.values.any { it.price.isEmpty() }) {
                CoreLogger.e(LogTag.PRICE_ERROR, "Plan ${plan.name} prices empty error.")
            }
            val instances = plan.instances.values.map { instance ->
                val productId = instance.vendors[AppStore.GooglePlay]?.productId
                when (val prices = productId?.let { getStorePrice.get().invoke(ProductId(it)) }) {
                    null -> instance
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
            plan.copy(instances = instances.associateBy { it.cycle })
        })
    }
}
