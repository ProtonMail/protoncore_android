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

import androidx.test.espresso.intent.rule.IntentsRule
import me.proton.core.auth.test.robot.CredentialLessWelcomeRobot
import me.proton.core.auth.test.robot.signup.RecoveryMethodRobot
import me.proton.core.auth.test.robot.signup.SetPasswordRobot
import me.proton.core.auth.test.robot.signup.SignUpRobot
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.humanverification.HVCodeRobot
import me.proton.core.test.android.robots.humanverification.HVRobot
import me.proton.core.test.quark.Quark
import me.proton.core.util.kotlin.random
import me.proton.test.fusion.Fusion.intent
import org.junit.Rule
import kotlin.test.BeforeTest
import kotlin.test.Test

public interface MinimalSignInGuestTests {
    @get:Rule
    public val intentsRule: IntentsRule
        get() = IntentsRule()

    public val quark: Quark

    public fun navigateToSignupFromCredentialLess()
    public fun verifyAfterCredentialLessSignup()
    public fun verifyAfterRegularSignup(username: String)

    @BeforeTest
    public fun setUp() {
        intent.stubExternalIntents()
        quark.jailUnban()
    }

    @Test
    public fun signInWithCredentialLessAccount() {
        CredentialLessWelcomeRobot.clickContinueAsGuest()
        verifyAfterCredentialLessSignup()
    }

    @Test
    public fun credentialLessToRegularAccount() {
        CredentialLessWelcomeRobot.clickContinueAsGuest()
        verifyAfterCredentialLessSignup()

        navigateToSignupFromCredentialLess()

        val testUsername = "test-${String.random()}"
        SignUpRobot.forExternal().clickSwitch()
        SignUpRobot
            .forInternal()
            .fillUsername(testUsername)
            .clickNext()
        SetPasswordRobot
            .fillAndClickNext("123123123")
        RecoveryMethodRobot
            .skip()
            .skipConfirm()
        HVRobot()
            .email()
            .setEmail("${testUsername}@proton.wtf")
            .getVerificationCode()
        HVCodeRobot()
            .setCode("666666")
            .verifyCode(CoreRobot::class.java)

        verifyAfterRegularSignup(testUsername)
    }

    @Test
    public fun opensVpnNoLogsLink() {
        CredentialLessWelcomeRobot.clickNoLogsLinkButton()
        CredentialLessWelcomeRobot.noLogsLinkBrowserOpened()
    }

    @Test
    public fun opensTermsAndConditionsLink() {
        CredentialLessWelcomeRobot.clickTermsAndConditionsLink()
        CredentialLessWelcomeRobot.termsAndConditionsOpened()
    }
}
