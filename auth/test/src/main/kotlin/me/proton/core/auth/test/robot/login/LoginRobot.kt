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

package me.proton.core.auth.test.robot.login

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view

/** Corresponds to [me.proton.core.auth.presentation.ui.LoginActivity]. */
public object LoginRobot {
    private val usernameInput = view.withCustomMatcher(inputFieldMatcher(R.id.usernameInput))
    private val passwordInput = view.withCustomMatcher(inputFieldMatcher(R.id.passwordInput))
    private val signInButton = view.withId(R.id.signInButton)

    public fun fillUsername(username: String): LoginRobot = apply {
        usernameInput.typeText(username)
    }

    public fun fillPassword(password: String): LoginRobot = apply {
        passwordInput.typeText(password)
    }

    public fun login() {
        signInButton.click()
    }
}
