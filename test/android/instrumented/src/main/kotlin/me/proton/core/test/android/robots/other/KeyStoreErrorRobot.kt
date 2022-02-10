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

package me.proton.core.test.android.robots.other

import me.proton.core.crypto.validator.presentation.R
import me.proton.core.test.android.robots.CoreRobot
import me.proton.core.test.android.robots.CoreVerify

class KeyStoreErrorRobot : CoreRobot() {

    fun tapContinue(): KeyStoreErrorRobot {
        view.withText(R.string.crypto_keystore_error_continue_action).click()
        return this
    }
    fun tapMoreInfo(): KeyStoreErrorRobot {
        view.withText(R.string.crypto_keystore_error_more_info_action).click()
        return this
    }
    fun tapLogout(): KeyStoreErrorRobot {
        view.withText(R.string.crypto_keystore_error_logout_action).click()
        return this
    }


    class Verify : CoreVerify() {
        fun dialogIsDisplayed() = view.withText(R.string.crypto_keystore_error_title).checkDisplayed()
        fun exitButtonIsDisplayed() =
            view.withText(R.string.crypto_keystore_error_exit_action).checkDisplayed()
        fun logoutButtonIsDisplayed() =
            view.withText(R.string.crypto_keystore_error_logout_action).checkDisplayed()
    }

    inline fun verify(block: Verify.() -> Unit) = Verify().apply(block)

}
