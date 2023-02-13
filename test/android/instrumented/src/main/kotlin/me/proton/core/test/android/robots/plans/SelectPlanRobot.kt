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

import androidx.core.widget.NestedScrollView
import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.view.PlanItemView
import me.proton.core.test.quark.data.BillingCycle
import me.proton.core.test.quark.data.Currency
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

class SelectPlanRobot : CoreRobot() {

    /**
     * Scrolls to a provided [plan]
     * @return an instance of [SelectPlanRobot]
     */
    fun scrollToPlan(plan: Plan): SelectPlanRobot {
        view.instanceOf(PlanItemView::class.java)
            .hasDescendant(view.withId(R.id.planNameText).withText(plan.text))
            .scrollTo()
        return this
    }

    /**
     * Clicks on a provided [plan] to expand/collapse it.
     * Note: We don't use the chevron button, because it's not displayed if there's only one plan.
     */
    fun toggleExpandPlan(plan: Plan): SelectPlanRobot {
        view
            .withChild(view.withId(R.id.planNameText).withText(plan.text))
            .scrollToNestedScrollView()
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
            .clickPlanButtonWithText(plan)

    /**
     * Clicks 'Upgrade' button on a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> upgradeToPlan(plan: Plan): T =
        toggleExpandPlan(plan)
            .scrollToPlan(plan)
            .clickPlanButtonWithText(plan)

    /**
     * Clicks button with corresponding to a provided [plan]
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> clickPlanButtonWithText(plan: Plan): T {
        view.withId(R.id.select)
            .isDescendantOf(
                view.withId(R.id.planGroup).hasSibling(
                    view.withText(plan.text)
                )
            )
            .scrollTo()
            .click()
        return T::class.java.newInstance()
    }

    /**
     * Changes billing cycle to provided [billingCycle]
     */
    fun changeBillingCycle(billingCycle: BillingCycle): SelectPlanRobot {
        view.instanceOf(NestedScrollView::class.java).swipeDown()
        view.withText(billingCycle.value).click()
        return this
    }

    /**
     * Changes currency to provided [currency]
     */
    fun changeCurrency(currency: Currency): SelectPlanRobot {
        view.withId(R.id.currencySpinner)
            .checkEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
            .scrollTo()
            .click()

        view.withText(currency.code).click()
        return this
    }

    inner class Verify : CoreVerify() {

        fun planDetailsDisplayed(plan: Plan) {
            view.withText(plan.text).checkDisplayed()
        }

        fun planDetailsDisplayedInsideRecyclerView(plan: Plan) {
            scrollToPlan(plan)
            view.withText(plan.text).checkDisplayed()
        }

        fun planCycleDisplayed(value: String) {
            view.withText(value).checkDisplayed()
        }

        fun planRenewalDisplayed(value: String) {
            view.withText(value).checkDisplayed()
        }

        fun planDetailsNotDisplayed() {
            view.withId(R.id.currentPlan).checkNotDisplayed()
        }

        fun currentPlanDetailsDisplayed() {
            view.withId(R.id.currentPlan).checkDisplayed()
        }

        fun plansNotDisplayed() {
            view.withId(R.id.plansView).checkNotDisplayed()
        }

        fun plansDisplayed() {
            view.withId(R.id.plansView).checkDisplayed()
        }

        fun canSelectPlan(plan: Plan) {
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
            toggleExpandPlan(plan)
            view.withId(R.id.planPriceText).withText("${currency.symbol}${billingCycle.monthlyPrice}")
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
