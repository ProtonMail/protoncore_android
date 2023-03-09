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

package me.proton.core.test.android.uitests.tests.medium.usersettings

import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.R
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.settings.PasswordManagementRobot
import me.proton.core.test.android.uitests.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import me.proton.core.util.kotlin.random
import org.junit.Test

class PasswordManagementTests : BaseTest() {

    private val passwordManagementRobot = PasswordManagementRobot()

    companion object {
        val freeUser = quark.userCreate().first
        val paidUser = quark.seedNewSubscriber()
    }

    private fun navigateToPasswordManagement(user: User) {
        quark.jailUnban()
        login(user)

        CoreexampleRobot()
            .settingsPasswordManagement()
            .verify { passwordManagementElementsDisplayed() }
    }

    @Test
    fun passwordMismatch() {
        navigateToPasswordManagement(freeUser)

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(freeUser.password, String.random(), String.random())
            .verify { inputErrorDisplayed(R.string.auth_signup_error_passwords_do_not_match) }
    }

    @Test
    fun incorrectPassword() {
        val password = String.random()
        navigateToPasswordManagement(freeUser)

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(String.random(), password, password)
            .verify { errorSnackbarDisplayed("Incorrect login credentials. Please try again") }
    }

    @Test
    fun incompletePassword() {
        val password = String.random(length = 3)
        navigateToPasswordManagement(freeUser)

        passwordManagementRobot
            .save<PasswordManagementRobot>()
            .verify { inputErrorDisplayed(R.string.auth_signup_validation_password) }

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(String.random(), password, String.random())
            .verify { inputErrorDisplayed(R.string.auth_signup_validation_password_length) }
    }

    @Test
    @SmokeTest
    fun updatePasswordFreeUser() {
        navigateToPasswordManagement(freeUser)

        passwordManagementRobot
            .changePassword<CoreexampleRobot>(freeUser.password, freeUser.password, freeUser.password)
            .verify {
                accountSwitcherDisplayed()
                userStateIs(freeUser, AccountState.Ready, SessionState.Authenticated)
            }
    }

    @Test
    @SmokeTest
    fun updatePasswordPaidUser() {
        navigateToPasswordManagement(paidUser)

        passwordManagementRobot
            .changePassword<CoreexampleRobot>(paidUser.password, paidUser.password, paidUser.password)
            .verify {
                accountSwitcherDisplayed()
                userStateIs(paidUser, AccountState.Ready, SessionState.Authenticated)
            }
    }

    @Test
    fun backFromPasswordManagement() {
        navigateToPasswordManagement(freeUser)

        passwordManagementRobot
            .close<CoreexampleRobot>()
            .verify { accountSwitcherDisplayed() }
    }
}
