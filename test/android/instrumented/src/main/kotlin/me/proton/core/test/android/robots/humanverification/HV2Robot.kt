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
import android.widget.AutoCompleteTextView
import android.widget.TextView
import com.google.android.material.textview.MaterialTextView
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.TokenType.CAPTCHA
import me.proton.core.humanverification.domain.entity.TokenType.EMAIL
import me.proton.core.humanverification.domain.entity.TokenType.SMS
import me.proton.core.presentation.ui.view.ProtonAutoCompleteInput
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import java.util.Locale

internal class HV2Robot : HVRobot() {
    override fun help(): HVRobot = clickElement<HV2Robot>(R.id.menu_help, TextView::class.java)

    override fun captcha(): HVCaptchaRobot = hvOption<HV2CaptchaRobot>(CAPTCHA)

    override fun email(): HVEmailRobot = hvOption<HV2EmailRobot>(EMAIL)

    override fun sms(): HVSmsRobot = hvOption<HV2SmsRobot>(SMS)

    override fun verify(block: HVRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private inline fun <reified T> hvOption(option: TokenType): T =
        clickElement(option.value.uppercase(Locale.ROOT), TextView::class.java)

    private class Verify : HVRobot.Verify, CoreVerify() {
        override fun hvElementsDisplayed() {
            view.withText(EMAIL.value.uppercase(Locale.ROOT)).checkDisplayed()
            view.withText(SMS.value.uppercase(Locale.ROOT)).checkDisplayed()
            view.withText(CAPTCHA.value.uppercase(Locale.ROOT)).checkDisplayed()
        }
    }
}

internal class HV2CaptchaRobot : HVCaptchaRobot, CoreRobot() {
    override fun <T> iAmHuman(next: Class<T>): T {
        Thread.sleep(2000) // Special case
        view.instanceOf(WebView::class.java).click()
        return next.newInstance()
    }

    override fun verify(block: HVCaptchaRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private class Verify : HVCaptchaRobot.Verify, CoreVerify() {
        override fun captchaDisplayed() {
            view.withId(R.id.captchaWebView)
        }
    }
}

internal class HV2EmailRobot : HVEmailRobot, CoreRobot() {
    override fun setEmail(email: String): HV2EmailRobot = replaceText(R.id.emailEditText, email)

    override fun getVerificationCode(): HVCodeRobot = clickElement<HV2CodeRobot>(R.id.getVerificationCodeButton)
}

internal class HV2SmsRobot : HVSmsRobot, CoreRobot() {
    override fun setPhone(number: String?): HVSmsRobot = replaceText<HV2SmsRobot>(R.id.smsEditText, number!!)

    override fun countryCodeList(): HVSmsCountryRobot =
        clickElement<HV2SmsCountryRobot>(R.id.callingCodeText, ProtonAutoCompleteInput::class.java)

    override fun getVerificationCode(): HVCodeRobot = clickElement<HV2CodeRobot>(R.id.getVerificationCodeButton)
}

internal class HV2SmsCountryRobot : HVSmsCountryRobot, CoreRobot() {
    override fun <T> close(next: Class<T>): T = clickElement(R.id.closeButton, next = next)

    override fun search(text: String): HVSmsCountryRobot {
        view.withId(R.id.filterEditText).click()
        view.instanceOf(AutoCompleteTextView::class.java).typeText(text)
        return this
    }

    override fun selectCountry(country: String): HVSmsRobot =
        clickElement<HV2SmsRobot>(country, MaterialTextView::class.java)
}

internal class HV2CodeRobot : HVCodeRobot, CoreRobot() {
    override fun setCode(code: String): HVCodeRobot = replaceText(R.id.verificationCodeEditText, code)

    override fun <T> verifyCode(next: Class<T>): T = clickElement(R.id.verifyButton, next = next)
}
