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

package me.proton.core.test.android.robots.auth.login

import me.proton.core.auth.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

/**
 * [TwoFaRobot] class contains second factor authentication actions and verifications implementation.
 */
class TwoFaRobot : CoreRobot() {

    /**
     * Fills in second factor input (recovery or code) with given [value]
     * @return [TwoFaRobot]
     */
    fun setSecondFactorInput(value: String): TwoFaRobot = addText(R.id.secondFactorInput, value)

    /**
     * Clicks the recovery code button. Switches between second factor code and second factor recovery code
     * @return [TwoFaRobot]
     */
    fun switchTwoFactorMode(): TwoFaRobot = clickElement(R.id.recoveryCodeButton)

    /**
     * Clicks authenticate button
     * @param T next Robot in flow
     * @return an instance of [T]
     */
    inline fun <reified T> authenticate(): T = clickElement(R.id.authenticateButton)

    class Verify : CoreVerify() {
        fun formElementsDisplayed() {
            view.withId(R.id.authenticateButton).checkDisplayed()
            view.withId(R.id.recoveryCodeButton).checkDisplayed()
            view.withId(R.id.secondFactorInput).checkDisplayed()
        }
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)
}
