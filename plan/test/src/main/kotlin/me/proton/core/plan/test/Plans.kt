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

package me.proton.core.plan.test

/**
 * Represents billing plan object used in Payments tests with its billing cycle.
 */

public data class BillingPlan(
    val id: String,
    val name: String,
    val price: Double,
    val billingCycle: BillingCycle
) {

    override fun toString(): String {
        // Show only BillingPlan name & BillingPlan cycle in parametrized test
        return "$name, ${billingCycle.value}"
    }

    public companion object {
        // Predefined Billing Cycles
        public val Free: BillingPlan =
            BillingPlan("Free", "Free", 0.0, BillingCycle(BillingCycle.PAY_MONTHLY))
    }
}

public data class BillingCycle(
    val value: String
) {
    public companion object {
        // Predefined payment period values
        public const val PAY_ANNUALLY: String = "Pay annually"
        public const val PAY_MONTHLY: String = "Pay monthly"
    }
}
