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

package me.proton.core.presentation.ui.alert

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink

/**
 * A base cancellable alert dialog.
 *
 * @param action the action to be executed on positive button response.
 * @author Dino Kadrikj.
 */
class ProtonCancellableAlertDialog(
    private val action: () -> Unit
) : DialogFragment() {

    companion object {
        private const val ARG_TITLE = "arg.title"
        private const val ARG_DESCRIPTION = "arg.description"
        private const val ARG_POSITIVE_BTN = "arg.positiveButton"

        operator fun invoke(
            title: String,
            description: String,
            positiveButton: String?,
            action: () -> Unit
        ) = ProtonCancellableAlertDialog(action).apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_DESCRIPTION to description,
                ARG_POSITIVE_BTN to positiveButton
            )
        }
    }

    private val title: String by lazy {
        requireArguments().getString(ARG_TITLE) ?: getString(R.string.presentation_alert_title)
    }

    private val description: String by lazy {
        requireArguments().getString(ARG_DESCRIPTION) ?: ""
    }

    private val positiveButton: String by lazy {
        requireArguments().getString(ARG_POSITIVE_BTN) ?: getString(R.string.presentation_alert_ok)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(description)
                .setTitle(title)
                // passing null to the listeners is a workaround to prevent the dialog to auto-dismiss on button click
                .setPositiveButton(positiveButton, null)
                .setNegativeButton(getString(R.string.presentation_alert_cancel), null)
                .setCancelable(true)
            val alertDialog = builder.create()
            alertDialog.apply {
                setOnShowListener {
                    // workaround to prevent the dialog to auto-dismiss on button click
                    getButton(AlertDialog.BUTTON_POSITIVE).apply {
                        isAllCaps = false
                        onClick {
                            action.invoke()
                        }
                    }
                    getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                        isAllCaps = false
                        onClick {
                            dismissAllowingStateLoss()
                        }
                    }
                }
                setCanceledOnTouchOutside(false)
            }
            alertDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
