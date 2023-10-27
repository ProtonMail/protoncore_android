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

import android.view.View
import android.view.View.OnFocusChangeListener

class MultipleFocusChangeListener : OnFocusChangeListener, AdditionalOnFocusChangeListenerContract {
    private var additionalListener: OnFocusChangeListener? = null
    private var listener: OnFocusChangeListener? = null

    fun addListener(listener: OnFocusChangeListener?) {
        when (listener) {
            is AdditionalOnFocusChangeListener -> additionalListener = listener
            else -> this.listener = listener
        }
    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        listener?.onFocusChange(view, hasFocus)
        additionalListener?.onFocusChange(view, hasFocus)
    }

    override fun removeAdditionalOnFocusChangeListener() {
        additionalListener = null
    }
}

interface AdditionalOnFocusChangeListener : OnFocusChangeListener

interface AdditionalOnFocusChangeListenerContract {

    fun removeAdditionalOnFocusChangeListener()
}
