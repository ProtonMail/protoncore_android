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

import android.webkit.WebView
import android.widget.EditText
import android.widget.TextView
import me.proton.core.humanverification.R
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.TokenType.CAPTCHA
import me.proton.core.humanverification.domain.entity.TokenType.EMAIL
import me.proton.core.humanverification.domain.entity.TokenType.SMS
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.other.CountryRobot
import java.util.Locale

/**
 * [HumanVerificationRobot] base class contains human verification actions and verifications implementation.
 */
open class HumanVerificationRobot : CoreRobot() {

    /**
     * Clicks 'help' button
     * @return [HumanVerificationRobot]
     */
    fun help(): HumanVerificationRobot = clickElement(R.id.helpButton)

    /**
     * Selects 'email' human verification option
     * @return [CodeVerificationRobot]
     */
    fun email(): HumanVerificationRobot = hvOption(EMAIL)

    /**
     * Selects 'sms' human verification option
     * @return [CodeVerificationRobot]
     */
    fun sms(): HumanVerificationRobot = hvOption(SMS)

    /**
     * Sets the value of phone number input to [number]
     * @return [CodeVerificationRobot]
     */
    fun setPhone(number: String?): HumanVerificationRobot = setText(R.id.smsEditText, number!!)

    /**
     * Clicks country code list button
     * @return [CountryRobot]
     */
    fun countryCodeList(): CountryRobot = clickElement(R.id.callingCodeText, EditText::class.java)

    /**
     * Sets the value of email input to [email]
     * @return [CodeVerificationRobot]
     */
    fun setEmail(email: String): HumanVerificationRobot = setText(R.id.emailEditText, email)

    /**
     * Clicks 'get verification code' button
     * @return [CodeVerificationRobot]
     */
    fun getVerificationCode(): CodeVerificationRobot = clickElement(R.id.getVerificationCodeButton)

    /**
     * Selects 'captcha' human verification option
     * @return [HumanVerificationRobot]
     */
    fun captcha(): HumanVerificationRobot = hvOption(CAPTCHA)

    /**
     * Checks "I'm not a robot" checkbox. Only works with development reCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> imNotARobot(): T = clickElement(R.id.captchaWebView, WebView::class.java)

    /**
     * Checks "I am human" checkbox. Only works with development hCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> iAmHuman(): T {
        Thread.sleep(2000) // Special case
        view.instanceOf(WebView::class.java).wait().click()
        return T::class.java.newInstance()
    }

    /**
     * Clicks close button
     * @param T next Robot to be returned
     */
    inline fun <reified T> close(): T = clickElement(R.id.closeButton)

    /**
     * Clicks text view with [option] text
     * @return [CodeVerificationRobot]
     */
    private fun hvOption(option: TokenType): HumanVerificationRobot =
        clickElement(option.value.toUpperCase(Locale.ROOT), TextView::class.java)

    class Verify : CoreVerify() {
        fun hvElementsDisplayed() {
            view.withText(EMAIL.value.toUpperCase(Locale.ROOT)).wait()
            view.withText(SMS.value.toUpperCase(Locale.ROOT)).wait()
            view.withText(CAPTCHA.value.toUpperCase(Locale.ROOT)).wait()
        }

        fun captchaDisplayed() = view.withId(R.id.captchaWebView).wait()
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
