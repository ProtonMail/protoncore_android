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

import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import java.util.Locale

/**
 * Represents billing plan object used in Payments tests with its billing cycle.
 */

public data class BillingPlan(
    val id: String,
    val name: String,
    val nameExtension: String, // Used in PlayStore subscription view. Ex.: "(1 Month)"
    val price: Map<String, Double>,
    val billingCycle: BillingCycle,
    val description: String = "",
    val entitlements: List<String> = listOf()
) {

    override fun toString(): String {
        // Show only BillingPlan name & BillingPlan cycle in parametrized test name
        return "$name, ${billingCycle.value}"
    }

    public companion object {
        // Predefined Billing Cycles
        public val Free: BillingPlan =
            BillingPlan(
                id = "Free",
                name = "Free",
                nameExtension = "",
                price = mapOf(
                    BillingCurrency.USD.name to 0.0,
                    BillingCurrency.CHF.name to 0.0,
                    BillingCurrency.EUR.name to 0.0,
                    BillingCurrency.CAD.name to 0.0,
                    BillingCurrency.AUD.name to 0.0,
                    BillingCurrency.GBP.name to 0.0,
                    BillingCurrency.BRL.name to 0.0
                ),
                billingCycle = BillingCycle(BillingCycle.PAY_MONTHLY),
                entitlements = arrayListOf(
                    "Up to 5 GB drive storage",
                    "1 email address",
                    "3 personal calendars",
                    "Free VPN on a single device",
                    "And the free features of all other Proton products!",
                )
            )
    }
}

public data class BillingCycle(
    val value: String
) {
    public companion object {
        // Predefined payment period values
        public val PAY_ANNUALLY: String =
            stringFromResource(me.proton.core.plan.presentation.R.string.plans_pay_annually)
        public val PAY_BIENNIALLY: String =
            stringFromResource(me.proton.core.plan.presentation.R.string.plans_pay_biennially)
        public val PAY_MONTHLY: String =
            stringFromResource(me.proton.core.plan.presentation.R.string.plans_pay_monthly)
    }
}

public enum class BillingCurrency {
    CHF, USD, EUR, CAD, AUD, GBP, BRL
}

private val localeToCurrency: Map<Locale, BillingCurrency> = mapOf(
    Locale("en", "US") to BillingCurrency.USD, // United States
    Locale("de", "CH") to BillingCurrency.CHF, // Switzerland
    Locale("fr", "FR") to BillingCurrency.EUR, // Lithuania
    Locale("en", "CA") to BillingCurrency.CAD,
    Locale("en", "AU") to BillingCurrency.AUD,
    Locale("en", "GB") to BillingCurrency.GBP, // United Kingdom
    Locale("pt", "BR") to BillingCurrency.BRL
)

public var country: String = Locale.getDefault().country.uppercase(Locale.ROOT)

// By default CHF as per licensed tester account country. Change in your tests if needed.
public var currentCurrency: BillingCurrency = BillingCurrency.CHF
