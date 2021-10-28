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

import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.test.android.instrumented.utils.StringUtils.randomString
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.plugins.data.Plan.Free
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.ChooseUsernameRobot
import me.proton.core.test.android.robots.humanverification.CodeVerificationRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
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
                suffixDisplayed(BuildConfig.HOST)
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
    fun emailCodeVerification() {
        val user = User(name = "${randomString()}@example.lt")
        val defaultCode = quark.defaultVerificationCode

        chooseUsernameRobotExt
            .username(user.name)
            .next()
            .setAndConfirmPassword<CodeVerificationRobot>(user.password)
            .setCode(defaultCode)
            .verifyCode<SelectPlanRobot>()
            .verify { canSelectPlan(Free) }
    }
}
