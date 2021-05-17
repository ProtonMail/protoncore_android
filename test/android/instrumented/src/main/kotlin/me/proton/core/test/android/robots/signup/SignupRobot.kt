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

package me.proton.core.test.android.robots.signup

import android.widget.EditText
import android.widget.TextView
import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [SignupRobot] class contains signup (both external and internal) actions and verifications functionality
 */
class SignupRobot : CoreRobot() {

    /**
     * Sets the value of username input to [name]
     * @return [SignupRobot]
     */
    fun username(name: String): SignupRobot = setText(R.id.usernameInput, name)

    /**
     * Switches signup type to internal
     * @return [SignupRobot]
     */
    fun signupSecure(): SignupRobot = clickElement(R.string.auth_signup_create_secure_proton_address)

    /**
     * Switches signup type to external
     * @return [SignupRobot]
     */
    fun signupCurrent(): SignupRobot = clickElement(R.string.auth_signup_current_email)

    /**
     * Clicks 'next' button
     * @return [PasswordSetupRobot]
     */
    fun next(): PasswordSetupRobot = clickElement(R.id.nextButton)

    class Verify : CoreVerify() {
        fun usernameSetupElementsDisplayed() {
            view.withId(R.id.nextButton).wait().checkDisplayed()
            view.withId(R.id.usernameInput).instanceOf(EditText::class.java).wait().checkDisplayed()
        }

        fun externalSignupElementsDisplayed() {
            view.withText(R.string.auth_signup_create_secure_proton_address).wait()
        }

        fun signupElementsDisplayed(domain: String) {
            view.withText(R.string.auth_signup_current_email).wait()
            view.withText(domain).instanceOf(TextView::class.java).wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
