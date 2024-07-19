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

package me.proton.core.usersettings.test

import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.annotation.PrepareUser
import me.proton.core.test.rule.annotation.TestUserData
import me.proton.core.usersettings.test.flow.PasswordManagementFlow
import me.proton.core.usersettings.test.flow.UpdateRecoveryEmailFlow
import me.proton.core.util.kotlin.random
import org.junit.Test

/**
 * Note: requires [me.proton.test.fusion.FusionConfig.Compose.testRule] to be initialized.
 */
@HiltAndroidTest
public interface MinimalUserSettingsTest {

    public val protonRule: ProtonRule

    private val testUser: TestUserData
        get() = protonRule.testDataRule.mainTestUser ?: error("No User data was seeded")

    public fun startPasswordManagement()
    public fun startRecoveryEmail()

    @Test
    @PrepareUser
    public fun changePasswordSuccess() {
        val new = "new${String.random()}"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(testUser.name, testUser.password)

        startPasswordManagement()

        PasswordManagementFlow
            .changeLoginPassword(current = testUser.password, new = new, confirm = new)
            .successPasswordUpdatedIsDisplayed()
    }

    @Test
    @PrepareUser
    public fun changePasswordErrorDoNotMatch() {
        val new = "new${String.random()}"
        val other = "other${String.random()}"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(testUser.name, testUser.password)

        startPasswordManagement()

        PasswordManagementFlow
            .changeLoginPassword(current = testUser.password, new = new, confirm = other)
            .errorPasswordDoNotMatchIsDisplayed()
    }

    @Test
    @PrepareUser
    public fun changeRecoveryEmailSuccess() {
        val new = "new.${testUser.name}@email.com"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(testUser.name, testUser.password)

        startRecoveryEmail()

        UpdateRecoveryEmailFlow
            .changeRecoveryEmail(new = new, confirm = new, password = testUser.password)
            .successEmailUpdatedIsDisplayed()
    }

    @Test
    @PrepareUser
    public fun changeRecoveryEmailErrorDoNotMatch() {
        val new = "new.${testUser.name}@email.com"
        val other = "other.${testUser.name}@email.com"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(testUser.name, testUser.password)

        startRecoveryEmail()

        UpdateRecoveryEmailFlow
            .changeRecoveryEmail(new = new, confirm = other, password = null)
            .errorEmailDoNotMatchIsDisplayed()
    }

    @Test
    @PrepareUser
    public fun startAndCancelAccountRecovery() {
        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(testUser.name, testUser.password)

        startPasswordManagement()

        PasswordManagementFlow.startRecovery()
        PasswordManagementFlow.cancelRecovery(password = testUser.password)
    }
}
