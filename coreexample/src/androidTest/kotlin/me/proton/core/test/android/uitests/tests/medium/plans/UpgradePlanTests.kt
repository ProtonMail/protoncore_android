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
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.After
import org.junit.Test

class UpgradePlanTests : BaseTest() {

    private val selectPlanRobot = SelectPlanRobot()
    private val coreExampleRobot = CoreexampleRobot()
    private val freeUser = quark.userCreate()
    private val paidUser = users.getUser { it.isPaid }

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Test
    @SmokeTest
    fun userWithFreePlan() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
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
        login(paidUser)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }

    @Test
    fun userWithPaidPlanCardAndIAPPayment() {
        quark.jailUnban()
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = true)
        login(paidUser)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsNotDisplayed() }
    }
}
