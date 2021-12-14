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

package me.proton.core.test.android.robots.auth.signup

import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot

class CodeVerificationRobot : CoreRobot() {
    /**
     * Sets the value of verification code input to [code]
     * @return [CodeVerificationRobot]
     */
    fun setCode(code: String): CodeVerificationRobot = replaceText(R.id.verificationCodeEditText, code)

    /**
     * Clicks 'Verify Code' button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> verifyCode(): T = clickElement(R.id.verifyButton)
}