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

package me.proton.core.test.android.robots.plans

import me.proton.core.plan.presentation.R
import me.proton.core.test.android.instrumented.ProtonTest.Companion.getContext
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.core.test.android.plugins.data.BillingCycle
import me.proton.core.test.android.plugins.data.Currency
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

class SelectPlanRobot : CoreRobot() {

    /**
     * Clicks 'Select' button on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> selectPlan(plan: Plan): T {
        view
            .withId(R.id.selectPlan)
            .hasSibling(
                view.withId(R.id.planNameText).withText(plan.name)
            )
            .wait()
            .click()
        return T::class.java.newInstance()
    }

    /**
     * Changes billing cycle to provided [billingCycle]
     */
    fun changeBillingCycle(billingCycle: BillingCycle): SelectPlanRobot {
        view.withId(R.id.billingCycleSpinner).wait().click()
        view.withText(billingCycle.value).wait().click()
        return this
    }

    /**
     * Changes currency to provided [currency]
     */
    fun changeCurrency(currency: Currency): SelectPlanRobot {
        val currencyString = when(currency) {
            Currency.CHF -> currency.symbol
            else -> "${currency.symbol} ${currency.code}"
        }

        view.withId(R.id.currencySpinner).wait().click()
        view.withText(currencyString).wait().click()
        return this
    }

    class Verify : CoreVerify() {
        fun planDetailsDisplayed(plan: Plan?) {
            view
                .withId(R.id.planContents).hasSibling(
                    view.withId(R.id.planNameText).withText(plan!!.name)
                )
                .wait()
        }

        fun canSelectPlan(plan: Plan) {
            view
                .withId(R.id.selectPlan)
                .hasSibling(
                    view.withId(R.id.planNameText).withText(plan.name)
                )
                .wait()
        }

        fun billingCycleIs(billingCycle: BillingCycle, currency: Currency = Currency.Euro) {
            when (billingCycle) {
                BillingCycle.Monthly -> {
                    view.withId(R.id.planPriceText).withText("${currency.symbol}5.00").wait()
                }
                BillingCycle.Yearly -> {
                    val billedAsString =
                        stringFromResource(R.string.plans_billed_yearly).format("${currency.symbol}48")
                    view.withId(R.id.planPriceDescriptionText).withText(billedAsString).wait()
                    view.withId(R.id.planPriceText).withText("${currency.symbol}4.00").wait()
                }
            }
        }
    }

    companion object {
        val supportedBillingCycles: Array<String> =
            getContext().resources.getStringArray(R.array.supported_billing_cycle)
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}