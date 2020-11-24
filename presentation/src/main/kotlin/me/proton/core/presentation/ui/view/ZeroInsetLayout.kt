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

package me.proton.core.presentation.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ZeroInsetLayout : FrameLayout {

    constructor(context: Context) :
        super(context)

    constructor(context: Context, attrs: AttributeSet?) :
        super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes)

    init {
        fitsSystemWindows = true
        ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
            WindowInsetsCompat.Builder()
                .setSystemWindowInsets(Insets.of(0, 0, 0, insets.systemWindowInsets.bottom))
                .build()
                .apply { ViewCompat.onApplyWindowInsets(view, this) }
            insets
        }
    }
}
