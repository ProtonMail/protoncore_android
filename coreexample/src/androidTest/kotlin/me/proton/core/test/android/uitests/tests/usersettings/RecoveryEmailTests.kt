/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.test.android.uitests.tests.usersettings

import me.proton.core.account.domain.entity.AccountState
import me.proton.core.account.domain.entity.SessionState
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.settings.RecoveryEmailRobot
import me.proton.core.test.android.robots.settings.RecoveryEmailRobot.AuthenticationRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.usersettings.R
import org.junit.Before
import org.junit.Test

class RecoveryEmailTests: BaseTest() {

    private val recoveryEmailRobot = RecoveryEmailRobot()
    private val user = users.getUser { it.recoveryEmail.isNotEmpty() }

    @Before
    fun navigateToPasswordManagement() {
        AddAccountRobot()
            .signIn()
            .loginUser<CoreexampleRobot>(user)
            .settingsRecoveryEmail()
            .verify {
                recoveryEmailElementsDisplayed()
                currentRecoveryEmailIs(user.recoveryEmail)
            }

    }

    @Test
    fun emailMismatch() {
        recoveryEmailRobot.newEmail(user.recoveryEmail)
        listOf("", "incorrect", "different@example.com").forEach {
            recoveryEmailRobot
                .confirmNewEmail(it)
                .save<RecoveryEmailRobot>()
                .verify { inputErrorDisplayed(R.string.settings_recovery_email_error_no_match) }
        }
    }

    @Test
    fun emptyAndInvalidNewEmail() {
        listOf("", "incorrect").forEach {
            recoveryEmailRobot
                .newEmail(it)
                .save<RecoveryEmailRobot>()
                .verify { inputErrorDisplayed(R.string.settings_validation_email) }
        }
    }

    @Test
    fun incorrectCredentials() {
        recoveryEmailRobot
            .newEmail(user.recoveryEmail)
            .confirmNewEmail(user.recoveryEmail)
            .save<AuthenticationRobot>()
            .password("incorrect")
            .enter<RecoveryEmailRobot>()
            .verify { errorSnackbarDisplayed("Incorrect login credentials. Please try again") }
    }

    @Test
    fun cancelAuthentication() {
        recoveryEmailRobot
            .newEmail(user.recoveryEmail)
            .confirmNewEmail(user.recoveryEmail)
            .save<AuthenticationRobot>()
            .cancel()
            .verify {
                recoveryEmailElementsDisplayed()
                currentRecoveryEmailIs(user.recoveryEmail)
            }
    }

    @Test
    fun changeEmailToTheSame() {
        recoveryEmailRobot
            .newEmail(user.recoveryEmail)
            .confirmNewEmail(user.recoveryEmail)
            .save<AuthenticationRobot>()
            .password(user.password)
            .enter<CoreexampleRobot>()
            .verify {
                coreexampleElementsDisplayed()
                userStateIs(user, AccountState.Ready, SessionState.Authenticated)
            }
    }
}
