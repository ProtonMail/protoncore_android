/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.ClientPlanFilter
import me.proton.core.plan.domain.ProductOnlyPaidPlans
import me.proton.core.plan.domain.entity.MASK_CALENDAR
import me.proton.core.plan.domain.entity.MASK_DRIVE
import me.proton.core.plan.domain.entity.MASK_MAIL
import me.proton.core.plan.domain.entity.MASK_PASS
import me.proton.core.plan.domain.entity.MASK_VPN
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.plan.domain.repository.PlansRepository
import me.proton.core.util.kotlin.exhaustive
import me.proton.core.util.kotlin.hasFlag
import me.proton.core.util.kotlin.matchesMask
import javax.inject.Inject

class GetPlans @Inject constructor(
    private val plansRepository: PlansRepository,
    private val product: Product,
    @ProductOnlyPaidPlans val productExclusivePlans: Boolean,
    private val clientPlanFilter: ClientPlanFilter? = null
) {
    suspend operator fun invoke(userId: UserId?): List<Plan> {
        return plansRepository.getPlans(userId)
            .filter { it.enabled }
            .filter {
                when (product) {
                    Product.Calendar -> it.hasServiceFor(MASK_CALENDAR)
                    Product.Drive -> it.hasServiceFor(MASK_DRIVE)
                    Product.Mail -> it.hasServiceFor(MASK_MAIL)
                    Product.Vpn -> it.hasServiceFor(MASK_VPN)
                    Product.Pass -> it.hasServiceFor(MASK_PASS)
                }.exhaustive
            }.filter(clientPlanFilter?.filter() ?: { true })
            .sortedByDescending { it.services }
    }

    private fun Plan.hasServiceFor(mask: Int): Boolean =
        if (productExclusivePlans) (services ?: 0).matchesMask(mask)
        else
            (services ?: 0).hasFlag(mask)
}
