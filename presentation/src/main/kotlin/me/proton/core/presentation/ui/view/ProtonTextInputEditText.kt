/*
 * Copyright (c) 2022 Proton Technologies AG
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
import com.google.android.material.textfield.TextInputEditText

open class ProtonTextInputEditText : TextInputEditText, AdditionalOnFocusChangeListenerContract {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val multipleFocusChangeListeners = MultipleFocusChangeListener()

    init {
        super.setOnFocusChangeListener(multipleFocusChangeListeners)
    }

    override fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        multipleFocusChangeListeners.addListener(listener)
    }

    override fun removeAdditionalOnFocusChangeListener() {
        multipleFocusChangeListeners.removeAdditionalOnFocusChangeListener()
    }
}