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

package me.proton.core.auth.test

import androidx.annotation.CallSuper
import me.proton.core.auth.domain.LocalAuthFlags
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.login.LoginRobot
import me.proton.core.test.quark.Quark
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Minimal Sign In Tests for apps supporting SSO.
 */
public interface MinimalSSOSignInTests {
    public val localAuthFlags: LocalAuthFlags
    public val quark: Quark?

    @BeforeTest
    @CallSuper
    public fun setUp() {
        localAuthFlags.ssoEnabled = true
        quark?.jailUnban()
    }

    @Test
    public fun ssoSignInHappyPath() {
        AddAccountRobot.clickSignIn()
        LoginRobot.signInWithSSO()
        // TODO proceed with the test
    }
}
