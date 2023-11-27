/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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
import me.proton.core.test.android.instrumented.utils.StringUtils.stringFromResource
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.humanverification.HVRobot
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
        fun setRecoveryMethod(): RecoveryMethodsRobot =
            clickElement(stringFromResource(R.string.auth_signup_set_recovery))

        /**
         * Clicks "Skip" button
         * The next step is usually [HVRobot] or [SelectPlanRobot].
         */
        inline fun <reified T> skipConfirm(): T =
            clickElement(stringFromResource(R.string.auth_signup_skip_recovery))
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
    fun email(email: String): RecoveryMethodsRobot = addText(R.id.email, email)

    /**
     * Sets phone number input value to given [phoneNo]
     * @return [RecoveryMethodsRobot]
     */
    fun phone(phoneNo: String): RecoveryMethodsRobot = addText(R.id.phone, phoneNo)

    /**
     * Clicks country code element
     * @return [CountryRobot]
     */
    fun phoneCode(): CountryRobot = clickElement(R.id.phone_country)

    /**
     * Clicks 'next' button
     * @return [HVRobot]
     */
    inline fun <reified T> next(): T {
        view
            .withId(R.id.next)
            .hasSibling(
                view.withId(R.id.recoveryOptions)
            ).click()
        return T::class.java.newInstance()
    }

    /**
     * Clicks 'Skip' button.
     * @return [SkipRecoveryRobot]
     */
    fun skip(): SkipRecoveryRobot = clickElement(R.id.skip, TextView::class.java)

    class Verify : CoreVerify() {
        fun recoveryMethodsElementsDisplayed() {
            view.withId(R.id.email).checkDisplayed()
            view.withText(RecoveryMethodType.EMAIL.name).checkDisplayed()
            view.withText(RecoveryMethodType.PHONE.name).checkDisplayed()
        }

        fun onlyEmailRecoveryDisplayed() {
            view.withId(R.id.email).checkDisplayed()
            view.withId(R.id.recoveryOptions).checkNotDisplayed()
        }

        fun recoveryDestinationErrorSnackbarDisplayed() {
            errorSnackbarDisplayed(R.string.auth_signup_error_validation_recovery_destination)
        }

        fun skipMenuButtonNotDisplayed() {
            view.withId(R.id.skip).checkDoesNotExist()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
