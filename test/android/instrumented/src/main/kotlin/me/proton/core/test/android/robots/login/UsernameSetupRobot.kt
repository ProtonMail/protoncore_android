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

package me.proton.core.test.android.robots.login

import android.widget.EditText
import me.proton.core.test.android.instrumented.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [UsernameSetupRobot] class contains username setup actions and verifications implementation.
 * The view is displayed automatically when a user without username (eg. external email account) logs in
 */
class UsernameSetupRobot : CoreRobot() {

    /**
     * Sets the value of username input to [name]
     * @return [UsernameSetupRobot]
     */
    fun username(name: String): UsernameSetupRobot = setText(R.id.usernameInput, name)

    /**
     * Clicks 'next' button
     * @return [UsernameSetupRobot]
     */
    fun next(): UsernameSetupRobot = clickElement(R.id.nextButton)

    /**
     * Clicks 'create address' button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> createAddress(): T = clickElement(R.id.createAddressButton)

    class Verify : CoreVerify() {
        fun usernameSetupElementsDisplayed() {
            view.withId(R.id.nextButton).wait()
            view.withId(R.id.usernameInput).instanceOf(EditText::class.java).wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
