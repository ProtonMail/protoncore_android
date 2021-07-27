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

import me.proton.android.core.coreexample.BuildConfig
import me.proton.core.test.android.instrumented.utils.StringUtils.randomString
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot.RecoveryMethodType
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.plugins.data.Plan.Free
import org.junit.Before
import org.junit.Test

class RecoveryMethodsSetupTests : BaseTest() {

    private val recoveryMethodsRobot = RecoveryMethodsRobot()

    @Before
    fun goToPasswordSetup() {
        CoreRobot()
            .device
            .clickBackBtn()

        CoreexampleRobot()
            .signup()
            .verify { suffixDisplayed(BuildConfig.HOST) }

        ChooseUsernameRobot()
            .username(randomString())
            .next()
            .password("12345678")
            .confirmPassword("12345678")
            .next<RecoveryMethodsRobot>()
            .verify { recoveryMethodsElementsDisplayed() }
    }

    @Test
    fun emailAndPhoneValidation() {
        recoveryMethodsRobot
            .recoveryMethod(RecoveryMethodType.EMAIL)
            .email("notEmail")
            .next<RecoveryMethodsRobot>()
            .verify { errorSnackbarDisplayed("Email address failed validation") }

        recoveryMethodsRobot
            .recoveryMethod(RecoveryMethodType.PHONE)
            .phone("11")
            .next<RecoveryMethodsRobot>()
            .verify { errorSnackbarDisplayed("Phone number failed validation") }
    }

    @Test
    fun skipRecoveryMethods() {
        recoveryMethodsRobot
            .skip()
            .skipConfirm()
            .verify {
                planDetailsDisplayed(Free)
                canSelectPlan(Free)
            }
    }

    @Test
    fun emptyFieldsTriggerSkip() {
        recoveryMethodsRobot
            .next<RecoveryMethodsRobot.SkipRecoveryRobot>()
            .skipConfirm()
            .verify {
                planDetailsDisplayed(Free)
                canSelectPlan(Free)
            }
    }
}
