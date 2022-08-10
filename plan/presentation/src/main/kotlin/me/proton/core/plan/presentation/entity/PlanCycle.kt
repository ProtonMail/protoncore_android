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

import me.proton.core.paymentcommon.domain.entity.SubscriptionCycle
import me.proton.core.presentation.utils.PRICE_ZERO
import me.proton.core.presentation.utils.Price
import me.proton.core.util.kotlin.exhaustive

enum class PlanCycle(val value: Int) {
    FREE(0), MONTHLY(1), YEARLY(12), TWO_YEARS(24);

    fun getPrice(pricing: PlanPricing): Price? {
        return when (this) {
            MONTHLY -> pricing.monthly
            YEARLY -> pricing.yearly
            TWO_YEARS -> pricing.twoYearly
            FREE -> PRICE_ZERO
        }?.toDouble().exhaustive
    }

    fun toSubscriptionCycle(): SubscriptionCycle =
        when (this) {
            MONTHLY -> SubscriptionCycle.MONTHLY
            YEARLY -> SubscriptionCycle.YEARLY
            TWO_YEARS -> SubscriptionCycle.TWO_YEARS
            FREE -> SubscriptionCycle.FREE
        }.exhaustive

    companion object {
        val map = values().associateBy { it.value }
    }
}

