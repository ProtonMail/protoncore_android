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
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.util.kotlin.random
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@RunWith(Parameterized::class)
public abstract class BaseConvertExternalToInternalAccountTests(
    @Suppress("unused") public val friendlyName: String,
    @Suppress("unused") public val testUserData: TestUserData,
    public val onLogin: () -> Any
) {
    public abstract val protonRule: ProtonRule

    public abstract fun loggedIn(username: String)

    private val seededData: TestUserData get() = protonRule.testDataRule?.testUserData ?: error("No User data was seeded")

    @Before
    public fun setUp() {
        FusionConfig.Compose.waitTimeout.set(60.seconds)
        FusionConfig.Espresso.waitTimeout.set(60.seconds)
    }

    @Test
    public fun convert() {
        AddAccountRobot
            .clickSignIn()
            .fillUsername(seededData.externalEmail)
            .fillPassword(seededData.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                screenIsDisplayed()
                domainInputDisplayed()
                usernameInputIsFilled(seededData.name)
                continueButtonIsEnabled()
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()
            .also {
                onLogin()
            }
            .apply {
                loggedIn(seededData.name)
            }
    }

    public companion object {
        public val username: String = String.random()

        private val withKeys = arrayOf(
            "With keys",
            TestUserData(
                name = username,
                external = true,
                externalEmail = "$username@example.lt"
            ),
            {}
        )

        private val noKeys = arrayOf(
            "No Keys",
            TestUserData(
                name = username,
                external = true,
                genKeys = TestUserData.GenKeys.None,
                externalEmail = "$username@example.lt"
            ),
            {}
        )

        private val twoPass = arrayOf(
            "With Mailbox Password",
            TestUserData(
                name = username,
                external = true,
                mailboxPassword = "password",
                externalEmail = "$username@example.lt"
            ),
            {
                TwoPassRobot
                    .apply {
                        screenIsDisplayed()
                    }
                    .fillMailboxPassword("password")
                    .unlock()
            }
        )

        @get:Parameterized.Parameters(name = "{0}")
        @get:JvmStatic
        @Suppress("unused")
        public val data: Collection<Array<*>> = listOf(withKeys, noKeys, twoPass)
    }
}
