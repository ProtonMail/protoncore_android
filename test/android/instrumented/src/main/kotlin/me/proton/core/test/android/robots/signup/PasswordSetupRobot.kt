/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.test.android.robots.signup

import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot

/**
 * [PasswordSetupRobot] class contains password setup actions and verifications implementation
 */
class PasswordSetupRobot : CoreRobot() {

    /**
     * Sets the value of password input to [password]
     * @return [PasswordSetupRobot]
     */
    fun password(pwd: String): PasswordSetupRobot = setText(R.id.passwordInput, pwd)

    /**
     * Sets the value of password confirmation input to [password]
     * @return [PasswordSetupRobot]
     */
    fun confirmPassword(pwd: String): PasswordSetupRobot = setText(R.id.confirmPasswordInput, pwd)

    /**
     * Clicks 'next' button
     * @return [RecoveryMethodsRobot]
     */
    fun next(): RecoveryMethodsRobot = clickElement(R.id.nextButton)
}
