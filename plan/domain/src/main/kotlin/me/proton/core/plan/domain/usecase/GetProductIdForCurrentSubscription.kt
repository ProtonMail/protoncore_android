/*
 * Copyright (c) 2024 Proton AG
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
import me.proton.core.plan.domain.repository.PlansRepository
import javax.inject.Inject

class GetProductIdForCurrentSubscription @Inject constructor(
    private val appStore: AppStore,
    private val plansRepository: PlansRepository,
) {
    suspend operator fun invoke(userId: UserId): ProductId? {
        val dynamicSubscription = plansRepository.getDynamicSubscriptions(userId).first()

        // backwards compatibility: we pass null for userId in the getDynamicPlans because API will omit the paid plans for a paid user
        val plans = plansRepository.getDynamicPlans(null, appStore).plans
        val plan = plans.firstOrNull { it.name == dynamicSubscription.name }
        val instance = plan?.instances?.get(dynamicSubscription.cycleMonths)
        return instance?.vendors?.get(AppStore.GooglePlay)?.productId?.let {
            ProductId(it)
        }
    }
}
