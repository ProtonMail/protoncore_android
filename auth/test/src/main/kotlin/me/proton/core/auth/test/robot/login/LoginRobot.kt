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

package me.proton.core.auth.test.robot.login

import me.proton.core.test.quark.data.User

public object LoginRobot {

    public fun login(
        username: String,
        password: String,
        isLoginTwoStepEnabled: Boolean = false
    ): LoginRobot = apply {
        when (isLoginTwoStepEnabled) {
            true -> LoginTwoStepRobot
                .fillUsername(username)
                .clickContinue()
                .fillPassword(password)
                .clickContinue()

            false -> LoginLegacyRobot
                .fillUsername(username)
                .fillPassword(password)
                .login()
        }
    }

    public fun login(
        user: User,
        isLoginTwoStepEnabled: Boolean = false
    ): LoginRobot = apply {
        when (isLoginTwoStepEnabled) {
            true -> LoginTwoStepRobot.login(user)
            false -> LoginLegacyRobot.login(user)
        }
    }

    public fun help(
        isLoginTwoStepEnabled: Boolean = false
    ): LoginHelpRobot = LoginHelpRobot.apply {
        when (isLoginTwoStepEnabled) {
            true -> LoginTwoStepRobot.clickHelp()
            false -> LoginLegacyRobot.help()
        }
    }
}
