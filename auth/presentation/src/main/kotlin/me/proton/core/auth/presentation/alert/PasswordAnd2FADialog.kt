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
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.proton.core.auth.presentation.databinding.DialogEnterPasswordBinding
import me.proton.core.auth.presentation.entity.PasswordAnd2FAInput
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.onClick

class PasswordAnd2FADialog : DialogFragment() {

    companion object {
        private const val ARG_SHOW_TWO_FA = "arg.showTwoFA"
        private const val ARG_SHOW_PASSWORD = "arg.showPassword"

        const val KEY_PASS_2FA_SET = "key.pass_2fa_set"
        const val BUNDLE_KEY_PASS_2FA_DATA = "bundle.pass_2fa_data"

        operator fun invoke(
            showPassword: Boolean,
            showTwoFA: Boolean
        ) = PasswordAnd2FADialog().apply {
            arguments = bundleOf(
                ARG_SHOW_TWO_FA to showTwoFA,
                ARG_SHOW_PASSWORD to showPassword
            )
        }
    }

    private val twoFAVisibility: Int by lazy {
        if (requireArguments().getBoolean(ARG_SHOW_TWO_FA)) View.VISIBLE else View.GONE
    }

    private val passwordVisibility: Int by lazy {
        if (requireArguments().getBoolean(ARG_SHOW_PASSWORD)) View.VISIBLE else View.GONE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val binding = DialogEnterPasswordBinding.inflate(LayoutInflater.from(requireContext()))
        binding.twoFA.visibility = twoFAVisibility
        binding.password.visibility = passwordVisibility
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
                                KEY_PASS_2FA_SET, bundleOf(
                                    BUNDLE_KEY_PASS_2FA_DATA to PasswordAnd2FAInput(
                                        password.text.toString(), twoFA.text.toString()
                                    )
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

        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
