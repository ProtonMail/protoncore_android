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

package me.proton.core.auth.test.signup

import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.auth.signup.RecoveryMethodsRobot
import me.proton.core.test.android.robots.humanverification.HVRobot
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import me.proton.core.util.kotlin.random
import kotlin.test.BeforeTest
import kotlin.test.Test

public interface BaseUsernameAccountSignupTests {
    public val quark: Quark
    public val vpnUsers: User.Users

    public fun verifySuccessfulSignup()

    @BeforeTest
    public fun prepare() {
        quark.jailUnban()
    }

    @Test
    public fun happyPath() {
        val username = User.randomUsername()
        val user = User(name = username, recoveryEmail = "$username@${String.random()}.test")

        AddAccountRobot()
            .createAccount()
            .chooseUsername()
            .setUsername(user.name)
            .setAndConfirmPassword<RecoveryMethodsRobot>(user.password)
            .email(user.recoveryEmail)
            .next<HVRobot>()
            .captcha()
            .iAmHuman(CoreRobot::class.java)

        verifySuccessfulSignup()
    }
}
