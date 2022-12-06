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
 * [ChooseInternalEmailRobot] class contains internal setup actions and verifications implementation.
 */
class ChooseInternalEmailRobot : CoreRobot() {

    /**
     * Sets the value of username input to [name]
     * @return [ChooseInternalEmailRobot]
     */
    fun username(name: String): ChooseInternalEmailRobot = addText(R.id.usernameInput, name)

    /**
     * Clicks 'next' button
     * @return [ChooseInternalEmailRobot]
     */
    fun next(): PasswordSetupRobot = clickElement(R.id.nextButton)

    /**
     * Sets the username and clicks 'next'
     * @return [ChooseInternalEmailRobot]
     */
    fun setUsername(name: String): PasswordSetupRobot = username(name).next()

    /**
     * Switches signup type between external and internal
     * @return [ChooseExternalEmailRobot]
     */
    fun switchSignupType(): ChooseExternalEmailRobot = clickElement(R.id.switchButton)

    class Verify : CoreVerify() {
        fun accountTypeSwitchNotDisplayed() =
            view.withId(R.id.switchButton).checkNotDisplayed()

        fun accountTypeSwitchDisplayed() =
            view.withId(R.id.switchButton).checkDisplayed()

        fun internalAccountTextsDisplayedCorrectly() {
            view.withId(R.id.subtitleText).withText(R.string.auth_one_account_all_services).checkDisplayed()
            view.withId(R.id.footnoteText).withText(R.string.auth_signup_internal_footnote).checkDisplayed()
        }

        fun chooseInternalEmailElementsDisplayed() {
            view.withId(R.id.usernameInput).closeKeyboard().checkDisplayed()
            view.withId(R.id.nextButton).checkDisplayed()
        }

        fun switchToSecureDisplayed() =
            view.withText(R.string.auth_signup_create_secure_proton_address).checkDisplayed()

        fun switchToExternalDisplayed() =
            view.withText(R.string.auth_signup_current_email).checkDisplayed()

        fun suffixDisplayed(suffix: String) =
            view.withAncestor(view.withId(R.id.usernameInput))
                .withId(com.google.android.material.R.id.textinput_suffix_text)
                .withText("@$suffix").checkDisplayed()

        fun suffixNotDisplayed() =
            view.withAncestor(view.withId(R.id.usernameInput))
                .withId(com.google.android.material.R.id.textinput_suffix_text)
                .checkNotDisplayed()

        fun domainInputDisplayed() =
            view.withId(R.id.domainInput).checkDisplayed()

        fun domainInputNotDisplayed() =
            view.withId(R.id.domainInput).checkNotDisplayed()
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
