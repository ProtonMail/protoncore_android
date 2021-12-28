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
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.ScreenContentProtector
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.onClick
import me.proton.core.util.kotlin.exhaustive
import java.lang.IllegalArgumentException

/**
 * This dialog handles only [Scope.PASSWORD] or [Scope.LOCKED]. Any other scope will be ignored.
 */
@AndroidEntryPoint
class ConfirmPasswordDialog : ProtonDialogFragment() {

    private val viewModel by viewModels<ConfirmPasswordDialogViewModel>()

    private val screenProtector = ScreenContentProtector(ProtectScreenConfiguration())

    private val input: ConfirmPasswordInput by lazy {
        requireNotNull(requireArguments().getParcelable(ARG_INPUT))
    }

    private val missingScopes: List<Scope> by lazy {
        input.missingScopes.mapNotNull {
            Scope.getByValue(it)
        }
    }

    private val missingScope: Scope by lazy {
        // if we are required password scope we will try to obtain only that one
        when {
            missingScopes.firstOrNull { it == Scope.PASSWORD } != null -> {
                Scope.PASSWORD
            }
            missingScopes.firstOrNull { it == Scope.LOCKED } != null -> {
                Scope.LOCKED
            }
            else -> throw IllegalArgumentException("Unrecognized scope!")
        }
    }

    private val userId: UserId by lazy {
        UserId(input.userId)
    }

    override fun onBackPressed() {
        setResultAndDismiss(null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        screenProtector.protect(requireActivity())

        val binding = DialogEnterPasswordBinding.inflate(LayoutInflater.from(requireContext()))
        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.presentation_signin_to_continue)
            .setView(binding.root)
        val alertDialog = builder.create()

        viewModel.state.onEach {
            when (it) {
                is ConfirmPasswordDialogViewModel.State.Success -> setResultAndDismiss(it.state)
                is ConfirmPasswordDialogViewModel.State.ProcessingObtainScope ->
                    binding.enterButton.setLoading()
                is ConfirmPasswordDialogViewModel.State.ProcessingSecondFactor -> {
                    // noop
                }
                is ConfirmPasswordDialogViewModel.State.Error.Message -> {
                    setResultAndDismiss(MissingScopeState.ScopeObtainFailed)
                    binding.enterButton.setIdle()
                }
                is ConfirmPasswordDialogViewModel.State.Idle -> Unit
                is ConfirmPasswordDialogViewModel.State.SecondFactorResult -> {
                    binding.twoFA.visibility = if (it.needed) VISIBLE else GONE
                }
                is ConfirmPasswordDialogViewModel.State.Error.InvalidAccount -> {
                    context.errorToast(getString(R.string.auth_account_not_found_error))
                }
            }.exhaustive
        }.launchIn(lifecycleScope)

        binding.enterButton.onClick {
            val password = binding.password.text.toString()
            val twoFactorCode = binding.twoFA.text.toString()
            when (missingScope) {
                Scope.PASSWORD -> viewModel.unlock(
                    userId,
                    missingScope,
                    password,
                    if (twoFactorCode.isEmpty()) null else twoFactorCode
                )
                Scope.LOCKED -> viewModel.unlock(userId, missingScope, password, null)
            }.exhaustive
        }

        binding.cancelButton.onClick {
            setResultAndDismiss(null)
        }

        viewModel.checkForSecondFactorInput(userId, missingScope)

        return alertDialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    private fun setResultAndDismiss(state: MissingScopeState?) {
        val obtained = state is MissingScopeState.ScopeObtainSuccess

        viewModel.onConfirmPasswordResult(state).invokeOnCompletion {
            parentFragmentManager.setFragmentResult(
                CONFIRM_PASS_SET,
                bundleOf(
                    BUNDLE_CONFIRM_PASS_DATA to ConfirmPasswordResult(obtained)
                )
            )
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        screenProtector.unprotect(requireActivity())
    }

    companion object {
        private const val ARG_INPUT = "arg.confirmPasswordInput"

        const val CONFIRM_PASS_SET = "key.confirm_pass_set"
        const val BUNDLE_CONFIRM_PASS_DATA = "bundle.confirm_pass_data"

        operator fun invoke(
            input: ConfirmPasswordInput
        ) = ConfirmPasswordDialog().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
