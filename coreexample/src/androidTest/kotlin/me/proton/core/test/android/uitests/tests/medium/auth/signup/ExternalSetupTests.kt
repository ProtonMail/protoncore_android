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

import me.proton.core.test.quark.data.Plan.Dev
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.signup.ChooseExternalEmailRobot
import me.proton.core.test.android.robots.auth.signup.ChooseInternalEmailRobot
import me.proton.core.test.android.robots.auth.signup.CodeVerificationRobot
import me.proton.core.test.android.robots.auth.signup.SignupFinishedRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.util.kotlin.random
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ExternalSetupTests : BaseTest() {

    private val chooseExternalEmailRobot = ChooseExternalEmailRobot()
    private val chooseInternalEmailRobot = ChooseInternalEmailRobot()

    @Before
    fun closeWelcomeScreen() {
        quark.jailUnban()
        CoreRobot()
            .device
            .clickBackBtn()

        CoreexampleRobot()
            .signupExternal()
            .verify { chooseExternalEmailElementsDisplayed() }
    }

    @Test
    fun switchToInternalAndBack() {
        chooseExternalEmailRobot
            .switchSignupType()
            .verify {
                chooseInternalEmailElementsDisplayed()
                suffixNotDisplayed()
                switchToExternalDisplayed()
            }

        chooseInternalEmailRobot
            .switchSignupType()
            .verify {
                chooseExternalEmailElementsDisplayed()
                switchToSecureDisplayed()
            }
    }

    @Test
    @Ignore("May be enabled after !548 is merged (External account signups).")
    fun emailCodeVerification() {
        val user = User(name = "${String.random()}@example.lt")
        val defaultCode = quark.defaultVerificationCode

        val codeVerificationRobot = chooseExternalEmailRobot
            .email(user.name)
            .next()
            .setAndConfirmPassword<CodeVerificationRobot>(user.password)
            .setCode(defaultCode)

        if (paymentProvidersForSignup().isNotEmpty()) {
            codeVerificationRobot.verifyCode<SelectPlanRobot>()
                .toggleExpandPlan(Dev)
                .verify { canSelectPlan(Dev) }
        } else {
            codeVerificationRobot.verifyCode<SignupFinishedRobot>().verify {
                signupFinishedDisplayed()
            }
        }
    }
}
