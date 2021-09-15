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

package me.proton.core.test.android.robots.settings

import android.widget.Button
import android.widget.EditText
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.usersettings.R

class RecoveryEmailRobot : CoreRobot() {

    /**
     * Replaces new email input with [email]
     * @return [RecoveryEmailRobot]
     */
    fun newEmail(email: String): RecoveryEmailRobot = replaceText(R.id.newEmailInput, email)

    /**
     * Replaces confirm email input with [email]
     * @return [RecoveryEmailRobot]
     */
    fun confirmNewEmail(email: String): RecoveryEmailRobot = replaceText(R.id.confirmNewEmailInput, email)

    /**
     * Clicks 'Save' button
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> save(): T = clickElement(R.id.saveButton)

    /**
     * [AuthenticationRobot] class contains password confirmation actions
     */
    class AuthenticationRobot : CoreRobot() {

        /**
         * Fills password field
         * @return [AuthenticationRobot]
         */
        fun password(password: String): AuthenticationRobot = replaceText(R.id.password, password)

        /**
         * Clicks "Cancel" button
         * @return [RecoveryEmailRobot]
         */
        fun cancel(): RecoveryEmailRobot = clickElement("Cancel")

        /**
         * Clicks "Enter" button
         * @return [RecoveryEmailRobot]
         */
        inline fun <reified T> enter(): T = clickElement("Enter")
    }


    class Verify : CoreVerify() {
        fun recoveryEmailElementsDisplayed() {
            view.withId(R.id.newEmailInput).instanceOf(EditText::class.java).wait().closeKeyboard()
            view.withId(R.id.confirmNewEmailInput).instanceOf(EditText::class.java).wait()
            view.withId(R.id.currentEmailInput).instanceOf(EditText::class.java).wait()
            view.withId(R.id.saveButton).instanceOf(Button::class.java).wait()
        }

        fun currentRecoveryEmailIs(email: String) {
            view.withId(R.id.currentEmailInput).withText(email).wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
