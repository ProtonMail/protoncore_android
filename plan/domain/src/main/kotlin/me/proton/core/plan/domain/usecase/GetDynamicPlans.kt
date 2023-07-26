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

import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.ProductOnlyPaidPlans
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.hasServiceFor
import me.proton.core.plan.domain.entity.isFree
import me.proton.core.plan.domain.repository.PlansRepository
import javax.inject.Inject

class GetDynamicPlans @Inject constructor(
    private val plansRepository: PlansRepository,
    private val product: Product,
    @ProductOnlyPaidPlans val productExclusivePlans: Boolean
) {
    suspend operator fun invoke(userId: UserId?): List<DynamicPlan> = plansRepository
        .getDynamicPlans(userId)
        .filter { it.hasServiceFor(product, exclusive = productExclusivePlans) || it.isFree() }
        .sortedBy { plan -> plan.order }
}
