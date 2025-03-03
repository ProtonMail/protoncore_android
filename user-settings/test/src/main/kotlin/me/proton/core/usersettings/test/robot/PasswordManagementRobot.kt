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

import me.proton.core.accountrecovery.test.robot.AccountRecoveryGracePeriodRobot
import me.proton.core.accountrecovery.test.robot.PasswordResetDialogRobot
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.core.test.android.withToastAwait
import me.proton.core.usersettings.presentation.R
import me.proton.test.fusion.Fusion
import kotlin.time.Duration.Companion.seconds


public object PasswordManagementRobot {

    private val currentLoginPassInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.currentLoginPasswordInput))
    private val newLoginPasswordInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.newLoginPasswordInput))
    private val confirmLoginPassInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.confirmNewLoginPasswordInput))

    private val currentMailboxPassInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.currentMailboxPasswordInput))
    private val newMailboxPasswordInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.newMailboxPasswordInput))
    private val confirmMailboxPassInput =
        Fusion.view.withCustomMatcher(inputFieldMatcher(R.id.confirmNewMailboxPasswordInput))

    private val saveLoginButton = Fusion.view.withId(R.id.saveLoginPasswordButton)
    private val saveMailboxButton = Fusion.view.withId(R.id.saveMailboxPasswordButton)
    private val dontKnowPasswordButton = Fusion.view.withId(R.id.dontKnowYourCurrentPassword)

    private val passwordResetRequestedInfo = Fusion.node.withText(R.string.account_recovery_info_grace_title)

    public fun fillLoginPassword(
        current: String,
        new: String,
        confirm: String = new
    ): PasswordManagementRobot = apply {
        currentLoginPassInput.typeText(current)
        newLoginPasswordInput.typeText(new)
        confirmLoginPassInput.typeText(confirm)
    }

    public fun fillMailboxPassword(
        current: String,
        new: String,
        confirm: String = new
    ): PasswordManagementRobot = apply {
        currentMailboxPassInput.typeText(current)
        newMailboxPasswordInput.typeText(new)
        confirmMailboxPassInput.typeText(confirm)
    }

    public fun saveLoginPassword(): PasswordManagementRobot = apply {
        saveLoginButton.click()
    }

    public fun saveMailboxPassword(): PasswordManagementRobot = apply {
        saveMailboxButton.click()
    }

    public fun clickDontKnowPasswordButton(): PasswordResetDialogRobot {
        dontKnowPasswordButton.click()
        return PasswordResetDialogRobot
    }

    public fun awaitPasswordResetRequestedInfoDisplayed(): PasswordManagementRobot {
        passwordResetRequestedInfo.await { assertIsDisplayed() }
        return PasswordManagementRobot
    }

    public fun clickPasswordResetRequestedInfo(): AccountRecoveryGracePeriodRobot {
        passwordResetRequestedInfo.click()
        return AccountRecoveryGracePeriodRobot
    }

    public fun errorPasswordDoNotMatchIsDisplayed(): PasswordManagementRobot = apply {
        Fusion.view
            .withText(R.string.auth_signup_error_passwords_do_not_match)
            .checkIsDisplayed()
    }

    public fun successPasswordUpdatedIsDisplayed(): PasswordManagementRobot = apply {
        Fusion.view
            .withToastAwait(R.string.settings_password_management_success, timeout = 20.seconds) {
                checkIsDisplayed()
            }
    }
}
