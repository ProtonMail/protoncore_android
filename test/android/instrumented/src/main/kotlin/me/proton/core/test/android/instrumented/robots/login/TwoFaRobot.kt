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

package me.proton.core.test.android.instrumented.robots.login

import me.proton.core.auth.R
import me.proton.core.test.android.instrumented.robots.BaseRobot

/**
 * [TwoFaRobot] class contains actions and verifications for Two factor authentication functionality.
 */
open class TwoFaRobot : BaseRobot() {
    fun setSecondFactorInput(input: String): TwoFaRobot = setText(R.id.secondFactorInput, input)
    fun switchTwoFactorMode(): TwoFaRobot = clickElement(R.id.recoveryCodeButton)
    inline fun <reified T> authenticate(): T = clickElement(R.id.authenticateButton)
}
