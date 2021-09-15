/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.test.android.robots.auth.login

import android.widget.Button
import android.widget.EditText
import me.proton.core.auth.R
import me.proton.core.test.android.plugins.data.User
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [LoginRobot] class contains login actions and verifications implementation.
 */
class LoginRobot : CoreRobot() {
    /**
     * Sets the value of username input to [name]
     * @return [LoginRobot]
     */
    fun username(name: String?): LoginRobot = setText(R.id.usernameInput, name!!)

    /**
     * Sets the value of password input to [password]
     * @return [LoginRobot]
     */
    fun password(password: String): LoginRobot = setText(R.id.passwordInput, password)

    /**
     * Clicks help button
     * @return [HelpRobot]
     */
    fun needHelp(): HelpRobot = clickElement(R.id.helpButton)

    /**
     * Clicks sign in button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> signIn(): T = clickElement(R.id.signInButton)

    /**
     * Fills in username and password of a given [user]. Clicks sign in button.
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> loginUser(user: User): T {
        return username(user.name)
            .password(user.password)
            .signIn()
    }

    class Verify : CoreVerify() {
        fun loginElementsDisplayed() {
            view.withId(R.id.usernameInput).instanceOf(EditText::class.java).closeKeyboard()
            view.withId(R.id.passwordInput).instanceOf(EditText::class.java).checkDisplayed()
            view.withId(R.id.signInButton).instanceOf(Button::class.java).checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
