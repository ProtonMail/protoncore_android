/*
 * Copyright (c) 2024 Proton AG
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
import me.proton.core.compose.component.PROTON_OUTLINED_TEXT_INPUT_TAG
import me.proton.core.test.quark.data.User
import me.proton.test.fusion.Fusion.node

/** Corresponds to [me.proton.core.auth.presentation.ui.LoginTwoStepActivity]. */
public object LoginTwoStepRobot {
    private val usernameInput = node.withTag(PROTON_OUTLINED_TEXT_INPUT_TAG)
    private val passwordInput = node.withTag(PROTON_OUTLINED_TEXT_INPUT_TAG)
    private val helpButton = node.withText(R.string.auth_login_help)
    private val continueButton = node.withText(R.string.auth_login_continue)

    public fun fillUsername(username: String): LoginTwoStepRobot = apply {
        usernameInput.typeText(username)
    }

    public fun fillPassword(password: String): LoginTwoStepRobot = apply {
        passwordInput.typeText(password)
    }

    public fun clickHelp() {
        helpButton.click()
    }

    public fun clickContinue(): LoginTwoStepRobot = apply {
        continueButton.click()
    }

    public fun login(user: User) {
        fillUsername(user.name)
        clickContinue()
        fillPassword(user.password)
        clickContinue()
    }
}
