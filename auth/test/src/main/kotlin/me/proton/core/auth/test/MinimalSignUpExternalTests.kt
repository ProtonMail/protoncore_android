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

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.flow.SignUpFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.util.kotlin.random
import kotlin.test.Test

/**
 * Minimal Tests for signing up with an external account.
 *
 * Only for apps that provide [AccountType.External].
 */
public interface MinimalSignUpExternalTests {

    @Test
    public fun signupExternalAccountHappyPath() {
        val testEmail = String.Companion.random(10, ('a'..'z').toList()) + "@example.com"

        AddAccountRobot.clickSignUp()
        SignUpFlow.signUpExternalEmail(testEmail)
    }
}
