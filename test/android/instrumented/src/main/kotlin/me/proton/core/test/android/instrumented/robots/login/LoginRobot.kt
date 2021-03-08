/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.test.android.instrumented.robots.login

import me.proton.core.auth.R
import me.proton.core.test.android.instrumented.data.User
import me.proton.core.test.android.instrumented.robots.BaseRobot
import me.proton.core.test.android.instrumented.robots.BaseVerify

/**
 * [LoginRobot] class contains actions and verifications for login functionality.
 */
open class LoginRobot : BaseRobot() {

    fun username(name: String?): LoginRobot = setText(R.id.usernameInput, name!!)
    fun password(password: String): LoginRobot = setText(R.id.passwordInput, password)
    fun needHelp(): HelpRobot = clickElement(R.id.helpButton)
    inline fun <reified T> signIn(): T = clickElement(R.id.signInButton)

    inline fun <reified T> loginUser(user: User): T {
        return username(user.name)
            .password(user.password)
            .signIn()
    }

    class Verify : BaseVerify()

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
