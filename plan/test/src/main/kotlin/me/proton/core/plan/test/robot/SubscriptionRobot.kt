/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.plan.test.robot

import androidx.test.espresso.matcher.ViewMatchers
import me.proton.core.plan.presentation.R
import me.proton.test.fusion.Fusion.view

public object SubscriptionRobot {

    private val toolbar = view.withId(R.id.toolbar)
    private val currentPlan = view.withId(R.id.subscription)
    private val planSelection = view.withId(R.id.plan_selection)
    private val upgradeYourPlanText = view.withText(R.string.plans_upgrade_your_plan)
        .withClassName("TextView")
    private val upgradeYourPlanTitle = view.withText(R.string.plans_upgrade_your_plan)
        .hasAncestor(toolbar)
    private val cannotUpgrade = view.withText(R.string.plans_can_not_upgrade_from_mobile)
    private val cardView = view.withId(R.id.card_view)

    private fun currentPlanIsDisplayed() {
        currentPlan.await { checkIsDisplayed() }
    }

    private fun currentPlanIsNotDisplayed() {
        currentPlan.checkIsNotDisplayed()
    }

    private fun upgradeYourPlanTextIsDisplayed() {
        upgradeYourPlanText.await { checkIsDisplayed() }
    }

    private fun upgradeYourPlanTitleIsDisplayed() {
        upgradeYourPlanTitle.await { checkIsDisplayed() }
    }

    private fun planSelectionIsDisplayed() {
        planSelection.scrollTo()
        planSelection.await { checkIsDisplayed() }
        planSelection.hasDescendant(cardView).checkIsDisplayed()
    }

    private fun expandAndSelectFirstPlan() {
        planSelection.hasDescendant(cardView).click()
        view.withCustomMatcher(ViewMatchers.withSubstring("Get"))
    }

    public fun verifySubscriptionIsShown() {
        currentPlanIsDisplayed()
    }

    public fun verifyUpgradeIsShown() {
        currentPlanIsNotDisplayed()
    }

    public fun verifyUpgradeYourPlanTextIsDisplayed() {
        upgradeYourPlanTextIsDisplayed()
    }

    public fun verifyUpgradeYourPlanTitleIsDisplayed() {
        upgradeYourPlanTitleIsDisplayed()
    }

    public fun verifyAtLeastOnePlanIsShown() {
        planSelectionIsDisplayed()
        expandAndSelectFirstPlan()
    }

    public fun verifyNoPaidPlansAreShown() {
        planSelection.checkIsNotDisplayed()
    }

    public fun verifyCannotUpgradeFromMobile() {
        cannotUpgrade.checkIsDisplayed()
    }
}
