/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.test.quark.data.Plan
import me.proton.test.fusion.Fusion.device
import me.proton.test.fusion.Fusion.view
import me.proton.test.fusion.FusionConfig
import me.proton.test.fusion.ui.espresso.builders.OnView

public object SubscriptionRobot {

    private val toolbar = view.withId(R.id.toolbar)
    private val currentPlan = view.withId(R.id.subscription)
    private val planSelection = view.withId(R.id.plan_selection)
    private val planSelectionWithCardView =
        view.withId(R.id.plan_selection).hasDescendant(view.withId(R.id.card_view))
    private val upgradeYourPlanText =
        view.withId(R.id.title).withText(R.string.plans_upgrade_your_plan)
    private val upgradeYourPlanTitle = view.withText(R.string.plans_upgrade_your_plan)
        .hasAncestor(toolbar)
    private val cannotUpgrade = view.withText(R.string.plans_can_not_upgrade_from_mobile)
    private val managementInfo = view.withId(R.id.management_info)
    private val noUpgradeAvailableTextView = view.withText(R.string.plans_no_upgrade_available)

    // region Actions

    public fun close() {
        device.pressBack()
    }

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
        planSelectionWithCardView.await { checkIsDisplayed() }
    }

    private fun expandAndSelectFirstPlan() {
        planSelectionWithCardView.click()
        view.withCustomMatcher(ViewMatchers.withSubstring("Get"))
    }

    private fun togglePlanItem(plan: Plan) {
        view.withId(R.id.title).containsText(plan.text).scrollTo().click()
    }

    private fun getPlanButton(plan: Plan): OnView {
        val buttonText = FusionConfig.targetContext.getString(R.string.plans_get_proton, plan.text)
        return view.withId(R.id.content_button).containsText(buttonText)
    }

    private fun expandAndSelectPlan(plan: Plan) {
        togglePlanItem(plan)

        getPlanButton(plan)
            .scrollTo()
            .click()
    }

    public fun selectPlan(plan: Plan) {
        planSelectionIsDisplayed()
        expandAndSelectPlan(plan)
    }

    public fun togglePlan(plan: Plan) {
        togglePlanItem(plan)
    }

    // endregion

    // region Verifications

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

    public fun verifyNoUpgradeAvailable() {
        noUpgradeAvailableTextView.await { checkIsDisplayed() }
    }

    public fun verifyCannotManagePlansFromMobile() {
        managementInfo.checkContainsText(R.string.plans_manage_your_subscription_other)
    }

    public fun verifyPlanRenewalDisplayed(value: String) {
        view.withId(R.id.content_renewal).withText(value).await { checkIsDisplayed() }
    }

    public fun verifyPlanCycleDisplayed(value: String) {
        view.withId(R.id.price_cycle).withText(value).await { checkIsDisplayed() }
    }

    public fun verifyCanGetPlan(plan: Plan) {
        getPlanButton(plan)
            .scrollTo()
            .checkIsDisplayed()
            .checkIsEnabled()
    }

    // endregion
}
