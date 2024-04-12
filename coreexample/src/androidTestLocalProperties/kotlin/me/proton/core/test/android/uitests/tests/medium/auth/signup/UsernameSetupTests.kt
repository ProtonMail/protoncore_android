/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.signup.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.util.kotlin.random
import org.junit.Before
import org.junit.Test

class UsernameSetupTests : BaseTest() {
    private val chooseUsernameRobot = ChooseUsernameRobot()

    @Before
    fun navigateToSignup() {
        CoreRobot()
            .device
            .clickBackBtn()

        CoreexampleRobot()
            .signupUsername()
            .verify {
                chooseUsernameElementsDisplayed()
            }
    }

    @Test
    fun navigateToPasswordSetup() {
        chooseUsernameRobot
            .username(String.random())
            .next()
            .verify { passwordSetupElementsDisplayed() }
    }

    @Test
    fun usernameAlreadyExists() {
        val existentUsername = users.getUser().name
        chooseUsernameRobot
            .username(existentUsername)
            .next()
            .verify { errorSnackbarDisplayed("Username already used") }
    }

    @Test
    fun recoveryMethod() {
        val recoveryMethodsRobot = chooseUsernameRobot
            .username(String.random())
            .next()
            .password("12345678")
            .confirmPassword("12345678")
            .next<RecoveryMethodsRobot>()

        recoveryMethodsRobot.verify {
            onlyEmailRecoveryDisplayed()
            skipMenuButtonNotDisplayed()
        }

        recoveryMethodsRobot
            .next<RecoveryMethodsRobot>()
            .verify { recoveryDestinationErrorSnackbarDisplayed() }
    }
}
