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

package me.proton.core.auth.test.robot.dialog

import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion
import me.proton.core.auth.presentation.R as AuthR
import me.proton.core.presentation.R as PresentationR


public object PasswordAnd2FARobot {

    private val passwordInput = Fusion.view.withCustomMatcher(inputFieldMatcher(AuthR.id.password))
    private val twoFAInput = Fusion.view.withCustomMatcher(inputFieldMatcher(AuthR.id.twoFA))

    private val enterButton = Fusion.view.withText(PresentationR.string.presentation_alert_enter)
    private val cancelButton = Fusion.view.withText(PresentationR.string.presentation_alert_cancel)

    public fun fillPassword(
        password: String
    ): PasswordAnd2FARobot = apply {
        passwordInput.typeText(password)
    }

    public fun fillTwoFA(
        code: String
    ): PasswordAnd2FARobot = apply {
        twoFAInput.typeText(code)
    }

    public fun clickEnter(): PasswordAnd2FARobot = apply {
        enterButton.click()
    }

    public fun clickCancel(): PasswordAnd2FARobot = apply {
        cancelButton.click()
    }
}
