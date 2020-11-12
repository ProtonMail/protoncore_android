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

package me.proton.core.presentation.ui

import android.text.InputType
import android.view.View
import android.widget.TextView

/*
 * Common view extension functions for Proton custom views.
 * @author Dino Kadrikj.
 */

/**
 * Sets new text for [TextView]  or sets the [TextView.setVisibility] to [View.GONE] if `null` is passed.
 */
internal fun TextView.setTextOrGoneIfNull(value: CharSequence?) {
    visibility = if (value != null) {
        text = value
        View.VISIBLE
    } else {
        View.GONE
    }
}

/**
 * Checks if the input type mask contains password type.
 */
internal fun Int.isInputTypePassword(): Boolean {
    return this and (InputType.TYPE_MASK_CLASS or InputType.TYPE_MASK_VARIATION) ==
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
}
