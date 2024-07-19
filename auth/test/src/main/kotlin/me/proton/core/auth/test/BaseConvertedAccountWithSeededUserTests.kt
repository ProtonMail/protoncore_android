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

package me.proton.core.auth.test

import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.signup.ChooseInternalAddressRobot
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.TestUserData.Companion.randomUsername
import org.junit.Test

public interface BaseConvertedAccountWithSeededUserTests {

    public fun loggedIn(username: String)

    public fun loggedOut()

    public val protonRule: ProtonRule

    private val testUser: TestUserData
        get() = protonRule.testDataRule?.mainTestUser ?: error("SeedUser data is not provided")

    @Test
    public fun accountWithUnavailableUsername() {
        val username = randomUsername()
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.externalEmail)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
                continueButtonIsEnabled()
                domainInputDisplayed()
                usernameInputIsEmpty()
            }
            .fillUsername(username)
            .next()
            .apply {
                loggedIn(username)
            }
    }

    @Test
    public fun chooseInternalAddressIsClosed() {
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.externalEmail)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
            }
            .cancel()
            .apply {
                loggedOut()
            }
    }
}