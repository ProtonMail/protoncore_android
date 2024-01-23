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
import me.proton.test.fusion.Fusion.intent
import org.junit.Rule
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

public interface MinimalSignInGuestTests {
    @get:Rule
    public val intentsRule: IntentsRule
        get() = IntentsRule()

    public fun verifyAfter()

    @BeforeTest
    public fun setUp() {
        intent.stubExternalIntents()
    }

    @Test
    @Ignore("Not yet implemented.")
    public fun signInWithGuestAccount() {
        CredentialLessWelcomeRobot.clickContinueAsGuest()
        verifyAfter()
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
