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

package me.proton.core.test.android.uitests.tests.medium.confirmpassword

import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.SessionState.Authenticated
import me.proton.core.test.android.robots.confirmpassword.ConfirmPasswordRobot
import me.proton.core.test.android.uitests.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.Before
import org.junit.Test

class ConfirmPasswordTests : BaseTest() {

    private val confirmPasswordRobot = ConfirmPasswordRobot()
    private val user = users.getUser()

    @Before
    fun triggerConfirmPassword() {
        quark.jailUnban()

        login(user)
    }

    @Test
    @SmokeTest
    fun closeConfirmPassword() {
        confirmPasswordRobot
            .cancel<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
            }
    }

    @Test
    fun lockedScope() {
        CoreexampleRobot()
            .confirmPasswordLocked()
            .verify { confirmPasswordElementsDisplayed() }

        confirmPasswordRobot
            .setPassword(user.password)
            .enter<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
                userStateIs(user, Ready, Authenticated)
            }
    }

    @Test
    fun passwordScope() {
        CoreexampleRobot()
            .confirmPasswordPassword()
            .verify { confirmPasswordElementsDisplayed() }

        confirmPasswordRobot
            .setPassword(user.password)
            .enter<CoreexampleRobot>()
            .verify {
                accountSwitcherDisplayed()
                userStateIs(user, Ready, Authenticated)
            }
    }
}
