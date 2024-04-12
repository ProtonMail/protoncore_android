/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.test

import androidx.test.espresso.intent.rule.IntentsRule
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.login.LoginHelpRobot
import me.proton.core.auth.test.robot.login.LoginRobot
import me.proton.core.auth.test.robot.login.TwoPassRobot
import me.proton.core.auth.test.robot.signup.ChooseInternalAddressRobot
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import org.junit.Rule
import org.junit.Test

/**
 * Minimal SignIn Tests for app providing [AccountType.Internal].
 */
public interface MinimalSignInInternalTests {
    @get:Rule
    public val intentsRule: IntentsRule
        get() = IntentsRule()

    public val quark: Quark
    public val users: User.Users

    public fun verifyAfter()

    @Test
    public fun signInInternalHappyPath() {
        val user = users.getUser { it.name == "pro" }

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)
        verifyAfter()
    }

    @Test
    public fun signInInternalTwoPassHappyPath() {
        val user = users.getUser(usernameAndOnePass = false) { it.name == "twopasswords" }

        AddAccountRobot
            .clickSignIn()
            .fillUsername(user.name)
            .fillPassword(user.password)
            .login()

        TwoPassRobot
            .fillMailboxPassword(user.passphrase)
            .unlock()

        verifyAfter()
    }

    @Test
    public fun signInExternalHappyPath() {
        val username = User.randomUsername()
        val user = User(
            name = "",
            email = "$username@external-domain.test",
            isExternal = true
        )
        quark.userCreate(user, Quark.CreateAddress.WithKey())

        AddAccountRobot
            .clickSignIn()
            .fillUsername(user.email)
            .fillPassword(user.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
                domainInputDisplayed()
                usernameInputIsFilled(username)
                continueButtonIsEnabled()
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()

        verifyAfter()
    }

    @Test
    public fun opensHelp() {
        AddAccountRobot.clickSignIn()
        LoginRobot.help()

        LoginHelpRobot
            .forgotUsername()
            .forgotUsernameBrowserOpened()

        LoginHelpRobot
            .forgotPassword()
            .forgotPasswordBrowserOpened()

        LoginHelpRobot
            .otherIssues()
            .otherIssuesBrowserOpened()
    }
}
