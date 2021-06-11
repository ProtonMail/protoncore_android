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

package me.proton.core.test.android.robots.login

import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.signup.SignupRobot

/**
 * [WelcomeRobot] class contains welcome screen actions and verifications implementation.
 */
class WelcomeRobot : CoreRobot() {

    /**
     * Clicks 'sign in' button
     * @return [LoginRobot]
     */
    fun signIn(): LoginRobot = clickElement(R.id.sign_in)

    /**
     * Clicks 'create account' button
     * @return [SignupRobot]
     */
    fun createAccount(): SignupRobot = clickElement(R.id.sign_up)
}
