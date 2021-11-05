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

import androidx.annotation.StringRes
import androidx.core.widget.NestedScrollView
import me.proton.core.plan.presentation.R
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.core.test.android.plugins.data.BillingCycle
import me.proton.core.test.android.plugins.data.Currency
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify


class SelectPlanRobot : CoreRobot() {

    /**
     * Scrolls to a provided [plan]
     * @return an instance of [SelectPlanRobot]
     */
    fun scrollToPlan(plan: Plan): SelectPlanRobot {
        // Only one paid plan is currently implemented
        val position = when (plan) {
            Plan.Dev -> 0
            Plan.Free -> 1
            else -> -1
        }

        recyclerView.withId(R.id.planListRecyclerView)
            .onItemAtPosition(position).scrollTo()
        return this
    }

    /**
     * Clicks 'Select' button on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> selectPlan(plan: Plan): T =
        scrollToPlan(plan)
            .clickPlanButtonWithText(plan, R.string.plans_select_plan)

    /**
     * Clicks 'Upgrade' button on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> upgradeToPlan(plan: Plan): T =
        scrollToPlan(plan)
            .clickPlanButtonWithText(plan, R.string.plans_upgrade_plan)

    /**
     * Clicks button with [textId] resource on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> clickPlanButtonWithText(plan: Plan, @StringRes textId: Int): T {
        view.withText(textId)
            .isDescendantOf(
                view.withId(R.id.planGroup).hasSibling(
                    view.withText(plan.text)
                )
            ).click()
        return T::class.java.newInstance()
    }

    /**
     * Changes billing cycle to provided [billingCycle]
     */
    fun changeBillingCycle(billingCycle: BillingCycle): SelectPlanRobot {
        view.instanceOf(NestedScrollView::class.java).swipeDown()
        view.withId(R.id.billingCycleSpinner).click()
        view.withText(billingCycle.value).click()
        return this
    }

    /**
     * Changes currency to provided [currency]
     */
    fun changeCurrency(currency: Currency): SelectPlanRobot {
        view.withId(R.id.billingCycleSpinner).checkDisplayed()
        view.instanceOf(NestedScrollView::class.java).swipeUp()
        view.withId(R.id.currencySpinner).click()
        view.withText(currency.code).click()
        return this
    }

    class Verify : CoreVerify() {

        fun planDetailsDisplayed(plan: Plan) {
            view.withText(plan.text).checkDisplayed()
        }

        fun canSelectPlan(plan: Plan) {
            view.withText(R.string.plans_select_plan)
                .isDescendantOf(
                    view.withId(R.id.planGroup).hasSibling(
                        view.withText(plan.text)
                    )
                ).checkDisplayed()
        }

        fun canUpgradeToPlan(plan: Plan) {
            view.withText(R.string.plans_upgrade_plan)
                .isDescendantOf(
                    view.withId(R.id.planGroup).hasSibling(
                        view.withText(plan.text)
                    )
                ).checkDisplayed()
        }

        fun billingCycleIs(billingCycle: BillingCycle, currency: Currency = Currency.Euro) {
            view.withId(R.id.planPriceText).withText("${currency.symbol}${billingCycle.monthlyPrice}")
            when (billingCycle) {
                BillingCycle.Monthly -> {
                    view.withText(R.string.plans_save_20).checkDisplayed()
                }
                else -> {
                    val yearlyPriceString = String.format("%.2f", billingCycle.yearlyPrice)
                    val billedAsString =
                        stringFromResource(R.string.plans_billed_yearly).format("${currency.symbol}$yearlyPriceString")
                    view.withId(R.id.planPriceDescriptionText).withText(billedAsString).checkDisplayed()
                    view.withText(R.string.plans_renew_info).checkDisplayed()
                }
            }
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
