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
                view.withId(R.id.planNameText).withText(plan.text)
            )
            .click()
        return T::class.java.newInstance()
    }

    /**
     * Changes billing cycle to provided [billingCycle]
     */
    fun changeBillingCycle(billingCycle: BillingCycle): SelectPlanRobot {
        view.withId(R.id.billingCycleSpinner).click()
        view.withText(billingCycle.value).click()
        return this
    }

    /**
     * Changes currency to provided [currency]
     */
    fun changeCurrency(currency: Currency): SelectPlanRobot {
        view.withId(R.id.currencySpinner).click()
        view.withText(currency.code).click()
        return this
    }

    class Verify : CoreVerify() {
        fun planDetailsDisplayed(plan: Plan?) {
            view
                .withId(R.id.planContents).hasSibling(
                    view.withId(R.id.planNameText).withText(plan!!.name)
                )
        }

        fun canSelectPlan(plan: Plan) {
            view
                .withId(R.id.selectPlan)
                .hasSibling(
                    view.withId(R.id.planNameText).withText(plan.name)
                )
        }

        fun billingCycleIs(billingCycle: BillingCycle, currency: Currency = Currency.Euro) {
            when (billingCycle) {
                BillingCycle.Monthly -> {
                    view.withId(R.id.planPriceText).withText("${currency.symbol}4.99")
                }
                BillingCycle.Yearly -> {
                    val billedAsString =
                        stringFromResource(R.string.plans_billed_yearly).format("${currency.symbol}47.88")
                    view.withId(R.id.planPriceDescriptionText).checkContains(billedAsString)
                    view.withId(R.id.planPriceText).checkContains("${currency.symbol}3.99")
                }
            }
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
