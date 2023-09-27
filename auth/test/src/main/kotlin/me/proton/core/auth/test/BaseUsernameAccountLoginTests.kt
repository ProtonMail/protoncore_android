/*
 * Copyright (c) 2022 Proton Technologies AG
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

import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import kotlin.test.BeforeTest
import kotlin.test.Test

public interface BaseUsernameAccountLoginTests {
    public val quark: Quark
    public val vpnUsers: User.Users

    public fun verifySuccessfulLogin()

    @BeforeTest
    public fun prepare() {
        quark.jailUnban()
    }

    @Test
    public fun loginWithFreeInternalAccount() {
        val user = quark.userCreate().first
        AddAccountRobot()
            .signIn()
            .loginUser<CoreRobot>(user)
        verifySuccessfulLogin()
    }

    @Test
    public fun loginWithVpnUsernameAccount() {
        val user = vpnUsers.getUser()
        AddAccountRobot()
            .signIn()
            .loginUser<CoreRobot>(user)
        verifySuccessfulLogin()
    }
}
