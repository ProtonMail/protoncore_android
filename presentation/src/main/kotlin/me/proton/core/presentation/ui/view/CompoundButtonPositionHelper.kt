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

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.CompoundButton
import com.google.android.material.checkbox.MaterialCheckBox

/**
 * A helper for putting CompoundButton's button on the end side of the view.
 */
class CompoundButtonPositionHelper(private val button: CompoundButton) {

    private val drawable = button.buttonDrawable

    init {
        button.buttonDrawable = null
        drawable?.jumpToCurrentState()
    }

    fun onDraw(canvas: Canvas) {
        if (drawable != null) {
            // Similar to what CompoundButton.onDraw does.
            val verticalGravity = button.gravity and Gravity.VERTICAL_GRAVITY_MASK
            val w = drawable.intrinsicWidth
            val h = drawable.intrinsicHeight

            val top = when (verticalGravity) {
                Gravity.BOTTOM -> button.height - h
                Gravity.CENTER_VERTICAL -> (button.height - h) / 2
                else -> 0
            }

            val isLayoutRtl = MaterialCheckBox.LAYOUT_DIRECTION_RTL == button.getLayoutDirection()
            val left = if (isLayoutRtl) 0 else button.width - w
            val right = if (isLayoutRtl) w else button.width
            val bottom = top + h

            drawable.setBounds(left, top, right, bottom)
            button.background.setHotspotBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }
    }

    fun onDrawableStateChanged() {
        if (drawable != null && drawable.isStateful && drawable.setState(button.drawableState)) {
            button.invalidateDrawable(drawable)
        }
    }

    fun jumpDrawablesToCurrentState() {
        drawable?.jumpToCurrentState()
    }

    fun verifyDrawable(who: Drawable): Boolean = drawable == who
}
