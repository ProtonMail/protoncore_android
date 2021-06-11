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

package me.proton.core.test.android.uitests.tests.humanverification

import me.proton.android.core.coreexample.R
import me.proton.core.test.android.robots.humanverification.HumanVerificationRobot
import me.proton.core.test.android.plugins.Requests.jailUnban
import me.proton.core.test.android.robots.login.WelcomeRobot
import me.proton.core.test.android.robots.humanverification.CodeVerificationRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore
class HumanVerificationTests : BaseTest() {

    private val humanVerificationRobot = HumanVerificationRobot()
    private val user = users.getUser { it.country == "Lithuania" && it.isDefault }

    @Before
    fun triggerHumanVerification() {
        jailUnban()
        WelcomeRobot()
            .signIn()
            .loginUser<CoreexampleRobot>(user)
            .humanVerification()
    }

    @Test
    fun closeHvViews() {
        humanVerificationRobot
            .sms()
            .countryCodeList()
            .close<HumanVerificationRobot>()
            .help()
            .close<HumanVerificationRobot>()
            .close<CoreexampleRobot>()
            .verify { primaryUserIs(user) }
    }

    @Test
    fun canRequestEmailVerification() {
        humanVerificationRobot
            .email()
            .setEmail(user.email)
            .getVerificationCode()
            .verify { errorSnackbarDisplayed(R.string.human_verification_sending_failed) }
    }

    @Test
    fun canRequestPhoneVerification() {
        val countryWithoutLastChar = user.country.substring(0, user.country.length - 1)
        humanVerificationRobot
            .sms()
            .countryCodeList()
            .search(countryWithoutLastChar)
            .selectCountry<CodeVerificationRobot>(user.country)
            .setPhone(user.phone)
            .getVerificationCode()
            .verify { errorSnackbarDisplayed(R.string.human_verification_sending_failed) }
    }

    @Test
    fun canVerifyCaptcha() {
        humanVerificationRobot
            .sms()
            .email()
            .captcha()
            .verify { captchaDisplayed() }
    }

    @Test
    fun alreadyHaveCode() {
        humanVerificationRobot
            .sms()
            .alreadyHaveCode()
            .setCode("1")
            .verifyCode<CoreexampleRobot>()
            .verify { primaryUserIs(user) }
    }
}
