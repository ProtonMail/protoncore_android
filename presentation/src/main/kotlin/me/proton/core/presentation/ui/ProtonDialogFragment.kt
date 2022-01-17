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

package me.proton.core.presentation.ui

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.annotation.LayoutRes
import androidx.fragment.app.DialogFragment

/**
 * Base Proton Fragment from which all project fragments should extend.
 */
abstract class ProtonDialogFragment : DialogFragment {
    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    /**
     * Provide fragment theme.
     * Default is null, in which case the paretn activity theme will be used.
     */
    protected open fun getStyleResource(): Int? = null
    protected abstract fun onBackPressed()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val theme = activity?.componentName?.let {
            activity?.packageManager?.getActivityInfo(it, 0)?.themeResource
        }
        setStyle(STYLE_NO_FRAME, getStyleResource() ?: theme ?: 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                this@ProtonDialogFragment.onBackPressed()
            }
        }
        // request a window without the title
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
}
