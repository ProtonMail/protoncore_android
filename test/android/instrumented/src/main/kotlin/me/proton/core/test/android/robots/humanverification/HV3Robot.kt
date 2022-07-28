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
import android.widget.TextView
import androidx.test.espresso.web.model.Atom
import androidx.test.espresso.web.model.ElementReference
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.selectFrameByIndex
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.TokenType.CAPTCHA
import me.proton.core.humanverification.domain.entity.TokenType.EMAIL
import me.proton.core.humanverification.domain.entity.TokenType.SMS
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

internal class HV3Robot : HVRobot() {
    override fun help(): HVRobot = clickElement<HV2Robot>(R.id.menu_help, TextView::class.java)

    override fun captcha(): HVCaptchaRobot = hvOption<HV3CaptchaRobot>(CAPTCHA)

    override fun email(): HVEmailRobot = hvOption<HV3EmailRobot>(EMAIL)

    override fun sms(): HVSmsRobot = hvOption<HV3SmsRobot>(SMS)

    override fun verify(block: HVRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private inline fun <reified T> hvOption(option: TokenType): T {
        val testId = when (option) {
            CAPTCHA -> "tab-header-CAPTCHA-button"
            SMS -> "tab-header-SMS-button"
            EMAIL -> "tab-header-Email-button"
            else -> throw IllegalArgumentException("Only Captcha, SMS and Email are supported")
        }
        view.withId(R.id.humanVerificationWebView).checkDisplayed()
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button[data-testid=\"$testId\"]"))
            .perform(webClick())
            .reset()
        return T::class.java.newInstance()
    }

    private class Verify : HVRobot.Verify, CoreVerify() {
        override fun hvElementsDisplayed() {
            view.withId(R.id.humanVerificationWebView).checkDisplayed()
        }
    }
}

internal class HV3CaptchaRobot : HVCaptchaRobot, CoreRobot() {
    override fun <T> iAmHuman(next: Class<T>): T {
        Thread.sleep(2000) // Special case
        view.instanceOf(WebView::class.java).click()
        onWebView()
            .inWindow(selectFrameByIndex(0))
            .withElement(findElement(Locator.ID, "checkbox"))
            .perform(webClick())
        return next.newInstance()
    }

    override fun verify(block: HVCaptchaRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private class Verify : HVCaptchaRobot.Verify, CoreVerify() {
        override fun captchaDisplayed() {
            view.withId(R.id.humanVerificationWebView).checkDisplayed()
        }
    }
}

internal class HV3EmailRobot : HVEmailRobot, CoreRobot() {
    override fun setEmail(email: String): HV3EmailRobot = setWebText(findElement(Locator.ID, "email"), email)

    override fun getVerificationCode(): HVCodeRobot {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button.button-large"))
            .perform(webClick())
        return HV3CodeRobot()
    }
}

internal class HV3SmsRobot : HVSmsRobot, CoreRobot() {
    override fun setPhone(number: String?): HVSmsRobot =
        setWebText<HV3SmsRobot>(findElement(Locator.ID, "phone"), number)

    override fun countryCodeList(): HVSmsCountryRobot {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button[data-testid=\"dropdown-button\"]"))
            .perform(webClick())
        return HV3SmsCountryRobot()
    }

    override fun getVerificationCode(): HVCodeRobot {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button.button-large"))
            .perform(webClick())
        return HV3CodeRobot()
    }
}

internal class HV3SmsCountryRobot : HVSmsCountryRobot, CoreRobot() {
    override fun search(text: String): HVSmsCountryRobot {
        internalSearch(text)
        return this
    }

    override fun selectCountry(country: String): HVSmsRobot {
        internalSearch(country)
            .withElement(findElement(Locator.CLASS_NAME, "dropdown-content"))
            .withContextualElement(findElement(Locator.CLASS_NAME, "dropdown-item-button"))
            .perform(webClick())
        return HV3SmsRobot()
    }

    private fun dialog() = onWebView().withElement(findElement(Locator.CSS_SELECTOR, "div[role=\"dialog\"]"))

    private fun internalSearch(text: String) = dialog()
        .withContextualElement(findElement(Locator.ID, "search-keyword"))
        .perform(webClick())
        .perform(webKeys(text))
}

internal class HV3CodeRobot : HVCodeRobot, CoreRobot() {
    override fun setCode(code: String): HVCodeRobot {
        onWebView()
            .withElement(findElement(Locator.ID, "verification"))
            .perform(webClick())
            .perform(webKeys(code))
        return this
    }

    override fun <T> verifyCode(next: Class<T>): T {
        onWebView()
            .withElement(findElement(Locator.CLASS_NAME, "button-solid-norm"))
            .perform(webClick())
            .reset()
        return next.newInstance()
    }
}

private inline fun <reified T> setWebText(element: Atom<ElementReference>, text: String?): T {
    onWebView()
        .withElement(element)
        .perform(clearElement())
        .perform(webKeys(text))
    return T::class.java.newInstance()
}
