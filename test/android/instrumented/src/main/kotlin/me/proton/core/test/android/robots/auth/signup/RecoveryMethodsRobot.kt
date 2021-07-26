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

import android.widget.TextView
import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.humanverification.HumanVerificationRobot
import me.proton.core.test.android.robots.other.CountryRobot
import me.proton.core.test.android.robots.plans.SelectPlanRobot

/**
 * [RecoveryMethodsRobot] class contains recovery methods actions and verifications functionality
 */
class RecoveryMethodsRobot : CoreRobot() {

    /**
     * [SkipRecoveryRobot] class contains recovery methods skip actions
     */
    class SkipRecoveryRobot : CoreRobot() {

        /**
         * Clicks "Set recovery method" button
         * @return [RecoveryMethodsRobot]
         */
        fun setRecoveryMethod(): RecoveryMethodsRobot = clickElement(R.string.auth_signup_set_recovery)

        /**
         * Clicks "Skip" button
         * @return [HumanVerificationRobot]
         */
        fun skipConfirm(): SelectPlanRobot = clickElement("Skip")
    }

    enum class RecoveryMethodType { EMAIL, PHONE }

    /**
     * Clicks an element with given recovery method [type]
     * @return [RecoveryMethodsRobot]
     */
    fun recoveryMethod(type: RecoveryMethodType): RecoveryMethodsRobot = clickElement(type.name, TextView::class.java)

    /**
     * Sets email input value to given [email]
     * @return [RecoveryMethodsRobot]
     */
    fun email(email: String): RecoveryMethodsRobot = setText(R.id.emailEditText, email)

    /**
     * Sets phone number input value to given [phoneNo]
     * @return [RecoveryMethodsRobot]
     */
    fun phone(phoneNo: String): RecoveryMethodsRobot = setText(R.id.smsEditText, phoneNo)

    /**
     * Clicks country code element
     * @return [CountryRobot]
     */
    fun phoneCode(): CountryRobot = clickElement(R.id.callingCodeText)

    /**
     * Clicks 'next' button
     * @return [HumanVerificationRobot]
     */
    inline fun <reified T> next(): T {
        view
            .withId(R.id.nextButton)
            .hasSibling(
                view.withId(R.id.recoveryOptions)
            ).click()
        return T::class.java.newInstance()
    }

    /**
     * Clicks 'Skip' button.
     * @return [SkipRecoveryRobot]
     */
    fun skip(): SkipRecoveryRobot = clickElement(R.id.recovery_menu_skip, TextView::class.java)

    class Verify : CoreVerify() {
        fun recoveryMethodsElementsDisplayed() {
            view.withId(R.id.emailEditText).instanceOf(TextView::class.java).wait()
            view.withText(RecoveryMethodType.EMAIL.name).instanceOf(TextView::class.java).wait()
            view.withText(RecoveryMethodType.PHONE.name).instanceOf(TextView::class.java).wait()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
