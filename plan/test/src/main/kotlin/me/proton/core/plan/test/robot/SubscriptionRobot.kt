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
import me.proton.core.payment.presentation.view.ProtonPaymentButton
import me.proton.core.paymentiap.test.robot.GPBottomSheetSubscribeRobot
import me.proton.core.paymentiap.test.robot.PlayStoreSubscriptionsRobot
import me.proton.core.plan.presentation.R
import me.proton.core.plan.test.BillingCycle
import me.proton.core.plan.test.BillingPlan
import me.proton.core.plan.test.currentCurrency
import me.proton.core.presentation.ui.view.ProtonButton
import me.proton.test.fusion.Fusion.device
import me.proton.test.fusion.Fusion.view
import me.proton.test.fusion.FusionConfig
import me.proton.test.fusion.ui.common.enums.SwipeDirection
import me.proton.test.fusion.ui.espresso.builders.OnView
import kotlin.time.Duration.Companion.seconds

public object SubscriptionRobot {

    private val toolbar = view.withId(R.id.toolbar)
    private val currentPlan = view.withId(R.id.subscription)
    private val planSelection = view.withId(R.id.plan_selection)
    private val planSelectionWithCardView =
        view.withId(R.id.plan_selection).hasDescendant(view.withId(R.id.card_view))
    private val upgradeYourPlanText =
        view.withId(R.id.title).withText(R.string.plans_upgrade_your_plan)
    private val upgradeYourPlanTitle = view.withText(R.string.plans_upgrade_your_plan)
    private val managementInfo = view.withId(R.id.management_info)
    private val noUpgradeAvailableTextView = view.withText(R.string.plans_no_upgrade_available)

    /**
     * [ProtonButton] Manage subscription
     */
    private val playStoreSubscriptionManagementButton: OnView =
        view.withId(R.id.play_store_subscription_management)

    // region Actions

    public fun close() {
        device.pressBack()
    }

    public fun clickManageSubscription(): PlayStoreSubscriptionsRobot {
        playStoreSubscriptionManagementButton.click()
        return PlayStoreSubscriptionsRobot()
    }

    private fun expandAndSelectFirstPlan() {
        planSelectionWithCardView.click()
        view.withCustomMatcher(ViewMatchers.withSubstring("Get"))
    }

    internal fun togglePlanItem(billingPlan: BillingPlan) {
        view.withId(R.id.title).withText(billingPlan.name).scrollTo().click()
    }

    internal fun getPlanButton(billingPlan: BillingPlan): OnView {
        val buttonText =
            FusionConfig.targetContext.getString(R.string.plans_get_proton, billingPlan.name)
        return view.instanceOf(ProtonPaymentButton::class.java).containsText(buttonText)
    }

    private fun getFreePlanButton(): OnView {
        return view.withText(R.string.plans_proton_for_free)
    }

    private fun expandPlan(billingPlan: BillingPlan) {
        togglePlanItem(billingPlan)
    }

    public fun selectExpandedPlan(billingPlan: BillingPlan): GPBottomSheetSubscribeRobot {
        view.withId(R.id.scrollContent).hasDescendant(view.withId(R.id.plans))
            .swipe(SwipeDirection.Up)
        getPlanButton(billingPlan).click()
        return GPBottomSheetSubscribeRobot()
    }

    public fun selectFreePlan() {
        view.withText("Free").await(timeout = 90.seconds) { checkIsDisplayed() }
        view.withText("Free").scrollTo().click()
        getPlanButton(BillingPlan.Free).scrollTo().click()
    }

    public fun selectPlan(billingPlan: BillingPlan): GPBottomSheetSubscribeRobot {
        planSelectionIsDisplayed()
        expandPlan(billingPlan)
        selectExpandedPlan(billingPlan)
        return GPBottomSheetSubscribeRobot()
    }

    public fun togglePlan(billingPlan: BillingPlan) {
        togglePlanItem(billingPlan)
    }

    public fun selectBillingCycle(cycle: BillingCycle): SubscriptionRobot {
        view.withId(R.id.cycleSpinner).await { checkIsDisplayed() }
        view.withId(R.id.cycleSpinner).click()
        view.withId(android.R.id.text1).withText(cycle.value).await { checkIsDisplayed() }
        view.withId(android.R.id.text1).withText(cycle.value).click()
        return this
    }

    // endregion

    // region Verifications

    public fun currentPlanIsDisplayed() {
        currentPlan.await { checkIsDisplayed() }
    }

    public fun planIsDisplayed(plan: BillingPlan) {
        selectBillingCycle(plan.billingCycle)
        expandPlan(plan)

        // Check plan name text
        view.withId(R.id.title).hasAncestor(view.withId(R.id.card_view)).withText(plan.name)
            .scrollToNestedScrollView()
            .checkIsDisplayed()

        // Check plan price text
        view.withId(R.id.price_text).hasAncestor(view.withId(R.id.card_view))
            .containsText(plan.price.get(currentCurrency.name).toString())
            .scrollToNestedScrollView().checkIsDisplayed()

        // Check plan description text
        view.withId(R.id.description).hasAncestor(view.withId(R.id.card_view))
            .containsText(plan.description).scrollToNestedScrollView().checkIsDisplayed()

        // Check entitlements if any
        plan.entitlements.forEach { entitlement ->
            view.withId(R.id.text).hasAncestor(
                view.withId(R.id.content_entitlements).hasAncestor(
                    view.withId(R.id.card_view)
                        .hasDescendant(view.withId(R.id.title).withText(plan.name))
                )
            )
                .withText(entitlement).scrollToNestedScrollView().checkIsDisplayed()
        }
    }

    public fun upgradeYourPlanTextIsDisplayed() {
        upgradeYourPlanText.await { checkIsDisplayed() }
    }

    public fun upgradeYourPlanTitleIsDisplayed() {
        upgradeYourPlanTitle.await { checkIsDisplayed() }
    }

    private fun planSelectionIsDisplayed() {
        planSelection.apply {
            scrollTo()
            checkIsDisplayed()
        }
        planSelectionWithCardView.await { checkIsDisplayed() }
    }

    public fun verifyAtLeastOnePlanIsShown() {
        planSelectionIsDisplayed()
        expandAndSelectFirstPlan()
    }

    public fun verifyNoPaidPlansAreShown() {
        planSelection.checkIsNotDisplayed()
    }

    public fun verifyNoUpgradeAvailable() {
        noUpgradeAvailableTextView.await { checkIsDisplayed() }
    }

    public fun verifyCannotManagePlansFromMobile() {
        managementInfo.await { checkContainsText(R.string.plans_manage_your_subscription_other) }
    }

    public fun verifyPlanRenewalDisplayed(value: String) {
        view.withId(R.id.content_renewal).withText(value).await { checkIsDisplayed() }
    }

    public fun verifyPlanCycleDisplayed(value: String) {
        view.withId(R.id.price_cycle).withText(value).await { checkIsDisplayed() }
    }

    public fun verifyCanGetPlan(billingPlan: BillingPlan) {
        getPlanButton(billingPlan)
            .scrollTo()
            .checkIsDisplayed()
            .checkIsEnabled()
    }

    // endregion
}
