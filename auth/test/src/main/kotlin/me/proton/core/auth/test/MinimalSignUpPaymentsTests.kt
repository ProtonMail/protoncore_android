/*
 * Copyright (c) 2022 Proton Technologies AG
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

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.signup.SetPasswordRobot
import me.proton.core.auth.test.robot.signup.SignupInternal
import me.proton.core.humanverification.test.robot.HvCodeRobot
import me.proton.core.plan.test.robot.SubscriptionRobot
import me.proton.core.util.kotlin.random
import me.proton.test.fusion.FusionConfig
import org.junit.Before
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

/**
 * Minimal SignUp Tests for app providing [AccountType.External].
 */
public interface MinimalSignUpPaymentsTests {

    @Before
    public fun goToExternalSignup() {
        FusionConfig.Compose.waitTimeout.set(90.seconds)
        FusionConfig.Compose.assertTimeout.set(90.seconds)
        FusionConfig.Espresso.waitTimeout.set(90.seconds)
        FusionConfig.Espresso.assertTimeout.set(90.seconds)
        AddAccountRobot.clickSignUp()
    }

    @Test
    public fun signupExternalAccountHappyPath() {
        val testEmail = "${String.random()}@example.com"

        SignupInternal
            .apply {
                robotDisplayed()
            }
            .fillEmail(testEmail)
            .clickNext()
            .fillCode()
            .clickVerify()

        SetPasswordRobot
            .fillAndClickNext(String.random(12))

        SubscriptionRobot
            .selectFreePlan()

        HvCodeRobot
            .apply {
                waitForWebView()
            }
    }

    @Test
    public fun signupSwitchToInternalAccountCreationHappyPath() {
        val testUsername = "test-${String.random(15)}"

        SignupInternal
            .clickSwitch()
            .apply {
                robotDisplayed()
            }
            .fillUsername(testUsername)
            .clickNext()
            .apply {
                uiElementsDisplayed()
            }
            .fillAndClickNext(String.random(12))
            .skip()
            .skipConfirm()

        SubscriptionRobot
            .selectFreePlan()

        HvCodeRobot
            .apply {
                waitForWebView()
            }
    }
}
