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
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.After
import org.junit.Test

class CurrentPlanTests : BaseTest() {

    val user = quark.userCreate()

    private fun navigateUserToCurrentPlans(user: User): SelectPlanRobot {
        quark.setPaymentMethods(AppStore.GooglePlay, card = true, paypal = false, inApp = false)
        login(user)

        return CoreexampleRobot()
            .plansCurrent()
    }

    @After
    fun setDefaults() {
        quark.setDefaultPaymentMethods()
    }

    @Test
    fun userWithFreePlan() {
        navigateUserToCurrentPlans(user)
            .scrollToPlan(Plan.Dev)
            .toggleExpandPlan(Plan.Dev)
            .verify {
                planDetailsDisplayedInsideRecyclerView(Plan.Dev)
                canUpgradeToPlan(Plan.Dev)
            }
    }

    @Test
    fun userWithPaidPlan() {
        val paidUser = users.getUser { it.isPaid }
        navigateUserToCurrentPlans(paidUser)
            .verify {
                planDetailsDisplayed(paidUser.plan)
            }
    }
}
