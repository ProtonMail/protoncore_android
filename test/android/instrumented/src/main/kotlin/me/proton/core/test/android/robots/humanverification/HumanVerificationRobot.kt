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
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import me.proton.core.humanverification.R
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.TokenType.CAPTCHA
import me.proton.core.humanverification.domain.entity.TokenType.EMAIL
import me.proton.core.humanverification.domain.entity.TokenType.SMS
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify
import me.proton.core.test.android.robots.other.CountryRobot

/**
 * [HumanVerificationRobot] base class contains human verification actions and verifications implementation.
 */
open class HumanVerificationRobot : CoreRobot() {

    /**
     * Clicks 'help' button
     * @return [HumanVerificationRobot]
     */
    fun help(): HumanVerificationRobot = clickElement(R.id.menu_help, TextView::class.java)

    /**
     * Selects 'email' human verification option
     * @return [HumanVerificationRobot]
     */
    fun email(): HumanVerificationRobot = hvOption(EMAIL)

    /**
     * Selects 'sms' human verification option
     * @return [HumanVerificationRobot]
     */
    fun sms(): HumanVerificationRobot = hvOption(SMS)

    /**
     * Sets the value of phone number input to [number]
     * @return [HumanVerificationRobot]
     */
    fun setPhone(number: String?): HumanVerificationRobot = setWebText(findElement(Locator.ID, "phone"), number)

    /**
     * Clicks country code list button
     * @return [CountryRobot]
     */
    fun countryCodeList(): CountryWebRobot {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button[data-testid=\"dropdown-button\"]"))
            .perform(webClick())
        return CountryWebRobot()
    }

    /**
     * Sets the value of email input to [email]
     * @return [HumanVerificationRobot]
     */
    fun setEmail(email: String): HumanVerificationRobot = setWebText(findElement(Locator.ID, "email"), email)

    /**
     * Clicks 'get verification code' button
     * @return [HumanVerificationRobot]
     */
    fun getVerificationCode(): HumanVerificationRobot {
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button.button-large"))
            .perform(webClick())
        Thread.sleep(5000)
        return HumanVerificationRobot()
    }

    /**
     * Selects 'captcha' human verification option
     * @return [HumanVerificationRobot]
     */
    fun captcha(): HumanVerificationRobot = hvOption(CAPTCHA)

    fun setCode(code: String): HumanVerificationRobot {
        onWebView()
            .withElement(findElement(Locator.ID, "verification"))
            .perform(webClick())
            .perform(webKeys(code))
        return HumanVerificationRobot()
    }

    inline fun <reified T> verifyCode(): T {
        onWebView()
            .withElement(findElement(Locator.CLASS_NAME, "button-solid-norm"))
            .perform(webClick())
            .reset()
        return T::class.java.newInstance()
    }

    /**
     * Checks "I'm not a robot" checkbox. Only works with development reCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> imNotARobot(): T = clickElement(R.id.humanVerificationWebView, WebView::class.java)

    inline fun <reified T> setWebText(element: Atom<ElementReference>, text: String?): T {
        onWebView()
            .withElement(element)
            .perform(clearElement())
            .perform(webKeys(text))

        Thread.sleep(1000)
        return T::class.java.newInstance()
    }

    /**
     * Checks "I am human" checkbox. Only works with development hCAPTCHA enabled
     * @param T next Robot to be returned
     * @return an instance of [T]
     */
    inline fun <reified T> iAmHuman(): T {
        verify { hvElementsDisplayed() }
        Thread.sleep(2000) // Special case
        view.instanceOf(WebView::class.java).click()
        return T::class.java.newInstance()
    }

    /**
     * Clicks text view with [option] text
     * @return [HumanVerificationRobot]
     */
    private fun hvOption(option: TokenType): HumanVerificationRobot {
        val testId = when (option) {
            CAPTCHA -> "tab-header-CAPTCHA-button"
            SMS -> "tab-header-SMS-button"
            EMAIL -> "tab-header-Email-button"
            else -> throw IllegalArgumentException("Only Captcha, SMS and Email are supported")
        }
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button[data-testid=\"$testId\"]"))
            .perform(webClick())
            .reset()

        Thread.sleep(100L)

        return HumanVerificationRobot()
    }

    class Verify : CoreVerify() {
        fun hvElementsDisplayed() {
            view.withId(R.id.humanVerificationWebView).checkDisplayed()
        }

        fun captchaDisplayed() = onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "iframe"))
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}

class CountryWebRobot {

    fun dialog() = onWebView()
        .withElement(findElement(Locator.CSS_SELECTOR, "div[role=\"dialog\"]"))

    fun search(text: String?) : CountryWebRobot {
        internalSearch(text)
        return CountryWebRobot()
    }

    fun internalSearch(text: String?) = dialog()
        .withContextualElement(findElement(Locator.ID, "search-keyword"))
        .perform(webClick())
        .perform(webKeys(text))

    inline fun <reified T> selectCountry(country: String?): T {
        internalSearch(country)
            .withElement(findElement(Locator.CLASS_NAME, "dropdown-content"))
            .withContextualElement(findElement(Locator.CLASS_NAME, "dropdown-item-button"))
            .perform(webClick())
        return T::class.java.newInstance()
    }

    inline fun <reified T> close(): T {
        dialog()
            .withContextualElement(findElement(Locator.CLASS_NAME, "dropdown-backdrop"))
            .perform(webClick())
        return T::class.java.newInstance()
    }
}
