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

package me.proton.core.test.android.uitests.tests.medium.auth.signup

import me.proton.core.test.android.instrumented.utils.StringUtils.randomString
import me.proton.core.test.android.plugins.data.Plan.Dev
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.CodeVerificationRobot
import me.proton.core.test.android.robots.auth.signup.SignupFinishedRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ExternalSetupTests : BaseTest() {

    private val chooseUsernameRobotExt = ChooseUsernameRobot()

    @Before
    fun closeWelcomeScreen() {
        quark.jailUnban()
        CoreRobot()
            .device
            .clickBackBtn()

        CoreexampleRobot()
            .signupExternal()
            .verify { chooseUsernameElementsDisplayed() }
    }

    @Test
    fun switchToInternalAndBack() {
        chooseUsernameRobotExt
            .switchSignupType()
            .verify {
                chooseUsernameElementsDisplayed()
                suffixNotDisplayed()
                switchToExternalDisplayed()
            }

        chooseUsernameRobotExt
            .switchSignupType()
            .verify {
                chooseUsernameElementsDisplayed()
                switchToSecureDisplayed()
            }
    }

    @Test
    @Ignore("May be enabled after !548 is merged (External account signups).")
    fun emailCodeVerification() {
        val user = User(name = "${randomString()}@example.lt")
        val defaultCode = quark.defaultVerificationCode

        val codeVerificationRobot = chooseUsernameRobotExt
            .username(user.name)
            .next()
            .setAndConfirmPassword<CodeVerificationRobot>(user.password)
            .setCode(defaultCode)

        if (features.paymentsAndroidDisabled) {
            codeVerificationRobot.verifyCode<SignupFinishedRobot>().verify {
                signupFinishedDisplayed()
            }
        } else {
            codeVerificationRobot.verifyCode<SelectPlanRobot>()
                .toggleExpandPlan(Dev)
                .verify { canSelectPlan(Dev) }
        }
    }
}
