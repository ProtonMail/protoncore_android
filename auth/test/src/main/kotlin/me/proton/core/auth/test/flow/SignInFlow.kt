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

import me.proton.core.auth.test.robot.login.LoginRobot
import me.proton.core.auth.test.robot.login.TwoPassRobot
import me.proton.core.auth.test.robot.signup.ChooseInternalAddressRobot

public object SignInFlow {

    /**
     * Internal Account simple happy path:
     * - Enter email/password.
     */
    public fun signInInternal(username: String, password: String) {
        LoginRobot
            .fillUsername(username)
            .fillPassword(password)
            .login()
    }

    /**
     * Internal Account TwoPass happy path:
     * - Enter email/password.
     * - Enter TwoPass.
     */
    public fun signInInternalTwoPass(username: String, password: String, mailboxPassword: String) {
        LoginRobot
            .fillUsername(username)
            .fillPassword(password)
            .login()
        TwoPassRobot
            .fillMailboxPassword(mailboxPassword)
            .unlock()
    }

    /**
     * External Account Conversion happy path:
     * - Enter email/password.
     * - Start internal address conversion flow.
     * - Autofill username.
     * - Select primary domain.
     */
    public fun signInExternal(email: String, password: String, username: String) {
        LoginRobot
            .fillUsername(email)
            .fillPassword(password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                domainInputDisplayed()
                usernameInputIsFilled(username)
                continueButtonIsEnabled()
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()
    }
}
