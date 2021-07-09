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

package me.proton.core.test.android.uitests.tests.plans

import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class FreePlanTests: BaseTest() {

    private val coreexampleRobot = CoreexampleRobot()
    private val freeUser = users.getUser { it.plan == User.Plan.Free && it.isDefault }

    @Before
    fun login() {
        AddAccountRobot()
            .signIn()
            .loginUser<CoreexampleRobot>(freeUser)
            .verify { coreexampleElementsDisplayed() }
    }

    @Test
    fun currentPlan() {
        coreexampleRobot
            .plansCurrent()
            .verify {
                planDetailsDisplayed(User.Plan.Free)
                planDetailsDisplayed(User.Plan.Plus)
                canSelectPlan(User.Plan.Plus)
            }
    }

    @Test
    fun upgradePlan() {
        coreexampleRobot
            .plansUpgrade()
            .verify {
                planDetailsDisplayed(User.Plan.Plus)
                canSelectPlan(User.Plan.Plus)
            }
    }
}