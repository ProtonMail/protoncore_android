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

package me.proton.core.test.android.uitests.tests.medium.humanverification

import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.android.robots.humanverification.HVRobot
import me.proton.core.test.android.uitests.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Test

class HumanVerificationTests : BaseTest() {
    private val humanVerificationRobot = HVRobot()
    private val defaultCode = quark.defaultVerificationCode

    @Before
    fun triggerHumanVerification() {
        quark.jailUnban()

        AddAccountRobot()
            .back<CoreexampleRobot>()
            .humanVerification()
            .verify { hvElementsDisplayed() }
    }

    @Test
    fun closeHvViews() {
        humanVerificationRobot
            .sms()
            .countryCodeList()
            .dismiss()
        humanVerificationRobot
            .help()
            .close<HVRobot>()
            .close<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun email() {
        val testAddress = "testEmail@example.lt"

        humanVerificationRobot
            .email()
            .setEmail(testAddress)
            .getVerificationCode()
            .setCode(defaultCode)
            .verifyCode(CoreexampleRobot::class.java)
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun phone() {
        val testPhoneNo = "2087599036"
        val testCountry = "United Kingdom"

        humanVerificationRobot
            .sms()
            .countryCodeList()
            .selectCountry(testCountry)
            .setPhone(testPhoneNo)
            .getVerificationCode()
            .setCode(defaultCode)
            .verifyCode(CoreexampleRobot::class.java)
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun captcha() {
        humanVerificationRobot
            .captcha()
            .iAmHuman(CoreexampleRobot::class.java)
            .verify {
                accountSwitcherDisplayed()
            }
    }
}
