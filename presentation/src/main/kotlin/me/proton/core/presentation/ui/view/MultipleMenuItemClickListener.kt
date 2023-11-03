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

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

class MultipleMenuItemClickListener : Toolbar.OnMenuItemClickListener,
    AdditionalOnMenuItemClickListenerContract {
    private var additionalListener: Toolbar.OnMenuItemClickListener? = null
    private var mainListener: Toolbar.OnMenuItemClickListener? = null

    fun setListener(listener: Toolbar.OnMenuItemClickListener?) {
        mainListener = listener
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        additionalListener?.onMenuItemClick(item)
        return mainListener?.onMenuItemClick(item) ?: false
    }

    override fun setAdditionalOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener?) {
        additionalListener = listener
    }
}

interface AdditionalOnMenuItemClickListenerContract {
    fun setAdditionalOnMenuItemClickListener(listener: Toolbar.OnMenuItemClickListener?)
}
