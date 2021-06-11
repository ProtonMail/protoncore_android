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

import android.widget.EditText
import me.proton.core.humanverification.R
import me.proton.core.test.android.robots.other.CountryRobot

/**
 * [CodeVerificationRobot] class is an extension of [HumanVerificationRobot] contains human verification
 * code processing actions and verifications functionality.
 */
class CodeVerificationRobot : HumanVerificationRobot() {

    /**
     * Sets the value of verification code input to [code]
     * @return [CodeVerificationRobot]
     */
    fun setCode(code: String): CodeVerificationRobot = setText(R.id.verificationCodeEditText, code)

    /**
     * Sets the value of phone number input to [number]
     * @return [CodeVerificationRobot]
     */
    fun setPhone(number: String?): CodeVerificationRobot = setText(R.id.smsEditText, number!!)

    /**
     * Sets the value of email input to [email]
     * @return [CodeVerificationRobot]
     */
    fun setEmail(email: String): CodeVerificationRobot = setText(R.id.emailEditText, email)

    /**
     * Clicks 'get verification code' button
     * @return [CodeVerificationRobot]
     */
    fun getVerificationCode(): CodeVerificationRobot = clickElement(R.id.getVerificationCodeButton)

    /**
     * Clicks 'i already have a code' button
     * @return [CodeVerificationRobot]
     */
    fun alreadyHaveCode(): CodeVerificationRobot = clickElement(R.id.proceedButton)

    /**
     * Clicks country code list button
     * @return [CountryRobot]
     */
    fun countryCodeList(): CountryRobot = clickElement(R.id.callingCodeText, EditText::class.java)


    /**
     * Clicks 'verify' button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> verifyCode(): T = clickElement(R.id.verifyButton)
}
