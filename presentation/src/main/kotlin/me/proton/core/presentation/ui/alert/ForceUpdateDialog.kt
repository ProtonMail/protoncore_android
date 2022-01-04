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
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.presentation.utils.openMarketLink

/**
 * Presents non dismissable dialog to the user, informing of the no longer supported application version.
 * @author Dino Kadrikj.
 * @see ForceUpdateActivity
 */
class ForceUpdateDialog : DialogFragment() {
    private val learnMoreURL: String by lazy {
        requireArguments().getString(ARG_LEAR_MORE_URL) ?: getString(R.string.force_update_link)
    }

    private val apiErrorMessage: String by lazy {
        requireArguments().getString(ARG_API_ERROR_MESSAGE)!! // this one is mandatory
    }

    private val finishOnBackPress: Boolean by lazy {
        requireArguments().getBoolean(ARG_FINISH_ON_BACK_PRESS, DEFAULT_FINISH_ON_BACK_PRESS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            builder.setMessage(apiErrorMessage)
                .setTitle(R.string.presentation_force_update_title)
                // passing null to the listeners is a workaround to prevent the dialog to auto-dismiss on button click
                .setPositiveButton(R.string.presentation_force_update_update, null)
                .setNeutralButton(R.string.presentation_force_update_learn_more, null)
            val alertDialog = builder.create()
            alertDialog.apply {
                setOnShowListener {
                    // workaround to prevent the dialog to auto-dismiss on button click
                    getButton(AlertDialog.BUTTON_POSITIVE).apply {
                        isAllCaps = false
                        onClick {
                            requireContext().openMarketLink()
                        }
                    }
                    getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                        isAllCaps = false
                        onClick {
                            requireContext().openBrowserLink(learnMoreURL)
                        }
                    }
                }
            }
            alertDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onResume() {
        super.onResume()
        if (finishOnBackPress) {
            dialog?.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action != KeyEvent.ACTION_DOWN) {
                    requireActivity().finish()
                    true
                } else false
            }
        }
    }

    companion object {
        private const val ARG_LEAR_MORE_URL = "arg.learnMoreUrl"
        private const val ARG_API_ERROR_MESSAGE = "arg.apiErrorMessage"
        private const val ARG_FINISH_ON_BACK_PRESS = "arg.finishOnBackPress"
        private const val DEFAULT_FINISH_ON_BACK_PRESS = true

        operator fun invoke(
            apiErrorMessage: String,
            learnMoreURL: String? = null,
            finishActivityOnBackPress: Boolean = DEFAULT_FINISH_ON_BACK_PRESS
        ) = ForceUpdateDialog().apply {
            arguments = bundleOf(
                ARG_LEAR_MORE_URL to learnMoreURL,
                ARG_API_ERROR_MESSAGE to apiErrorMessage,
                ARG_FINISH_ON_BACK_PRESS to finishActivityOnBackPress
            )
        }
    }
}
