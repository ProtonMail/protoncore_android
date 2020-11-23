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
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.presentation.utils.openMarketLink

/**
 * Presents non dismissable dialog to the user, informing of the no longer supported application version.
 * @author Dino Kadrikj.
 */
class ForceUpdateDialog : DialogFragment() {

    companion object {
        private const val ARG_LEAR_MORE_URL = "arg.learnMoreUrl"
        private const val ARG_API_ERROR_MESSAGE = "arg.apiErrorMessage"

        operator fun invoke(
            apiErrorMessage: String,
            learnMoreURL: String? = null
        ) = ForceUpdateDialog().apply {
            arguments = bundleOf(
                ARG_LEAR_MORE_URL to learnMoreURL,
                ARG_API_ERROR_MESSAGE to apiErrorMessage
            )
        }
    }

    private val learnMoreURL: String by lazy {
        requireArguments().getString(ARG_LEAR_MORE_URL) ?: getString(R.string.force_update_link)
    }

    private val apiErrorMessage: String by lazy {
        requireArguments().getString(ARG_API_ERROR_MESSAGE)!! // this one is mandatory
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setMessage(apiErrorMessage)
                .setTitle(R.string.presentation_force_update_title)
                // passing null to the listeners is a workaround to prevent the dialog to auto-dismiss on button click
                .setPositiveButton(R.string.presentation_force_update_update, null)
                .setNeutralButton(R.string.presentation_force_update_learn_more, null)
                .setCancelable(false)
            val alertDialog = builder.create()
            alertDialog.apply {
                setOnShowListener {
                    // workaround to prevent the dialog to auto-dismiss on button click
                    val positiveButton = getButton(AlertDialog.BUTTON_POSITIVE)
                    val neutralButton = getButton(AlertDialog.BUTTON_NEUTRAL)
                    positiveButton.onClick {
                        requireContext().openMarketLink()
                    }
                    neutralButton.onClick {
                        requireContext().openBrowserLink(learnMoreURL)
                    }
                }
                setCanceledOnTouchOutside(false)
            }
            alertDialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onResume() {
        super.onResume()
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action != KeyEvent.ACTION_DOWN) {
                requireActivity().finish()
                true
            } else false
        }
    }
}
