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

package me.proton.core.auth.presentation.alert.confirmpass

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.DialogEnterPasswordBinding
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.auth.presentation.viewmodel.ConfirmPasswordDialogViewModel
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.ScreenContentProtector
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.showToast
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class ConfirmPasswordDialog : ProtonDialogFragment(R.layout.dialog_enter_password) {

    private val viewModel by viewModels<ConfirmPasswordDialogViewModel>()

    private val screenProtector = ScreenContentProtector(ProtectScreenConfiguration())

    private val input: ConfirmPasswordInput by lazy {
        requireNotNull(requireArguments().getParcelable(ARG_INPUT))
    }

    private val missingScope: Scope by lazy {
        Scope.getByValue(input.missingScope) ?: Scope.LOCKED
    }

    override fun onBackPressed() {
        setResultAndDismiss(null)
    }

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

        viewModel.state.onEach {
            when (it) {
                is ConfirmPasswordDialogViewModel.State.Success ->
                    setResultAndDismiss(confirmed = true)
                is ConfirmPasswordDialogViewModel.State.ProcessingObtainScope ->
                    requireContext().showToast("Processing")
                is ConfirmPasswordDialogViewModel.State.ProcessingSecondFactor -> {
                    // noop
                }
                is ConfirmPasswordDialogViewModel.State.Error.Message ->
                    setResultAndDismiss(confirmed = false)
                is ConfirmPasswordDialogViewModel.State.Idle -> Unit
                is ConfirmPasswordDialogViewModel.State.SecondFactorResult -> {
                    binding.twoFA.visibility = if (it.needed) VISIBLE else GONE
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
        viewModel.isSecondFactorNeeded(missingScope)

        return alertDialog.apply {
            setOnShowListener {
                // workaround to prevent the dialog to auto-dismiss on button click
                getButton(AlertDialog.BUTTON_POSITIVE).apply {
                    isAllCaps = false
                    onClick {
                        val password = binding.password.text.toString()
                        val twoFactorCode = binding.twoFA.text.toString()
                        when (missingScope) {
                            Scope.PASSWORD -> viewModel.unlockPassword(
                                password,
                                if (twoFactorCode.isEmpty()) null else twoFactorCode
                            )
                            Scope.LOCKED -> viewModel.unlock(password)
                        }.exhaustive
                    }
                }
                getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                    isAllCaps = false
                    onClick {
                        setResultAndDismiss(null)
                    }
                }
            }
            setCanceledOnTouchOutside(false)
        }
    }

    private fun setResultAndDismiss(confirmed: Boolean?) {
        parentFragmentManager.setFragmentResult(
            KEY_PASS_2FA_SET,
            bundleOf(
                BUNDLE_KEY_PASS_2FA_DATA to ConfirmPasswordResult(confirmed)
            )
        )

        dismissAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        screenProtector.unprotect(requireActivity())
    }

    companion object {
        private const val ARG_INPUT = "arg.confirmPasswordInput"

        const val KEY_PASS_2FA_SET = "key.pass_2fa_set"
        const val BUNDLE_KEY_PASS_2FA_DATA = "bundle.pass_2fa_data"

        operator fun invoke(
            input: ConfirmPasswordInput
        ) = ConfirmPasswordDialog().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
