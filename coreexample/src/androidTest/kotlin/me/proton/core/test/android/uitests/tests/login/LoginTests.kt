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

import me.proton.android.core.coreexample.R
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.instrumented.data.User
import me.proton.core.test.android.instrumented.data.User.Users.getUser
import me.proton.core.test.android.instrumented.robots.login.LoginRobot
import me.proton.core.test.android.instrumented.robots.login.UsernameSetupRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class LoginTests : BaseTest() {

    private val loginRobot = LoginRobot()
    private val user: User = getUser()

    @Before
    fun unban() {
        jailUnban()
    }

    @Test
    fun logout() {
        loginRobot
            .loginUser<CoreexampleRobot>(user)
            .logoutUser<CoreexampleRobot>(user)
            .verify { userIsLoggedOut(user) }
    }

    @Test
    fun passwordValidation() {
        loginRobot
            .signIn<LoginRobot>()
            .verify { inputErrorDisplayed(R.string.auth_login_assistive_text) }

        loginRobot
            .username(user.name)
            .password("Incorrect")
            .signIn<LoginRobot>()
            .verify { errorSnackbarDisplayed("Incorrect login credentials. Please try again") }
    }

    @Test
    fun loginOnePassword() {
        loginRobot
            .loginUser<CoreexampleRobot>(user)
            .verify { userStateIs(user, Ready, Authenticated) }
    }

    @Test
    fun closeLogin() {
        loginRobot
            .close<CoreexampleRobot>()
            .verify { userIsLoggedOut(user) }
    }

    @Test
    @Ignore
    fun createAddressForExternalUser() {
        val external = getUser { it.name.isNotEmpty() }
        loginRobot
            .loginUser<UsernameSetupRobot>(external)
            .verify { usernameSetupElementsDisplayed() }

        UsernameSetupRobot()
            .createProtonMailAddress<CoreexampleRobot>(external.firstName)
            .verify { userStateIs(external, Ready, Authenticated) }
    }
}
