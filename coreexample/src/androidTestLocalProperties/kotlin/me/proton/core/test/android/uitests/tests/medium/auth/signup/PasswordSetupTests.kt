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

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.signup.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.signup.PasswordSetupRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.util.kotlin.random
import org.junit.Before
import org.junit.Test

@HiltAndroidTest
class PasswordSetupTests : BaseTest() {

    private val passwordSetupRobot = PasswordSetupRobot()

    @Before
    fun goToPasswordSetup() {
        CoreRobot()
            .device
            .clickBackBtn()

        CoreexampleRobot()
            .signupInternal()
            .verify { domainInputDisplayed() }

        ChooseUsernameRobot()
            .username(String.random())
            .next()
            .verify {
                passwordSetupElementsDisplayed()
            }
    }

    @Test
    fun passwordMismatch() {
        passwordSetupRobot
            .password("123456789")
            .confirmPassword("1234567890")
            .next<PasswordSetupRobot>()
            .verify {
                recoveryMethodsElementsNotDisplayed()
                errorSnackbarDisplayed(R.string.auth_signup_error_passwords_do_not_match)
            }
    }

    @Test
    fun passwordLength() {
        passwordSetupRobot
            .password("12345")
            .next<PasswordSetupRobot>()
            .verify {
                passwordSetupElementsDisplayed()
                inputErrorDisplayed(R.string.auth_signup_validation_password_length)
            }
    }
}
