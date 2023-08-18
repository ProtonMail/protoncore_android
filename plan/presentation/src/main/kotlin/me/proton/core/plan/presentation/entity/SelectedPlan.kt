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

package me.proton.core.plan.presentation.entity

import android.content.res.Resources
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.domain.entity.MASK_NONE
import me.proton.core.plan.domain.entity.PLAN_PRODUCT
import me.proton.core.plan.presentation.R
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.Price

@Parcelize
data class SelectedPlan constructor(
    val planName: String,
    val planDisplayName: String,
    val free: Boolean,
    val cycle: PlanCycle,
    val currency: PlanCurrency,
    val amount: Price,
    val services: Int,
    val type: Int,
    val vendorNames: Map<AppStore, PlanVendorDetails>
) : Parcelable {
    companion object {
        private const val FREE_PLAN_ID = "free"
        fun free(resources: Resources) =
            SelectedPlan(
                planName = FREE_PLAN_ID,
                planDisplayName = resources.getString(R.string.plans_free_name),
                free = true,
                cycle = PlanCycle.FREE,
                currency = PlanCurrency.EUR,
                amount = PRICE_ZERO,
                type = PLAN_PRODUCT,
                services = MASK_NONE,
                vendorNames = emptyMap()
            )
    }
}
