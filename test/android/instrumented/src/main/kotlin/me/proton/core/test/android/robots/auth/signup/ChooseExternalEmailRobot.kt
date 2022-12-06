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
 * [ChooseExternalEmailRobot] class contains username setup actions and verifications implementation.
 * The view is displayed automatically when a user without username (eg. external email account) logs in
 */
class ChooseExternalEmailRobot : CoreRobot() {

    /**
     * Sets the value of username input to [name]
     * @return [ChooseExternalEmailRobot]
     */
    fun username(name: String): ChooseExternalEmailRobot = addText(R.id.usernameInput, name)

    /**
     * Clicks 'next' button
     * @return [ChooseExternalEmailRobot]
     */
    fun next(): PasswordSetupRobot = clickElement(R.id.nextButton)

    /**
     * Sets the username and clicks 'next'
     * @return [ChooseExternalEmailRobot]
     */
    fun setUsername(name: String): PasswordSetupRobot = username(name).next()

    /**
     * Switches signup type between external and internal
     * @return [ChooseInternalEmailRobot]
     */
    fun switchSignupType(): ChooseInternalEmailRobot = clickElement(R.id.switchButton)

    class Verify : CoreVerify() {

        fun chooseExternalEmailElementsDisplayed() {
            view.withId(R.id.emailInput).closeKeyboard().checkDisplayed()
            view.withId(R.id.nextButton).checkDisplayed()
        }

        fun accountTypeSwitchNotDisplayed() =
            view.withId(R.id.switchButton).checkNotDisplayed()

        fun accountTypeSwitchDisplayed() =
            view.withId(R.id.switchButton).checkDisplayed()

        fun externalAccountTextsDisplayedCorrectly() {
            view.withId(R.id.footnoteText).withText(R.string.auth_signup_external_footnote).checkDisplayed()
        }

        fun switchToSecureDisplayed() =
            view.withText(R.string.auth_signup_create_secure_proton_address).checkDisplayed()
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
