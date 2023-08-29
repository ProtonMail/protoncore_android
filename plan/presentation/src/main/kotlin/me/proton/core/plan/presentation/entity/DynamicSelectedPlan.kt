/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.plan.presentation.entity

import android.content.res.Resources
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.isFree
import me.proton.core.plan.presentation.viewmodel.toPlanVendorDetailsMap

internal fun DynamicPlan.getSelectedPlan(
    resources: Resources,
    cycle: Int,
    currency: String?
): SelectedPlan = when (val name = name) {
    null -> SelectedPlan.free(resources)
    else -> SelectedPlan(
        planName = name,
        planDisplayName = title,
        free = isFree(),
        cycle = PlanCycle.map[cycle] ?: PlanCycle.FREE,
        currency = PlanCurrency.mapName[currency] ?: PlanCurrency.CHF,
        amount = instances[cycle]?.price?.get(currency)?.current?.toDouble() ?: 0.0,
        services = services.sumOf { it.code },
        type = type?.value ?: 0,
        vendorNames = instances[cycle]?.vendors?.toPlanVendorDetailsMap(cycle).orEmpty()
    )
}
