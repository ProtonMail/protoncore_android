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

import me.proton.core.test.android.plugins.data.Plan
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.login.LoginRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class CurrentPlanTests : BaseTest() {

    @Before
    fun login() {
        AddAccountRobot()
            .signIn()
    }

    private fun navigateUserToCurrentPlans(user: User): SelectPlanRobot {
        LoginRobot()
            .loginUser<CoreexampleRobot>(user)
            .verify { primaryUserIs(user) }

        return CoreexampleRobot()
            .plansCurrent()
    }

    @Test
    fun userWithFreePlan() {
        val freeUser = users.getUser { !it.isPaid }
        navigateUserToCurrentPlans(freeUser)
            .scrollToPlan(Plan.Dev)
            .verify {
                canUpgradeToPlan(Plan.Dev)
                planDetailsDisplayed(Plan.Dev)
            }
    }

    @Test
    fun userWithPaidPlan() {
        val paidUser = users.getUser { it.isPaid }
        navigateUserToCurrentPlans(paidUser)
            .verify { planDetailsDisplayed(paidUser.plan) }
    }
}
