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

package me.proton.core.test.android.robots.confirmpassword

import me.proton.core.auth.presentation.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [ConfirmPasswordRobot] base class contains confirm password actions implementation.
 */
open class ConfirmPasswordRobot : CoreRobot() {

    /**
     * Clicks 'enter' button
     */
    inline fun <reified T> enter(): T = clickElement(R.id.enterButton)

    /**
     * Clicks 'cancel' button
     */
    inline fun <reified T> cancel(): T = clickElement(R.id.cancelButton)

    /**
     * Sets the value for password input to [password]
     */
    fun setPassword(password: String): ConfirmPasswordRobot = addText(R.id.password, password)

    /**
     * Sets the value for second factor input to [secondFactor]
     */
    fun setSecondFactor(secondFactor: String): ConfirmPasswordRobot = addText(R.id.twoFA, secondFactor)

    class Verify : CoreVerify() {
        fun confirmPasswordElementsDisplayed() {
            view.withId(R.id.password).checkDisplayed()
            view.withId(R.id.enterButton).checkDisplayed()
            view.withId(R.id.cancelButton).checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
