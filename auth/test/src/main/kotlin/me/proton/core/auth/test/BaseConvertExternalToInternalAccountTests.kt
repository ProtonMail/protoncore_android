/*
 * Copyright (c) 2023 Proton AG.
 * This file is part of Proton Drive.
 *
 * Proton Drive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Drive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Drive.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.auth.test

import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.signup.ChooseInternalAddressRobot
import me.proton.core.auth.test.robot.login.TwoPassRobot
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import me.proton.core.util.kotlin.random
import kotlin.test.BeforeTest
import kotlin.test.Test

public interface BaseConvertExternalToInternalAccountTests {
    public val quark: Quark
    public var testUser: User
    public var testUsername: String

    public fun verifyLoggedOut()
    public fun verifySuccessfulLogin()

    @BeforeTest
    public fun prepare() {
        quark.jailUnban()
    }

    @Test
    public fun happyPath() {
        // GIVEN
        testUsername = User.randomUsername()
        testUser = User(
            name = "",
            email = "${testUsername}@external-domain.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot.clickSignIn()
        SignInFlow.signInExternal(testUser.email, testUser.password, testUsername)

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun externalAccountWithAddressButNoAddressKey() {
        // GIVEN
        testUsername = User.randomUsername()
        testUser = User(
            name = "",
            email = "${testUsername}@external-domain.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.NoKey)

        // WHEN
        AddAccountRobot.clickSignIn()
        SignInFlow.signInExternal(testUser.email, testUser.password, testUsername)

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun accountWithTwoPasswordMode() {
        // GIVEN
        val passphrase = "passphrase"
        testUsername = User.randomUsername()
        testUser = User(
            name = "",
            email = "${testUsername}@external-domain.test",
            isExternal = true,
            passphrase = passphrase
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot.clickSignIn()
        SignInFlow.signInExternal(testUser.email, testUser.password, testUsername)

        TwoPassRobot
            .fillMailboxPassword(passphrase)
            .unlock()

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun accountWithUnavailableUsername() {
        // GIVEN
        val domain = String.random()
        testUsername = "proton_core_$domain"
        testUser = User(
            name = "",
            email = "free@${domain}.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
                domainInputDisplayed()
                usernameInputIsEmpty()
            }
            .fillUsername(testUsername)
            .next()

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun chooseInternalAddressIsClosed() {
        // GIVEN
        val domain = String.random()
        testUsername = "proton_core_$domain"
        testUser = User(
            name = "",
            email = "free@${domain}.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
            }
            .cancel()

        // THEN
        verifyLoggedOut()
    }
}
