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

package me.proton.core.test.android.robots

import android.widget.TextView
import androidx.annotation.StringRes
import me.proton.core.presentation.R
import me.proton.core.test.android.instrumented.utils.StringUtils

/**
 * [CoreVerify] Contains common core specific verifications implementation
 */
open class CoreVerify : CoreRobot() {
    fun errorSnackbarDisplayed(@StringRes stringRes: Int) {
        view
            .withSnackbarText(StringUtils.stringFromResource(stringRes))
    }

    fun errorSnackbarDisplayed(text: String) {
        view
            .withSnackbarText(text)
    }

    fun inputErrorDisplayed(@StringRes stringRes: Int, scroll: Boolean = false) {
        view
            .withId(R.id.textinput_error)
            .instanceOf(TextView::class.java)
            .withText(stringRes)
            .apply { if (scroll) scrollTo() }
            .checkDisplayed()
    }

    fun snackbarDisplayed(@StringRes stringRes: Int) {
        view
            .withSnackbarText(StringUtils.stringFromResource(stringRes))
    }
}
