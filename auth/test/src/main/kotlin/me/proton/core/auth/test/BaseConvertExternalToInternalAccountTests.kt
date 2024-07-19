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

import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.login.TwoPassRobot
import me.proton.core.auth.test.robot.signup.ChooseInternalAddressRobot
import me.proton.core.test.quark.data.Plan
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.test.rule.annotation.payments.TestSubscriptionData
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

public abstract class BaseConvertExternalToInternalAccountTests {

    public abstract val protonRule: ProtonRule
    public abstract fun loggedIn(username: String)

    private val testUser: TestUserData
        get() = protonRule.testDataRule.mainTestUser ?: error("No User data was seeded")

    @Before
    public fun setUp() {
        FusionConfig.Compose.waitTimeout.set(90.seconds)
        FusionConfig.Espresso.waitTimeout.set(90.seconds)
    }

    @Test
    @PrepareUser(userData = TestUserData(isExternal = true))
    public fun convertWithKeys() {
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.externalEmail)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
                domainInputDisplayed()
                usernameInputIsFilled(testUser.name)
                continueButtonIsEnabled()
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()
            .apply {
                loggedIn(testUser.name)
            }
    }

    @Test
    @PrepareUser(userData = TestUserData(isExternal = true, genKeys = TestUserData.GenKeys.None))
    public fun convertNoKeys() {
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.externalEmail)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
                domainInputDisplayed()
                usernameInputIsFilled(testUser.name)
                continueButtonIsEnabled()
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()
            .apply {
                loggedIn(testUser.name)
            }
    }

    @Test
    @PrepareUser(
        userData = TestUserData(isExternal = true, passphrase = "password"),
        subscriptionData = TestSubscriptionData(plan = Plan.MailPlus)
    )
    public fun convertWithMailboxPassword() {
        AddAccountRobot
            .clickSignIn()
            .fillUsername(testUser.externalEmail)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
                domainInputDisplayed()
                usernameInputIsFilled(testUser.name)
                continueButtonIsEnabled()
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()
            .also {
                TwoPassRobot
                    .apply {
                        screenIsDisplayed()
                    }
                    .fillMailboxPassword("password")
                    .unlock()
            }
            .apply {
                loggedIn(testUser.name)
            }
    }
}
