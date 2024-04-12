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

package me.proton.core.test.android.uitests.tests.medium.usersettings

import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.auth.R
import me.proton.core.test.quark.data.User
import me.proton.core.test.android.robots.settings.PasswordManagementRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import me.proton.core.util.kotlin.random
import org.junit.Test

class PasswordManagementTests : BaseTest() {

    private val passwordManagementRobot = PasswordManagementRobot()

    companion object {
        val freeUser = quark.userCreate().first
    }

    private fun prepareUser(user: User) {
        quark.jailUnban()
        login(user)
    }

    private fun navigateToPasswordManagement() {
        CoreexampleRobot()
            .settingsPasswordManagement()
            .verify { passwordManagementElementsDisplayed() }
    }

    private fun prepareTest(user: User) {
        prepareUser(user)
        navigateToPasswordManagement()
    }

    @Test
    fun passwordMismatch() {
        prepareTest(freeUser)

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(freeUser.password, String.random(), String.random())
            .verify { inputErrorDisplayed(R.string.auth_signup_error_passwords_do_not_match) }
    }

    @Test
    fun incorrectPassword() {
        val password = String.random()
        prepareTest(freeUser)

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(String.random(), password, password)
            .verify { errorSnackbarDisplayed("Incorrect login credentials. Please try again") }
    }

    @Test
    fun incompletePassword() {
        val password = String.random(length = 3)
        prepareTest(freeUser)

        passwordManagementRobot
            .save<PasswordManagementRobot>()
            .verify { inputErrorDisplayed(R.string.auth_signup_validation_password) }

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(String.random(), password, String.random())
            .verify { inputErrorDisplayed(R.string.auth_signup_validation_password_length) }
    }

    @Test
    @SmokeTest
    fun updatePassword() {
        prepareTest(freeUser)

        passwordManagementRobot
            .changePassword<CoreexampleRobot>(freeUser.password, freeUser.password, freeUser.password)
            .verify {
                accountSwitcherDisplayed()
                userStateIs(freeUser, AccountState.Ready, SessionState.Authenticated)
            }
    }

    @Test
    @SmokeTest
    fun passwordTooCommon() {
        prepareUser(freeUser)

        CoreexampleRobot()
            .featureFlags()
            .apply { verifyFlagsFetched() }
            .back<CoreexampleRobot>()

        navigateToPasswordManagement()

        passwordManagementRobot
            .changePassword<PasswordManagementRobot>(freeUser.password, "password", "password")
            .verify { inputErrorDisplayed(R.string.auth_signup_password_not_allowed) }
    }
}
