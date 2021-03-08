/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.test.android.uitests.tests.login

import me.proton.core.test.android.instrumented.data.User
import me.proton.core.test.android.instrumented.robots.login.LoginRobot
import me.proton.core.test.android.instrumented.robots.login.TwoFaRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class TwoFaTests : BaseTest() {

    private val loginRobot = LoginRobot()
    private val twoFaRobot = TwoFaRobot()
    private val invalidCode = "123456"

    @Before
    fun goToTwoFa() {
        jailUnban()
        val user = User.getUser { it.twoFa.isNotEmpty() }
        loginRobot
            .loginUser<TwoFaRobot>(user)
    }

    @Test
    fun invalidTwoFaCode() {
        twoFaRobot
            .setSecondFactorInput(invalidCode)
            .authenticate<LoginRobot>()
            .verify { errorSnackbarDisplayed("Incorrect login credentials. Please try again") }
    }

    @Test
    fun invalidVerificationCode() {
        twoFaRobot
            .switchTwoFactorMode()
            .setSecondFactorInput(invalidCode)
            .authenticate<LoginRobot>()
            .verify { errorSnackbarDisplayed("Incorrect login credentials. Please try again") }
    }
}
