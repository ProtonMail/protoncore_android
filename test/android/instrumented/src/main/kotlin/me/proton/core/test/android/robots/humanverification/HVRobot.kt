/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.test.android.robots.humanverification

import me.proton.core.humanverification.presentation.utils.HumanVerificationVersion

var DEFAULT_HV_VERSION = HumanVerificationVersion.HV3

class HVRobot(version: HumanVerificationVersion) : BaseHVRobot() {
    private val delegate: BaseHVRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3Robot()
    }

    constructor() : this(DEFAULT_HV_VERSION)

    override fun help(): BaseHVRobot = delegate.help()

    /** Selects 'captcha' human verification option. */
    override fun captcha(): BaseHVCaptchaRobot = delegate.captcha()

    /** Selects 'email' human verification option. */
    override fun email(): BaseHVEmailRobot = delegate.email()

    /** Selects 'sms' human verification option. */
    override fun sms(): BaseHVSmsRobot = delegate.sms()

    override fun verify(block: Verify.() -> Unit): Unit = delegate.verify(block)
}

class HVCaptchaRobot(version: HumanVerificationVersion) : BaseHVCaptchaRobot() {
    private val delegate: BaseHVCaptchaRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3CaptchaRobot()
    }

    constructor() : this(DEFAULT_HV_VERSION)

    /**
     * Checks "I am human" checkbox. Only works with development hCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    override fun <T> iAmHuman(next: Class<T>): T = delegate.iAmHuman(next)

    override fun verify(block: Verify.() -> Unit): Unit = delegate.verify(block)
}

class HVEmailRobot(version: HumanVerificationVersion) : BaseHVEmailRobot() {
    private val delegate: BaseHVEmailRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3EmailRobot()
    }

    constructor() : this(DEFAULT_HV_VERSION)

    /** Sets the value of email input to [email]. */
    override fun setEmail(email: String): BaseHVEmailRobot = delegate.setEmail(email)

    /** Clicks 'get verification code' button. */
    override fun getVerificationCode(): BaseHVCodeRobot = delegate.getVerificationCode()
}

abstract class HVSmsRobot(version: HumanVerificationVersion) : BaseHVSmsRobot() {
    private val delegate: BaseHVSmsRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3SmsRobot()
    }

    constructor() : this(DEFAULT_HV_VERSION)

    /** Sets the value of phone number input to [number]. */
    override fun setPhone(number: String?): BaseHVSmsRobot = delegate.setPhone(number)

    /** Clicks country code list button. */
    override fun countryCodeList(): BaseHVSmsCountryRobot = delegate.countryCodeList()

    /** Clicks 'get verification code' button. */
    override fun getVerificationCode(): BaseHVCodeRobot = delegate.getVerificationCode()
}

abstract class HVSmsCountryRobot(version: HumanVerificationVersion) : BaseHVSmsCountryRobot() {
    private val delegate: BaseHVSmsCountryRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3SmsCountryRobot()
    }

    constructor() : this(DEFAULT_HV_VERSION)

    override fun dismiss(): BaseHVSmsRobot = delegate.dismiss()
    override fun search(text: String): BaseHVSmsCountryRobot = delegate.search(text)
    override fun selectCountry(country: String): BaseHVSmsRobot = delegate.selectCountry(country)
}

abstract class HVCodeRobot(version: HumanVerificationVersion) : BaseHVCodeRobot() {
    private val delegate: BaseHVCodeRobot = when (version) {
        HumanVerificationVersion.HV3 -> HV3CodeRobot()
    }

    constructor() : this(DEFAULT_HV_VERSION)

    override fun setCode(code: String): BaseHVCodeRobot = delegate.setCode(code)
    override fun <T> verifyCode(next: Class<T>): T = delegate.verifyCode(next)
}
