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
import kotlinx.parcelize.Parcelize
import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.domain.entity.Plan
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.Price
import java.util.Date
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

sealed class PlanDetailsItem(
    open val name: String,
    open val displayName: String,
    open val storage: Long,
    open val addresses: Int,
    open val connections: Int,
    open val domains: Int,
    open val members: Int,
    open val calendars: Int
) : Parcelable {

    @Parcelize
    data class CurrentPlanDetailsItem(
        override val name: String,
        override val displayName: String,
        override val storage: Long,
        override val addresses: Int,
        override val connections: Int,
        override val domains: Int,
        override val members: Int,
        override val calendars: Int,
        val cycle: PlanCycle?,
        val currency: PlanCurrency?,
        val price: PlanPricing?,
        val isAutoRenewal: Boolean,
        val endDate: Date?,
        val progressValue: Int,
        val usedSpace: Long,
        val maxSpace: Long,
        val usedAddresses: Int,
        val usedDomains: Int,
        val usedMembers: Int,
        val usedCalendars: Int
    ) : PlanDetailsItem(
        name,
        displayName,
        storage,
        addresses,
        connections,
        domains,
        members,
        calendars
    )

    @Parcelize
    data class FreePlanDetailsItem(
        override val name: String,
        override val displayName: String,
        override val storage: Long,
        override val members: Int,
        override val addresses: Int,
        override val calendars: Int,
        override val domains: Int,
        override val connections: Int
    ) : PlanDetailsItem(
        name,
        displayName,
        storage,
        addresses,
        connections,
        domains,
        members,
        calendars
    )

    @Parcelize
    data class PaidPlanDetailsItem(
        override val name: String,
        override val displayName: String,
        override val storage: Long,
        override val members: Int,
        override val addresses: Int,
        override val calendars: Int,
        override val domains: Int,
        override val connections: Int,
        val cycle: PlanCycle?,
        val price: PlanPricing,
        val defaultPrice: PlanPricing,
        val offers: List<PlanOffer>? = null,
        val currency: PlanCurrency,
        val starred: Boolean,
        val purchaseEnabled: Boolean = true,
        val services: Int,
        val type: Int,
        val promotionPercentage: PlanPromotionPercentage?,
        val vendors: Map<AppStore, PlanVendorDetails>
    ) : PlanDetailsItem(
        name,
        displayName,
        storage,
        addresses,
        connections,
        domains,
        members,
        calendars
    )

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PlanDetailsItem>() {
            override fun areItemsTheSame(oldItem: PlanDetailsItem, newItem: PlanDetailsItem) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: PlanDetailsItem, newItem: PlanDetailsItem) =
                oldItem == newItem
        }
    }
}

@Parcelize
data class PlanPricing(
    val monthly: Price,
    val yearly: Price,
    val twoYearly: Price? = null,
    val other: Price? = null
) : Parcelable {

    companion object {

        fun fromPlan(plan: Plan) = getPricing(plan.pricing, plan.cycle, plan.amount)

        fun fromPlanDefaultPrice(plan: Plan) = getPricing(plan.defaultPricing, plan.cycle, plan.amount)

        private fun getPricing(
            planPricing: me.proton.core.plan.domain.entity.PlanPricing?,
            planCycle: Int?,
            planAmount: Int
        ) =
            planPricing?.let {
                PlanPricing(it.monthly.toDouble(), it.yearly.toDouble(), it.twoYearly?.toDouble())
            } ?: run {
                val cycle = PlanCycle.map[planCycle]
                val monthly = if (cycle == PlanCycle.MONTHLY) planAmount else PRICE_ZERO
                val yearly = if (cycle == PlanCycle.YEARLY) planAmount else PRICE_ZERO
                val twoYears = if (cycle == PlanCycle.TWO_YEARS) planAmount else PRICE_ZERO
                val otherPrice = if (planAmount != 0 && cycle != null) {
                    planAmount
                } else null
                PlanPricing(monthly.toDouble(), yearly.toDouble(), twoYears.toDouble(), otherPrice?.toDouble())
            }
    }
}

@Parcelize
data class PlanPromotionPercentage(
    val monthly: Int? = null,
    val yearly: Int? = null,
    val twoYearly: Int? = null,
    val other: Int? = null
) : Parcelable {
    companion object {
        private const val HUNDRED = 100
        fun fromPlan(plan: Plan): PlanPromotionPercentage? {
            val pricing = plan.pricing
            val defaultPricing = plan.defaultPricing
            return when {
                pricing == null -> null
                defaultPricing == null -> null
                else -> {
                    PlanPromotionPercentage(
                        monthly = calculatePricePercentage(defaultPricing.monthly, pricing.monthly),
                        yearly = calculatePricePercentage(defaultPricing.yearly, pricing.yearly),
                        twoYearly = calculatePricePercentage(defaultPricing.twoYearly, pricing.twoYearly)
                    )
                }
            }
        }

        private fun calculatePricePercentage(defaultPrice: Int?, promoPrice: Int?): Int {
            if (defaultPrice == null || promoPrice == null) return 0

            return if (defaultPrice < promoPrice) 0
            else (promoPrice / defaultPrice.toDouble() * HUNDRED - HUNDRED).roundToAbsoluteInt()
        }
    }
}

private fun Double.roundToAbsoluteInt(): Int = roundToInt().absoluteValue

@Parcelize
data class PlanOfferPricing(
    val monthly: Price? = null,
    val yearly: Price? = null,
    val twoYearly: Price? = null,
    val other: Price? = null
) : Parcelable

@Parcelize
data class PlanOffer(
    val name: String,
    val startTime: Long,
    val endTime: Long,
    val pricing: PlanOfferPricing
) : Parcelable

@Parcelize
data class PlanVendorDetails(
    val customerId: String,
    val names: Map<PlanCycle, String>
) : Parcelable
