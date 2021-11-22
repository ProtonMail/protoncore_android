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

import me.proton.core.test.android.plugins.data.BillingCycle
import me.proton.core.test.android.plugins.data.Currency
import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.Test

class UpgradePlanTests : BaseTest() {

    private val selectPlanRobot = SelectPlanRobot()
    private val coreExampleRobot = CoreexampleRobot()
    private val freeUser = users.getUser { !it.isPaid }
    private val paidUser = users.getUser { it.isPaid }

    @Test
    @SmokeTest
    fun userWithFreePlan() {

        login(freeUser)

        coreExampleRobot
            .plansUpgrade()
            .scrollToPlan(Plan.Dev)
            .verify {
                canUpgradeToPlan(Plan.Dev)
                planDetailsDisplayed(Plan.Dev)
            }

        selectPlanRobot
            .close<CoreexampleRobot>()
            .verify { accountSwitcherDisplayed() }
    }

    @Test
    fun userWithPaidPlan() {

        login(paidUser)

        coreExampleRobot
            .plansUpgrade()
            .verify { planDetailsDisplayed(paidUser.plan) }
    }

    @Test
    fun changeBillingCycleAndCurrency() {

        login(freeUser)

        coreExampleRobot
            .plansUpgrade()
            .scrollToPlan(Plan.Dev)

        BillingCycle.values().forEach { cycle ->
            selectPlanRobot.changeBillingCycle(cycle)
            Currency.values().forEach { currency ->
                selectPlanRobot
                    .changeCurrency(currency)
                    .verify { billingCycleIs(cycle, currency) }
            }
        }
    }
}
