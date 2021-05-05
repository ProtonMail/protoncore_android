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
package me.proton.core.test.android.instrumented.robots.humanverification

import android.widget.EditText
import android.widget.TextView
import me.proton.core.humanverification.R

/**
 * [CodeVerificationRobot] class contains actions and human verification via code functionality.
 */
open class CodeVerificationRobot : HumanVerificationRobot() {

    fun search(text: String): CodeVerificationRobot = setText(R.id.search_src_text, text)
    fun setCode(code: String): CodeVerificationRobot = setText(R.id.verificationCodeEditText, code)
    fun setPhone(number: String?): CodeVerificationRobot = setText(R.id.smsEditText, number!!)
    fun setEmail(email: String): CodeVerificationRobot = setText(R.id.emailEditText, email)
    fun getVerificationCode(): CodeVerificationRobot = clickElement(R.id.getVerificationCodeButton)
    fun alreadyHaveCode(): CodeVerificationRobot = clickElement(R.id.proceedButton)
    fun closeList(): CodeVerificationRobot = clickElement(R.id.closeButton)
    fun countryCodeList(): CodeVerificationRobot = clickElement(R.id.callingCodeText, EditText::class.java)
    fun selectCountry(country: String?): CodeVerificationRobot = clickElement(country!!, TextView::class.java)
    inline fun <reified T> verifyCode(): T = clickElement(R.id.verifyButton)
}
