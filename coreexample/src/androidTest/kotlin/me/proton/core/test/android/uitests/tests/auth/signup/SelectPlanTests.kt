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

package me.proton.core.test.android.uitests.tests.auth.signup

import me.proton.core.test.android.plugins.data.Plan.Plus
import me.proton.core.test.android.plugins.data.Plan.Free
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.humanverification.HumanVerificationRobot
import me.proton.core.test.android.robots.payments.AddCreditCardRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class SelectPlanTests: BaseTest() {

    private val selectPlanRobot = SelectPlanRobot()

    @Before
    fun goToPlanSelection() {
        val user = User()
        AddAccountRobot()
            .createAccount()
            .username(user.name)
            .next()
            .setAndConfirmPassword<RecoveryMethodsRobot>(user.password)
            .skip()
            .skipConfirm()
            .verify {
                planDetailsDisplayed(Free)
                canSelectPlan(Free)
            }
    }

    @Test
    fun selectFree() {
        selectPlanRobot
            .selectPlan<HumanVerificationRobot>(Free)
            .verify {
                hvElementsDisplayed()
                captchaDisplayed()
            }
    }

    @Test
    fun selectPlus() {
        selectPlanRobot
            .selectPlan<AddCreditCardRobot>(Plus)
            .verify { addCreditCardElementsDisplayed() }
    }
}