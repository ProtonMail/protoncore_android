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

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.Price

@Parcelize
data class SelectedPlan(
    val planId: String,
    val planName: String,
    val free: Boolean,
    val cycle: Cycle,
    val currency: PlanCurrency,
    val amount: Price
) : Parcelable {
    companion object {
        const val FREE_PLAN_ID = "free"
        fun free(freePlanName: String) =
            SelectedPlan(FREE_PLAN_ID, freePlanName, true, Cycle.YEARLY, PlanCurrency.EUR, PRICE_ZERO)
    }
}
