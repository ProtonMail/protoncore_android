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

package me.proton.core.auth.test.robot.login

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view

/** Corresponds to [me.proton.core.auth.presentation.ui.LoginSsoActivity]. */
public object LoginSsoRobot {
    private val emailInput = view.withCustomMatcher(inputFieldMatcher(R.id.emailInput))
    private val helpButton = view.withId(R.id.login_menu_help)
    private val signInButton = view.withId(R.id.signInButton)
    private val signInWithPasswordButton = view.withId(R.id.signInWithPasswordButton)

    public fun fillEmail(email: String): LoginSsoRobot = apply {
        emailInput.typeText(email)
    }

    public fun help() {
        helpButton.click()
    }

    public fun login() {
        signInButton.click()
    }

    public fun signInWithPassword() {
        signInWithPasswordButton.click()
    }
}
