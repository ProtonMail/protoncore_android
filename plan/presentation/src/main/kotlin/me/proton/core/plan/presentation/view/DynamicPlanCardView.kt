/*
 * Copyright (c) 2023 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.plan.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.google.android.material.card.MaterialCardView
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.databinding.DynamicPlanCardviewBinding.inflate
import me.proton.core.presentation.utils.onClick

class DynamicPlanCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding by lazy { inflate(LayoutInflater.from(context), this) }

    val cardView: MaterialCardView = binding.cardView
    val planView: DynamicPlanView = binding.planView

    init {
        context.withStyledAttributes(attrs, R.styleable.DynamicPlanCardView) {
            isCollapsed = getBoolean(R.styleable.DynamicPlanCardView_isCollapsed, false)
        }
        planView.isFocusable = false // Prevent child to take focus.
        planView.isClickable = false // Prevent child to take clicks.
        cardView.onClick {
            isCollapsed = !isCollapsed
        }
    }

    var isCollapsed: Boolean
        get() = planView.isCollapsed
        set(value) {
            planView.isCollapsed = value
            cardView.isChecked = !isCollapsed
        }
}
