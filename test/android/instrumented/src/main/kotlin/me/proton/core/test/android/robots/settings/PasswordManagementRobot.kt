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

class PasswordManagementRobot : CoreRobot() {

    /**
     * Replaces the value of password input with [password]
     * @return [PasswordManagementRobot]
     */
    fun currentPassword(password: String): PasswordManagementRobot =
        replaceText(R.id.currentLoginPasswordInput, password)

    /**
     * Replaces the value of password input with [password]
     * @return [PasswordManagementRobot]
     */
    fun newPassword(password: String): PasswordManagementRobot = replaceText(R.id.newLoginPasswordInput, password)

    /**
     * Replaces the value of password input with [password]
     * @return [PasswordManagementRobot]
     */
    fun confirmNewPassword(password: String): PasswordManagementRobot =
        replaceText(R.id.confirmNewLoginPasswordInput, password)

    /**
     * Clicks 'Save' button
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> save(): T = clickElement(R.id.saveLoginPasswordButton)

    /**
     * Sets current, new and confirm password values. Clicks 'Save' button
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> changePassword(current: String, new: String, confirm: String = new): T =
        currentPassword(current)
            .newPassword(new)
            .confirmNewPassword(confirm)
            .save()

    class Verify : CoreVerify() {
        fun passwordManagementElementsDisplayed() {
            view.withId(R.id.newLoginPasswordInput).instanceOf(EditText::class.java).wait()
            view.withId(R.id.confirmNewLoginPasswordInput).instanceOf(EditText::class.java).wait()
            view.withId(R.id.currentLoginPasswordInput).instanceOf(EditText::class.java).wait()
            view.withId(R.id.saveLoginPasswordButton).instanceOf(Button::class.java).wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
