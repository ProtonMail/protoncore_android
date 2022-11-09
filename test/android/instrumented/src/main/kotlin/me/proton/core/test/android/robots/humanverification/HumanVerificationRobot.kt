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

import me.proton.core.humanverification.presentation.utils.HumanVerificationVersion
import me.proton.core.test.android.robots.CoreRobot

/**
 * Base class for human verification actions and verifications.
 * @see HumanVerificationRobot
 */
abstract class HVRobot : CoreRobot() {
    abstract fun help(): HVRobot

    /** Selects 'captcha' human verification option. */
    abstract fun captcha(): HVCaptchaRobot

    /** Selects 'email' human verification option. */
    abstract fun email(): HVEmailRobot

    /** Selects 'sms' human verification option. */
    abstract fun sms(): HVSmsRobot

    abstract fun verify(block: Verify.() -> Unit)

    interface Verify {
        fun hvElementsDisplayed()
    }
}

class HumanVerificationRobot : HVRobot() {
    private val delegate: HVRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3Robot()
    }

    override fun help(): HVRobot = delegate.help()
    override fun captcha(): HVCaptchaRobot = delegate.captcha()
    override fun email(): HVEmailRobot = delegate.email()
    override fun sms(): HVSmsRobot = delegate.sms()
    override fun verify(block: Verify.() -> Unit) = delegate.verify(block)

    companion object {
        var version: HumanVerificationVersion = HumanVerificationVersion.HV3
    }
}

interface HVCaptchaRobot {
    /**
     * Checks "I am human" checkbox. Only works with development hCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    fun <T> iAmHuman(next: Class<T>): T

    fun verify(block: Verify.() -> Unit)

    interface Verify {
        fun captchaDisplayed()
    }
}

interface HVEmailRobot {
    /** Sets the value of email input to [email]. */
    fun setEmail(email: String): HVEmailRobot

    /** Clicks 'get verification code' button. */
    fun getVerificationCode(): HVCodeRobot
}

interface HVSmsRobot {
    /** Sets the value of phone number input to [number]. */
    fun setPhone(number: String?): HVSmsRobot

    /** Clicks country code list button. */
    fun countryCodeList(): HVSmsCountryRobot

    /** Clicks 'get verification code' button. */
    fun getVerificationCode(): HVCodeRobot
}

interface HVSmsCountryRobot {
    fun search(text: String): HVSmsCountryRobot
    fun selectCountry(country: String): HVSmsRobot
    fun <T> close(next: Class<T>): T
}

interface HVCodeRobot {
    fun setCode(code: String): HVCodeRobot
    fun <T> verifyCode(next: Class<T>): T
}
