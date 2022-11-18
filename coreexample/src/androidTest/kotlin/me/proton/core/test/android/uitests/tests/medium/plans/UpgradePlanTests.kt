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

package me.proton.core.test.android.uitests.tests.medium.plans

import me.proton.core.domain.entity.AppStore
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.After
import org.junit.Test

class UpgradePlanTests : BaseTest() {

    private val selectPlanRobot = SelectPlanRobot()
    private val coreExampleRobot = CoreexampleRobot()

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Test
    @SmokeTest
    fun userWithFreePlan() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        val freeUser = quark.userCreate().first
        login(freeUser)

        coreExampleRobot
            .plansUpgrade()
            .scrollToPlan(Plan.Dev)
            .toggleExpandPlan(Plan.Dev)
            .verify {
                planDetailsDisplayedInsideRecyclerView(Plan.Dev)
                canUpgradeToPlan(Plan.Dev)
            }

        selectPlanRobot
            .close<CoreexampleRobot>()
            .verify { accountSwitcherDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardPayment() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        val paidUser = users.getUser { it.isPaid }
        login(paidUser)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUser = users.getUser { it.isPaid }
        login(paidUser)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment1month() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle1 = User(plan = Plan.MailPlus)
        val cycle1 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 1
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle1, cycle1.cycleDurationMonths)
        login(user)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment12months() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle12 = User(plan = Plan.MailPlus)
        val cycle12 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 12
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle12, cycle12.cycleDurationMonths)
        login(user)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment15months() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle15 = User(plan = Plan.MailPlus)
        val cycle15 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 15
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle15, cycle15.cycleDurationMonths)
        login(user)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment30months() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        val paidUserCycle15 = User(plan = Plan.MailPlus)
        val cycle15 = PlanCycle.OTHER.apply {
            cycleDurationMonths = 30
        }
        val user = quark.seedNewSubscriberWithCycle(paidUserCycle15, cycle15.cycleDurationMonths)
        login(user)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }
}
