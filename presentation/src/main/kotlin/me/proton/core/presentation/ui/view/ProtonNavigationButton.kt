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
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.doOnApplyWindowInsets

class ProtonNavigationButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.style.ProtonButton_Navigation
) : ProtonButton(context, attrs, defStyleAttr) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        doOnApplyWindowInsets { _, insets, initialMargin, _ ->
            updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.stableInsetTop + initialMargin.top
                marginStart = insets.stableInsetLeft + initialMargin.start
                marginEnd = insets.stableInsetRight + initialMargin.end
                bottomMargin = insets.stableInsetBottom + initialMargin.bottom
            }
        }
    }
}
