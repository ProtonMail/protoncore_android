/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.auth.test.flow

import me.proton.core.auth.test.robot.signup.SetPasswordRobot
import me.proton.core.auth.test.robot.signup.SignUpRobot

public object SignUpFlow {

    /**
     * External Email happy path:
     * - Enter email.
     * - Solve Human Verification.
     * - Set and confirm password.
     */
    public fun signUpExternalEmail(email: String) {
        SignUpRobot
            .forExternal()
            .fillEmail(email)
            .clickNext()
            .fillCode()
            .clickVerify()
        SetPasswordRobot
            .fillAndClickNext("123123123")
    }
}
