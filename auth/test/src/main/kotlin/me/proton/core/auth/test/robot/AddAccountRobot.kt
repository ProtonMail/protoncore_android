/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.auth.test.robot

import me.proton.core.auth.presentation.R
import me.proton.core.auth.test.robot.login.LoginRobot
import me.proton.core.auth.test.robot.signup.SignUpRobot
import me.proton.test.fusion.Fusion.view
import me.proton.test.fusion.FusionConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Corresponds to [me.proton.core.auth.presentation.ui.AddAccountActivity]. */
public object AddAccountRobot {
    private val signInButton = view.withId(R.id.sign_in)
    private val signUpButton = view.withId(R.id.sign_up)

    public fun clickSignIn(): LoginRobot {
        signInButton.await(30.seconds) { checkIsDisplayed() }
        signInButton.click()
        return LoginRobot
    }

    public fun clickSignUp(): SignUpRobot {
        signUpButton.await(30.seconds) { checkIsDisplayed() }
        signUpButton.click()
        return SignUpRobot
    }

    public fun uiElementsDisplayed(
        timeout: Duration = FusionConfig.Espresso.waitTimeout.get()
    ): AddAccountRobot {
        signInButton.await(timeout = timeout) {
            signInButton.checkIsDisplayed()
            signUpButton.checkIsDisplayed()
        }
        return AddAccountRobot
    }
}
