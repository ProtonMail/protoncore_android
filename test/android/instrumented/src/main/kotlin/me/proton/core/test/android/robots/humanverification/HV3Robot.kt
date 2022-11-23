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
import android.widget.TextView
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.espresso.web.webdriver.DriverAtoms.clearElement
import androidx.test.espresso.web.webdriver.DriverAtoms.findElement
import androidx.test.espresso.web.webdriver.DriverAtoms.webClick
import androidx.test.espresso.web.webdriver.DriverAtoms.webKeys
import androidx.test.espresso.web.webdriver.Locator
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until.findObject
import androidx.test.uiautomator.Until.hasObject
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.TokenType.CAPTCHA
import me.proton.core.humanverification.domain.entity.TokenType.EMAIL
import me.proton.core.humanverification.domain.entity.TokenType.SMS
import me.proton.core.humanverification.presentation.R
import me.proton.core.test.android.robots.CoreVerify

private const val GET_VERIFICATION_CODE = "Get verification code"
private const val LOADING_TIMEOUT_MS = 30_000L

internal class HV3Robot : BaseHVRobot(), WithUiDevice {
    override fun help(): BaseHVRobot = clickElement<HV3Robot>(R.id.menu_help, TextView::class.java)

    override fun captcha(): BaseHVCaptchaRobot = hvOption<HV3CaptchaRobot>(CAPTCHA)

    override fun email(): BaseHVEmailRobot = hvOption<HV3EmailRobot>(EMAIL)

    override fun sms(): BaseHVSmsRobot = hvOption<HV3SmsRobot>(SMS)

    override fun verify(block: BaseHVRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private inline fun <reified T> hvOption(option: TokenType): T {
        view.withId(R.id.humanVerificationWebView).checkDisplayed()
        val testId = when (option) {
            CAPTCHA -> "tab-header-CAPTCHA-button"
            SMS -> "tab-header-SMS-button"
            EMAIL -> "tab-header-Email-button"
            else -> throw IllegalArgumentException("Only Captcha, SMS and Email are supported")
        }
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button[data-testid=\"$testId\"]"))
            .perform(webClick())
        return T::class.java.newInstance()
    }

    private class Verify : BaseHVRobot.Verify, CoreVerify(), WithUiDevice {
        override fun hvElementsDisplayed() {
            hasObject(By.textContains("CAPTCHA")).waitForIt()
            hasObject(By.textContains("Email")).waitForIt()
            hasObject(By.textContains("SMS")).waitForIt()
        }
    }
}

internal class HV3CaptchaRobot : BaseHVCaptchaRobot(), WithUiDevice {
    override fun <T> iAmHuman(next: Class<T>): T {
        findObject(By.text("I am human")).waitForIt().click()
        return next.newInstance()
    }

    override fun verify(block: BaseHVCaptchaRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private class Verify : BaseHVCaptchaRobot.Verify, CoreVerify(), WithUiDevice {
        override fun captchaDisplayed() {
            hasObject(By.text("I am human")).waitForIt()
        }
    }
}

internal class HV3EmailRobot : BaseHVEmailRobot(), WithUiDevice {
    override fun setEmail(email: String): HV3EmailRobot {
        hasObject(By.text("Email address")).waitForIt()
        findObject(By.clazz(EditText::class.java))
            .waitForIt()
            .text = email
        return this
    }

    override fun getVerificationCode(): BaseHVCodeRobot {
        findObject(By.text(GET_VERIFICATION_CODE))
            .waitForIt()
            .click()
        return HV3CodeRobot()
    }
}

internal class HV3SmsRobot : BaseHVSmsRobot(), WithUiDevice {
    private val phoneNumber = "Phone number"

    override fun setPhone(number: String?): BaseHVSmsRobot {
        hasObject(By.text(phoneNumber)).waitForIt()
        findObject(By.clazz(EditText::class.java))
            .waitForIt()
            .text = number
        return this
    }

    override fun countryCodeList(): BaseHVSmsCountryRobot {
        hasObject(By.text(phoneNumber)).waitForIt()
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "button[data-testid=\"dropdown-button\"]"))
            .perform(webClick())
        return HV3SmsCountryRobot()
    }

    override fun getVerificationCode(): BaseHVCodeRobot {
        findObject(By.text(GET_VERIFICATION_CODE))
            .waitForIt()
            .click()
        return HV3CodeRobot()
    }
}

internal class HV3SmsCountryRobot : BaseHVSmsCountryRobot(), WithUiDevice {
    override fun dismiss(): BaseHVSmsRobot = HV3Robot().sms()

    override fun search(text: String): BaseHVSmsCountryRobot {
        internalSearch(text)
        return this
    }

    override fun selectCountry(country: String): BaseHVSmsRobot {
        internalSearch(country)
        uiDevice.waitForIdle()
        onWebView()
            .withElement(findElement(Locator.CLASS_NAME, "dropdown-content"))
            .withContextualElement(findElement(Locator.CLASS_NAME, "dropdown-item-button"))
            .perform(webClick())
        return HV3SmsRobot()
    }

    private fun internalSearch(country: String) {
        uiDevice.waitForIdle()

        // We have to use a trick, since inserting text via `webKeys`
        // doesn't perform the filtering on the list.
        // First we use `onWebView()...` to insert the placeholder text,
        // and then we can easily use UiAutomator
        // to find an input field with the placeholder.
        val placeholder = "AAA"
        onWebView()
            .withElement(findElement(Locator.CSS_SELECTOR, "div[role=\"dialog\"]"))
            .withContextualElement(findElement(Locator.ID, "search-keyword"))
            .perform(clearElement())
            .perform(webKeys(placeholder))
        findObject(By.text(placeholder).clazz(EditText::class.java)).waitForIt().text = country
    }
}

internal class HV3CodeRobot : BaseHVCodeRobot(), WithUiDevice {
    private val verifyButton = "Verify"

    override fun setCode(code: String): BaseHVCodeRobot {
        hasObject(By.text(verifyButton)).waitForIt()
        findObject(By.clazz(EditText::class.java))
            .waitForIt()
            .text = code
        return this
    }

    override fun <T> verifyCode(next: Class<T>): T {
        hasObject(By.text(verifyButton)).waitForIt()
        onWebView()
            .withElement(findElement(Locator.XPATH, "//button[text()='$verifyButton']"))
            .perform(webClick())
        return next.newInstance()
    }

    override fun verify(block: BaseHVCodeRobot.Verify.() -> Unit) {
        Verify().apply(block)
    }

    private class Verify: BaseHVCodeRobot.Verify, WithUiDevice {
        override fun incorrectCode() {
            hasObject(By.textContains("Invalid verification code")).waitForIt()
        }
    }
}

private interface WithUiDevice {
    val uiDevice: UiDevice get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun <T : Any> SearchCondition<T>.waitForIt(): T {
        val result = uiDevice.wait(this, LOADING_TIMEOUT_MS)
        check(result != null) { "Could not find a UI object." }
        return result
    }
}
