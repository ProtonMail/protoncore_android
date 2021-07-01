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

package me.proton.core.test.android.robots.auth

import android.widget.EditText
import android.widget.TextView
import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.auth.signup.PasswordSetupRobot

/**
 * [ChooseUsernameRobot] class contains username setup actions and verifications implementation.
 * The view is displayed automatically when a user without username (eg. external email account) logs in
 */
class ChooseUsernameRobot : CoreRobot() {

    /**
     * Sets the value of username input to [name]
     * @return [ChooseUsernameRobot]
     */
    fun username(name: String): ChooseUsernameRobot = setText(R.id.usernameInput, name)

    /**
     * Clicks 'next' button
     * @return [ChooseUsernameRobot]
     */
    fun next(): PasswordSetupRobot = clickElement(R.id.nextButton)

    /**
     * Switches signup type between external and internal
     * @return [ChooseUsernameRobot]
     */
    fun switchSignupType(): ChooseUsernameRobot = clickElement(R.id.useCurrentEmailButton)

    class Verify : CoreVerify() {
        fun chooseUsernameElementsDisplayed() {
            view.withId(R.id.usernameInput).instanceOf(EditText::class.java).wait().closeKeyboard()
            view.withId(R.id.nextButton).wait()
        }

        fun switchToSecureDisplayed() =
            view.withText(R.string.auth_signup_create_secure_proton_address).wait()

        fun switchToExternalDisplayed() =
            view.withText(R.string.auth_signup_create_account).wait()

        fun suffixDisplayed(suffix: String) =
            view.withText("@$suffix").instanceOf(TextView::class.java).wait()
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
