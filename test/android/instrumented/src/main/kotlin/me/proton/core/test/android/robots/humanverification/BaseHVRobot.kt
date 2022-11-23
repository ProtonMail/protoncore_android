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

package me.proton.core.test.android.robots.humanverification

import me.proton.core.test.android.robots.CoreRobot

/**
 * Base class for human verification actions and verifications.
 */
abstract class BaseHVRobot : CoreRobot() {
    abstract fun help(): BaseHVRobot

    /** Selects 'captcha' human verification option. */
    abstract fun captcha(): BaseHVCaptchaRobot

    /** Selects 'email' human verification option. */
    abstract fun email(): BaseHVEmailRobot

    /** Selects 'sms' human verification option. */
    abstract fun sms(): BaseHVSmsRobot

    abstract fun verify(block: Verify.() -> Unit)

    interface Verify {
        fun hvElementsDisplayed()
    }
}

abstract class BaseHVCaptchaRobot : CoreRobot() {
    /**
     * Checks "I am human" checkbox. Only works with development hCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    abstract fun <T> iAmHuman(next: Class<T>): T

    abstract fun verify(block: Verify.() -> Unit)

    interface Verify {
        fun captchaDisplayed()
    }
}

abstract class BaseHVEmailRobot : CoreRobot() {
    /** Sets the value of email input to [email]. */
    abstract fun setEmail(email: String): BaseHVEmailRobot

    /** Clicks 'get verification code' button. */
    abstract fun getVerificationCode(): BaseHVCodeRobot
}

abstract class BaseHVSmsRobot : CoreRobot() {
    /** Sets the value of phone number input to [number]. */
    abstract fun setPhone(number: String?): BaseHVSmsRobot

    /** Clicks country code list button. */
    abstract fun countryCodeList(): BaseHVSmsCountryRobot

    /** Clicks 'get verification code' button. */
    abstract fun getVerificationCode(): BaseHVCodeRobot
}

abstract class BaseHVSmsCountryRobot : CoreRobot() {
    abstract fun dismiss(): BaseHVSmsRobot
    abstract fun search(text: String): BaseHVSmsCountryRobot
    abstract fun selectCountry(country: String): BaseHVSmsRobot
}

abstract class BaseHVCodeRobot : CoreRobot() {
    abstract fun setCode(code: String): BaseHVCodeRobot
    abstract fun <T> verifyCode(next: Class<T>): T

    abstract fun verify(block: Verify.() -> Unit)

    interface Verify {
        fun incorrectCode()
    }
}
