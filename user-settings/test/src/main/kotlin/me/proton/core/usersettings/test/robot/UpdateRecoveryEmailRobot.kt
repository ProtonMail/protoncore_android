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

package me.proton.core.usersettings.test.robot

import me.proton.core.auth.test.robot.dialog.PasswordAnd2FARobot
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.core.test.android.withToastAwait
import me.proton.core.usersettings.presentation.R
import me.proton.test.fusion.Fusion
import kotlin.time.Duration.Companion.seconds


public object UpdateRecoveryEmailRobot {

    private val newEmailInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.newEmailInput))
    private val confirmEmailInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.confirmNewEmailInput))

    private val saveButton = Fusion.view.withId(R.id.saveButton)

    public fun fillEmail(
        new: String,
        confirm: String = new
    ): UpdateRecoveryEmailRobot = apply {
        newEmailInput.typeText(new)
        confirmEmailInput.typeText(confirm)
    }

    public fun saveEmail(): PasswordAnd2FARobot {
        saveButton.click()
        return PasswordAnd2FARobot
    }

    public fun errorEmailDoNotMatchIsDisplayed(): UpdateRecoveryEmailRobot = apply {
        Fusion.view
            .withText(R.string.settings_recovery_email_error_no_match)
            .checkIsDisplayed()
    }

    public fun successEmailUpdatedIsDisplayed(): UpdateRecoveryEmailRobot = apply {
        Fusion.view
            .withToastAwait(R.string.settings_recovery_email_success, timeout = 20.seconds) {
                checkIsDisplayed()
            }
    }
}
