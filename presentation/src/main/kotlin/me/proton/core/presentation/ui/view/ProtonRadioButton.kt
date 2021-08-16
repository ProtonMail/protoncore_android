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

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.google.android.material.radiobutton.MaterialRadioButton

/**
 * A radio button that draws its button at the end (on the right in LTR).
 */
open class ProtonRadioButton : MaterialRadioButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    // drawableStateChanged() is called by the constructor of the super class which is before this field is
    // initialized, that's why the field is nullable.
    @Suppress("LeakingThis", "RedundantNullableReturnType")
    private val helper: CompoundButtonPositionHelper? = CompoundButtonPositionHelper(this)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        helper?.onDraw(canvas)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        helper?.onDrawableStateChanged()
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        helper?.jumpDrawablesToCurrentState()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || helper?.verifyDrawable(who) ?: false
    }
}
