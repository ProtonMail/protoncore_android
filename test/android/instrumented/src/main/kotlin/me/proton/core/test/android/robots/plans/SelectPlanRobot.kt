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
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
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
            Plan.Dev -> 2
            Plan.Free -> 3
            else -> 0
        }

        recyclerView.withId(R.id.planListRecyclerView)
            .onItemAtPosition(position).scrollTo()
        return this
    }

    /**
     * Clicks on the chevron button on a provided [plan]
     */
    fun expandPlan(plan: Plan): SelectPlanRobot {
        view.withId(R.id.collapse)
            .hasSibling(
                view.withText(plan.text)
            ).scrollToNestedScrollView()
            .click()
        return this
    }

    /**
     * Clicks 'Select' button on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> selectPlan(plan: Plan): T =
        scrollToPlan(plan)
            .clickPlanButtonWithText(plan, R.string.plans_get_proton)

    /**
     * Clicks 'Upgrade' button on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> upgradeToPlan(plan: Plan): T =
            expandPlan(plan)
            .scrollToPlan(plan)
            .clickPlanButtonWithText(plan, R.string.plans_upgrade_plan)

    /**
     * Clicks button with [textId] resource on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> clickPlanButtonWithText(plan: Plan, @StringRes textId: Int): T {
        view.withId(R.id.select)
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

    fun scrollForward(): SelectPlanRobot {
        UiScrollable(
            UiSelector().scrollable(true)
        ).scrollForward()
        return this
    }

    /**
     * Changes currency to provided [currency]
     */
    fun changeCurrency(currency: Currency): SelectPlanRobot {
        view.instanceOf(NestedScrollView::class.java).swipeUp()
        view.withId(R.id.currencySpinner).click()
        view.withText(currency.code).click()
        return this
    }

    inner class Verify : CoreVerify() {

        fun planDetailsDisplayed(plan: Plan) {
            view.withText(plan.text).checkDisplayed()
        }

        fun planDetailsDisplayedInsideRecyclerView(plan: Plan) {
            expandPlan(plan)
            scrollToPlan(plan)
            view.withText(plan.text).checkDisplayed()
        }

        fun planDetailsNotDisplayed(plan: Plan) {
            view.withText(plan.text).checkNotDisplayed()
        }

        fun canSelectPlan(plan: Plan) {
            expandPlan(plan)
            scrollToPlan(plan)

            view.withId(R.id.select)
                .isDescendantOf(
                    view.withId(R.id.planGroup).hasSibling(
                        view.withText(plan.text)
                    )
                ).checkDisplayed()
        }

        fun canUpgradeToPlan(plan: Plan) {
            view.withId(R.id.select)
                .isDescendantOf(
                    view.withId(R.id.planGroup).hasSibling(
                        view.withText(plan.text)
                    )
                ).checkDisplayed()
        }

        fun billingCycleIs(plan: Plan, billingCycle: BillingCycle, currency: Currency = Currency.Euro) {
            expandPlan(plan)
            view.withId(R.id.planPriceText).withText("${currency.symbol}${billingCycle.monthlyPrice}")
            val yearlyPriceString = String.format("%.2f", billingCycle.yearlyPrice)
            val billedAsString =
                stringFromResource(R.string.plans_billed_yearly).format("${currency.symbol}$yearlyPriceString")
            if (billingCycle == BillingCycle.Monthly) {
                view.withId(R.id.planPriceDescriptionText)
                    .isDescendantOf(
                        view.withId(R.id.planGroup).hasSibling(
                            view.withText(plan.text)
                        )
                    ).checkNotDisplayed()
            } else {
                view.withId(R.id.planPriceDescriptionText).withText(billedAsString)
                    .isDescendantOf(
                        view.withId(R.id.planGroup).hasSibling(
                            view.withText(plan.text)
                        )
                    ).checkDisplayed()
            }
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
