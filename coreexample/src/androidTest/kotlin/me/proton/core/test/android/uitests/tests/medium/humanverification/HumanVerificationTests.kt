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

import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.robots.humanverification.HumanVerificationRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore("WebView human verification does not support atlas proxy")
class HumanVerificationTests : BaseTest() {

    private val humanVerificationRobot = HumanVerificationRobot()
    private val user = users.getUser()
    private val defaultCode = quark.defaultVerificationCode

    @Before
    fun triggerHumanVerification() {
        quark.jailUnban()

        login(user)

        CoreexampleRobot()
            .humanVerification()
            .verify { hvElementsDisplayed() }
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
            .verify {
                accountSwitcherDisplayed()
                userStateIs(user, Ready, Authenticated)
            }
    }

    @Test
    @Ignore("WebView issue. A espresso bug that prevents modification of the value of input fields makes it impossible to actually test this.")
    fun email() {
        val testAddress = "testEmail@example.lt"

        humanVerificationRobot
            .email()
            .setEmail(testAddress)
            .getVerificationCode()
            .setCode(defaultCode)
            .verifyCode<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
                userStateIs(user, Ready, Authenticated)
            }
    }

    @Test
    @Ignore("WebView issue. A espresso bug that prevents modification of the value of input fields makes it impossible to actually test this.")
    fun phone() {
        val testPhoneNo = "2087599036"
        val testCountry = "United Kingdom"

        humanVerificationRobot
            .sms()
            .countryCodeList()
            .search(testCountry)
            .selectCountry<HumanVerificationRobot>(testCountry)
            .setPhone(testPhoneNo)
            .getVerificationCode()
            .setCode(defaultCode)
            .verifyCode<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
                userStateIs(user, Ready, Authenticated)
            }
    }

    @Test
    fun captcha() {
        humanVerificationRobot
            .captcha()
            .iAmHuman<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
                userStateIs(user, Ready, Authenticated)
            }
    }
}
