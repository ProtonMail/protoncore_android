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

package me.proton.core.test.android.uitests.tests.medium.auth.login

import me.proton.android.core.coreexample.R
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.uitests.tests.SmokeTest
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.ChooseUsernameRobot
import me.proton.core.test.android.robots.auth.login.LoginRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class LoginTests : BaseTest() {
    private val loginRobot = LoginRobot()
    private val user: User = users.getUser { it.isPaid }

    @Before
    fun signIn() {
        AddAccountRobot()
            .signIn()
            .verify { loginElementsDisplayed() }
    }

    @Test
    fun missingPasswordAndUsername() {
        loginRobot
            .signIn<LoginRobot>()
            .verify { inputErrorDisplayed(R.string.auth_login_assistive_text) }
    }

    @Test
    @SmokeTest
    fun incorrectPassword() {
        loginRobot
            .username(user.name)
            .password("Incorrect")
            .signIn<LoginRobot>()
            .verify {
                // Error message itself is coming from the server; we cannot reliably check the contents,
                // given we support multiple languages.
                errorSnackbarDisplayed("")

                passwordInputIsEmpty()
                passwordInputHasError()
                usernameInputHasError()
            }
    }

    @Test
    @SmokeTest
    fun loginOnePassword() {
        loginRobot
            .loginUser<CoreexampleRobot>(user)
            .verify { userStateIs(user, Ready, Authenticated) }
    }

    @Test
    fun closeLogin() {
        loginRobot
            .close<AddAccountRobot>()
            .verify { addAccountElementsDisplayed() }
    }

    @Test
    @Ignore("Missing setup address robot")
    fun createAddressForExternalUser() {
        val external = users.getUser { it.name.isNotEmpty() }
        loginRobot
            .loginUser<ChooseUsernameRobot>(external)
            .verify { chooseUsernameElementsDisplayed() }

//        ChooseUsernameRobot()
//            .username(external.firstName)
//            .next()
//            .createAddress<CoreexampleRobot>()
//            .verify { userStateIs(external, Ready, Authenticated) }
    }
}
