/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation.alert

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.proton.core.auth.presentation.databinding.DialogEnterPasswordBinding
import me.proton.core.auth.presentation.entity.PasswordInput
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.ScreenContentProtector
import me.proton.core.presentation.utils.onClick

class ConfirmPasswordInputDialog : DialogFragment() {

    companion object {
        const val KEY_PASS_SET = "key.pass_set"
        const val BUNDLE_KEY_PASS_DATA = "bundle.pass_data"

        operator fun invoke() = ConfirmPasswordInputDialog()
    }

    private val screenProtector = ScreenContentProtector(ProtectScreenConfiguration())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        screenProtector.protect(requireActivity())

        val binding = DialogEnterPasswordBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.presentation_authenticate)
            // passing null to the listeners is a workaround to prevent the dialog to auto-dismiss on button click
            .setPositiveButton(R.string.presentation_alert_enter, null)
            .setNegativeButton(R.string.presentation_alert_cancel, null)
            .setView(binding.root)
        val alertDialog = builder.create()

        return alertDialog.apply {
            setOnShowListener {
                // workaround to prevent the dialog to auto-dismiss on button click
                getButton(AlertDialog.BUTTON_POSITIVE).apply {
                    isAllCaps = false
                    onClick {
                        with(binding) {
                            parentFragmentManager.setFragmentResult(
                                KEY_PASS_SET, bundleOf(
                                    BUNDLE_KEY_PASS_DATA to PasswordInput(password.text.toString())
                                )
                            )

                            dismissAllowingStateLoss()
                        }
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
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        screenProtector.unprotect(requireActivity())
    }
}
