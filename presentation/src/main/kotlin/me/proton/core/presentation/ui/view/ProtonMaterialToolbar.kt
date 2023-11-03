/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.presentation.ui.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.appbar.MaterialToolbar

class ProtonMaterialToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.toolbarStyle
) : MaterialToolbar(context, attrs, defStyleAttr), AdditionalOnMenuItemClickListenerContract {
    private val multipleMenuItemClickListener by lazy {
        MultipleMenuItemClickListener().also {
            super.setOnMenuItemClickListener(it)
        }
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        multipleMenuItemClickListener.setListener(listener)
    }

    override fun setAdditionalOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        multipleMenuItemClickListener.setAdditionalOnMenuItemClickListener(listener)
    }
}
