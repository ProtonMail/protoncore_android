/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

import me.proton.core.test.android.robots.confirmpassword.ConfirmPasswordRobot
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import me.proton.core.test.android.uitests.tests.SmokeTest
import org.junit.Before
import org.junit.Test

class ConfirmPasswordTests : BaseTest() {
    private lateinit var confirmPasswordRobot: ConfirmPasswordRobot
    private val user = users.getUser()

    @Before
    fun triggerConfirmPassword() {
        quark.jailUnban()
        login(user)

        CoreexampleRobot()
            .lockScopes()
            .verify {
                secureScopeStateIs("[]")
                scopeTriggerStatusIs("removed")
            }
    }

    @Test
    @SmokeTest
    fun closeConfirmPassword() {
        confirmPasswordRobot = CoreexampleRobot().confirmPasswordPassword()
        confirmPasswordRobot.verify { confirmPasswordElementsDisplayed() }

        confirmPasswordRobot
            .cancel<CoreexampleRobot>()
            .verify {
                secureScopeStateIs()
                scopeTriggerStatusIs("403")
                primaryUserIs(user)
            }
    }

    @Test
    fun lockedScope() {
        confirmPasswordRobot = CoreexampleRobot().confirmPasswordLocked()
        confirmPasswordRobot.verify { confirmPasswordElementsDisplayed() }

        confirmPasswordRobot
            .setPassword(user.password)
            .enter<CoreexampleRobot>()
            .verify {
                secureScopeStateIs("locked")
                scopeTriggerStatusIs("1000")
                primaryUserIs(user)
            }
    }

    @Test
    fun passwordScope() {
        confirmPasswordRobot = CoreexampleRobot().confirmPasswordPassword()
        confirmPasswordRobot.verify { confirmPasswordElementsDisplayed() }

        confirmPasswordRobot
            .setPassword(user.password)
            .enter<CoreexampleRobot>()
            .verify {
                secureScopeStateIs("locked", "password")

                // Receiving code 2011 or 2501 is still fine, it just means the account
                // cannot be used to get a mnemonic;
                // 2011 "Recovery phrase can be used only by users with migrated keys"
                // 2501 "Recovery phrase not set for the current account"
                scopeTriggerStatusIs("1000", "2011", "2501")

                primaryUserIs(user)
            }
    }
}
