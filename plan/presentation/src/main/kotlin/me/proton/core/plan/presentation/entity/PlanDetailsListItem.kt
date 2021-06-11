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
import androidx.recyclerview.widget.DiffUtil
import kotlinx.android.parcel.Parcelize
import me.proton.core.plan.domain.entity.Plan

sealed class PlanDetailsListItem(
    open val id: String,
    open val current: Boolean
) : Parcelable {

    @Parcelize
    data class FreePlanDetailsListItem(
        override val id: String,
        override val current: Boolean,
        val selectable: Boolean = true
    ) : PlanDetailsListItem(id, current)

    @Parcelize
    data class PaidPlanDetailsListItem(
        override val id: String,
        val name: String,
        val price: PlanPricing?,
        override val current: Boolean,
        val selectable: Boolean = true,
        val upgrade: Boolean,
        val renewalDate: String?
    ) : PlanDetailsListItem(id, current)

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PlanDetailsListItem>() {
            override fun areItemsTheSame(oldItem: PlanDetailsListItem, newItem: PlanDetailsListItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PlanDetailsListItem, newItem: PlanDetailsListItem) =
                oldItem == newItem
        }
    }
}

@Parcelize
data class PlanPricing(
    val monthly: Int,
    val yearly: Int,
    val twoYearly: Int? = null
) : Parcelable {

    companion object {
        fun fromPlan(plan: Plan) =
            plan.pricing?.let {
                PlanPricing(it.monthly, it.yearly, it.twoYearly)
            } ?: run {
                null
            }
    }
}
