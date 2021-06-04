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

import android.widget.TextView
import me.proton.core.humanverification.R
import me.proton.core.humanverification.domain.entity.TokenType.CAPTCHA
import me.proton.core.humanverification.domain.entity.TokenType.EMAIL
import me.proton.core.humanverification.domain.entity.TokenType.SMS
import me.proton.core.test.android.instrumented.robots.BaseRobot
import me.proton.core.test.android.instrumented.robots.BaseVerify

/**
 * [HumanVerificationRobot] class contains actions and verifications for Human verification functionality.
 */
open class HumanVerificationRobot : BaseRobot() {

    fun help(): HumanVerificationRobot = clickElement(R.id.helpButton)
    fun email(): CodeVerificationRobot = hvOption(EMAIL.value)
    fun sms(): CodeVerificationRobot = hvOption(SMS.value)
    fun captcha(): HumanVerificationRobot = hvOption(CAPTCHA.value)
    private fun hvOption(option: String): CodeVerificationRobot =
        clickElement(option.toUpperCase(), TextView::class.java)

    class Verify : BaseVerify() {

        fun hvElementsDisplayed() {
            view.withText(EMAIL.value.toUpperCase()).wait().checkDisplayed()
            view.withText(SMS.value.toUpperCase()).wait().checkDisplayed()
            view.withText(CAPTCHA.value.toUpperCase()).wait().checkDisplayed()
        }

        fun captchaDisplayed() {
            view.withId(R.id.captchaWebView).wait().checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
