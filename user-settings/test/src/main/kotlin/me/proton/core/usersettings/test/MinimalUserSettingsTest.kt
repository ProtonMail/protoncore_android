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

import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.test.quark.Quark
import me.proton.core.usersettings.test.flow.PasswordManagementFlow
import me.proton.core.usersettings.test.flow.UpdateRecoveryEmailFlow
import me.proton.core.util.kotlin.random
import org.junit.Test
import kotlin.test.BeforeTest

/**
 * Note: requires [me.proton.test.fusion.FusionConfig.Compose.testRule] to be initialized.
 */
public interface MinimalUserSettingsTest {

    public val quark: Quark

    public fun startPasswordManagement()
    public fun startRecoveryEmail()

    @BeforeTest
    public fun prepare() {
        quark.jailUnban()
    }

    @Test
    public fun changePasswordSuccess() {
        val (user, _) = quark.userCreate()
        val new = "new${String.random()}"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        startPasswordManagement()

        PasswordManagementFlow
            .changeLoginPassword(current = user.password, new = new, confirm = new)
            .successPasswordUpdatedIsDisplayed()
    }

    @Test
    public fun changePasswordErrorDoNotMatch() {
        val (user, _) = quark.userCreate()
        val new = "new${String.random()}"
        val other = "other${String.random()}"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        startPasswordManagement()

        PasswordManagementFlow
            .changeLoginPassword(current = user.password, new = new, confirm = other)
            .errorPasswordDoNotMatchIsDisplayed()
    }

    @Test
    public fun changeRecoveryEmailSuccess() {
        val (user, _) = quark.userCreate()
        val new = "mew.${String.random()}@domain.com"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        startRecoveryEmail()

        UpdateRecoveryEmailFlow
            .changeRecoveryEmail(new = new, confirm = new, password = user.password)
            .successEmailUpdatedIsDisplayed()
    }

    @Test
    public fun changeRecoveryEmailErrorDoNotMatch() {
        val (user, _) = quark.userCreate()
        val new = "new.${String.random()}@domain.com"
        val other = "other.${String.random()}@domain.com"

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        startRecoveryEmail()

        UpdateRecoveryEmailFlow
            .changeRecoveryEmail(new = new, confirm = other, password = null)
            .errorEmailDoNotMatchIsDisplayed()
    }

    @Test
    public fun startAndCancelAccountRecovery() {
        val (user, _) = quark.userCreate()

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        startPasswordManagement()

        PasswordManagementFlow.startRecovery()
        PasswordManagementFlow.cancelRecovery(password = user.password)
    }
}
