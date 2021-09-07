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

/**
 * A base cancellable alert dialog.
 */
class ProtonCancellableAlertDialog : DialogFragment() {

    companion object {
        private const val ARG_TITLE = "arg.title"
        private const val ARG_DESCRIPTION = "arg.description"
        private const val ARG_POSITIVE_BTN = "arg.positiveButton"
        private const val ARG_NEGATIVE_BTN = "arg.negativeButton"

        const val KEY_ACTION_DONE = "key.action_done"

        operator fun invoke(
            title: String,
            description: String,
            positiveButton: String?,
            negativeButton: String? = null
        ) = ProtonCancellableAlertDialog().apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_DESCRIPTION to description,
                ARG_POSITIVE_BTN to positiveButton,
                ARG_NEGATIVE_BTN to negativeButton
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

    private val negativeButton: String by lazy {
        requireArguments().getString(ARG_NEGATIVE_BTN) ?: getString(R.string.presentation_alert_cancel)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(description)
                .setTitle(title)
                // passing null to the listeners is a workaround to prevent the dialog to auto-dismiss on button click
                .setPositiveButton(positiveButton, null)
                .setNegativeButton(negativeButton, null)
                .setCancelable(true)
            val alertDialog = builder.create()
            alertDialog.apply {
                setOnShowListener {
                    // workaround to prevent the dialog to auto-dismiss on button click
                    getButton(AlertDialog.BUTTON_POSITIVE).apply {
                        isAllCaps = false
                        onClick {
                            parentFragmentManager.setFragmentResult(
                                KEY_ACTION_DONE, bundleOf()
                            )
                            dismissAllowingStateLoss()
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
