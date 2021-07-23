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

package me.proton.core.presentation.ui.view

import android.widget.CompoundButton
import com.google.android.material.checkbox.MaterialCheckBox

/**
 * A helper for putting CompoundButton's button on the end side of the view.
 *
 * Note: it uses drawableEnd to display the button drawable.
 */
class CompoundButtonPositionHelper(private val button: CompoundButton) {

    private val drawable = button.buttonDrawable

    init {
        button.buttonDrawable = null
        val originalDrawables = button.compoundDrawablesRelative
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
            originalDrawables[0],
            originalDrawables[1],
            drawable,
            originalDrawables[2]
        )
    }

    fun afterDraw() {
        drawable?.bounds?.let {
            // Similar to what CompoundButton.onDraw does.
            val isLayoutRtl = MaterialCheckBox.LAYOUT_DIRECTION_RTL == button.getLayoutDirection()
            val rippleLeft = if (isLayoutRtl) 0 else button.width - it.width()
            val rippleRight = if (isLayoutRtl) it.width() else button.width

            val contentHeight = button.height - button.paddingTop - button.paddingBottom
            val rippleTop = button.paddingTop + (contentHeight - it.height()) / 2

            button.background.setHotspotBounds(rippleLeft, rippleTop, rippleRight, rippleTop + it.height())
        }
    }
}
