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

package me.proton.core.test.android.robots.auth.signup

import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [ChooseUsernameRobot] class contains username setup actions and verifications implementation.
 */
class ChooseUsernameRobot : CoreRobot() {

    /**
     * Sets the value of username input to [name]
     * @return [ChooseUsernameRobot]
     */
    fun username(name: String): ChooseUsernameRobot = addText(R.id.usernameInput, name)

    /**
     * Clicks 'next' button
     * @return [ChooseUsernameRobot]
     */
    fun next(): PasswordSetupRobot = clickElement(R.id.nextButton)

    /**
     * Sets the username and clicks 'next'
     * @return [ChooseUsernameRobot]
     */
    fun setUsername(name: String): PasswordSetupRobot = username(name).next()

    class Verify : CoreVerify() {

        fun chooseUsernameElementsDisplayed() {
            view.withId(R.id.usernameInput).closeKeyboard().checkDisplayed()
            view.withId(R.id.nextButton).checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
