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

package me.proton.core.usersettings.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountrecovery.presentation.compose.entity.AccountRecoveryDialogInput
import me.proton.core.accountrecovery.presentation.compose.ui.AccountRecoveryDialogActivity
import me.proton.core.accountrecovery.presentation.compose.view.AccountRecoveryInfo
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonSecureFragment
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.ui.view.ProtonProgressButton
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePassword
import me.proton.core.presentation.utils.validatePasswordMatch
import me.proton.core.presentation.utils.validatePasswordMinLength
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.databinding.FragmentPasswordManagementBinding
import me.proton.core.usersettings.presentation.entity.PasswordManagementResult
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.Action
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel.PasswordType

@AndroidEntryPoint
class PasswordManagementFragment : ProtonSecureFragment(R.layout.fragment_password_management) {

    private val viewModel by viewModels<PasswordManagementViewModel>()
    private val binding by viewBinding(FragmentPasswordManagementBinding::bind)

    private val input: SettingsInput by lazy {
        requireArguments().get(ARG_INPUT) as SettingsInput
    }

    private val userId: UserId by lazy { input.user }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.addOnBackPressedCallback {
            finish()
        }
        viewModel.perform(Action.ObserveState(userId))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as PasswordManagementActivity).binding.toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
        binding.apply {
            accountRecoveryInfo.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent { AccountRecoveryInfo(onOpenDialog = { onOpenDialog() }) }
            }
            currentLoginPasswordInput.passwordValidation(false)
            newLoginPasswordInput.passwordValidation()
            confirmNewLoginPasswordInput.passwordValidation()
            currentMailboxPasswordInput.passwordValidation(false)
            newMailboxPasswordInput.passwordValidation()
            confirmNewMailboxPasswordInput.passwordValidation()

            saveLoginPasswordButton.onClick {
                onSaveLoginPasswordClicked()
            }
            saveMailboxPasswordButton.onClick {
                onSaveMailboxPasswordClicked()
            }
        }

        val twoFactorLauncher = childFragmentManager.registerShowPasswordDialogResultLauncher(this) { result ->
            if (result != null) {
                viewModel.perform(Action.SetTwoFactor(userId, result.twoFA))
            } else {
                viewModel.perform(Action.CancelTwoFactor(userId))
            }
        }

        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is PasswordManagementViewModel.State.Idle -> Unit
                    is PasswordManagementViewModel.State.Loading -> Unit
                    is PasswordManagementViewModel.State.ChangePassword -> {
                        binding.progress.visibility = View.GONE
                        binding.loginPasswordGroup.isVisible = it.loginPasswordAvailable
                        binding.mailboxPasswordGroup.isVisible = it.mailboxPasswordAvailable
                    }

                    is PasswordManagementViewModel.State.UpdatingPassword -> {
                        binding.saveLoginPasswordButton.showLoading(true)
                        binding.saveMailboxPasswordButton.showLoading(true)
                    }

                    is PasswordManagementViewModel.State.TwoFactorNeeded -> {
                        twoFactorLauncher.show(ShowPasswordInput(showPassword = false, showTwoFA = true))
                    }
                    is PasswordManagementViewModel.State.Success -> {
                        resetLoginPasswordInput()
                        resetMailboxPasswordInput()
                        showSuccess()
                    }

                    is PasswordManagementViewModel.State.Error -> showError(it.error.getUserMessage(resources))
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onOpenDialog() {
        AccountRecoveryDialogActivity.start(requireContext(), AccountRecoveryDialogInput(userId.id))
    }

    private fun resetLoginPasswordInput() = with(binding) {
        saveLoginPasswordButton.showLoading(false)
        currentLoginPasswordInput.text = ""
        newLoginPasswordInput.text = ""
        confirmNewLoginPasswordInput.text = ""
    }

    private fun resetMailboxPasswordInput() = with(binding) {
        saveMailboxPasswordButton.showLoading(false)
        currentMailboxPasswordInput.text = ""
        newMailboxPasswordInput.text = ""
        confirmNewMailboxPasswordInput.text = ""
    }

    private fun onSaveLoginPasswordClicked() = with(binding) {
        hideKeyboard()
        currentLoginPasswordInput.validatePassword()
            .onFailure { currentLoginPasswordInput.setInputError(getString(R.string.auth_signup_validation_password)) }
            .onSuccess { onLoginPasswordValidationSuccess() }
    }

    private fun onLoginPasswordValidationSuccess() = with(binding) {
        newLoginPasswordInput.validatePasswordMinLength()
            .onFailure { newLoginPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
            .onSuccess { onNewLoginPasswordValidationSuccess() }
    }

    private fun onNewLoginPasswordValidationSuccess() = with(binding) {
        val confirmPassword = confirmNewLoginPasswordInput.text.toString()
        newLoginPasswordInput.validatePasswordMatch(confirmPassword)
            .onFailure { confirmNewLoginPasswordInput.setInputError(getString(R.string.auth_signup_error_passwords_do_not_match)) }
            .onSuccess { onLoginPasswordConfirmed(it) }
    }

    private fun onLoginPasswordConfirmed(confirmedPassword: String) = with(binding) {
        viewModel.perform(
            Action.UpdatePassword(
                userId = userId,
                type = if (viewModel.userSettings?.password?.mode == 2) PasswordType.Login else PasswordType.Both,
                password = currentLoginPasswordInput.text.toString(),
                newPassword = confirmedPassword
            )
        )
    }

    private fun onSaveMailboxPasswordClicked() = with(binding) {
        hideKeyboard()
        currentMailboxPasswordInput.validatePassword()
            .onFailure { currentMailboxPasswordInput.setInputError(getString(R.string.auth_signup_validation_password)) }
            .onSuccess { onMailboxPasswordValidationSuccess() }
    }

    private fun onMailboxPasswordValidationSuccess() = with(binding) {
        newMailboxPasswordInput.validatePasswordMinLength()
            .onFailure { newMailboxPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
            .onSuccess { onNewMailboxPasswordValidationSuccess() }
    }

    private fun onNewMailboxPasswordValidationSuccess() = with(binding) {
        val confirmPassword = confirmNewMailboxPasswordInput.text.toString()
        newMailboxPasswordInput.validatePasswordMatch(confirmPassword)
            .onFailure { confirmNewMailboxPasswordInput.setInputError(getString(R.string.auth_signup_error_passwords_do_not_match)) }
            .onSuccess { onMailboxPasswordConfirmed(it) }
    }

    private fun onMailboxPasswordConfirmed(confirmedPassword: String) = with(binding) {
        viewModel.perform(
            Action.UpdatePassword(
                userId = userId,
                type = PasswordType.Mailbox,
                password = currentMailboxPasswordInput.text.toString(),
                newPassword = confirmedPassword
            )
        )
    }

    private fun finish(success: Boolean = false) {
        parentFragmentManager.setFragmentResult(
            KEY_UPDATE_RESULT, bundleOf(ARG_UPDATE_RESULT to PasswordManagementResult(success))
        )
        parentFragmentManager.popBackStackImmediate()
    }

    private fun ProtonInput.passwordValidation(validateLength: Boolean = true) {
        setOnFocusLostListener { _, _ ->
            val validation = if (validateLength) {
                validatePasswordMinLength()
            } else {
                validatePassword()
            }
            validation.onFailure {
                if (validateLength) {
                    setInputError(getString(R.string.auth_signup_validation_password_length))
                } else {
                    setInputError(getString(R.string.auth_signup_validation_password))
                }
            }.onSuccess { clearInputError() }
        }
    }

    private fun ProtonProgressButton.showLoading(loading: Boolean) {
        if (loading) {
            setLoading()
        } else {
            setIdle()
        }
    }

    private fun showSuccess() {
        finish(success = true)
    }

    private fun showError(message: String?) {
        binding.saveLoginPasswordButton.showLoading(false)
        binding.saveMailboxPasswordButton.showLoading(false)
        binding.root.errorSnack(
            message = message ?: getString(R.string.settings_general_error)
        )
    }

    companion object {
        const val KEY_UPDATE_RESULT = "key.update_result"
        const val ARG_UPDATE_RESULT = "bundle.update_result"
        const val ARG_INPUT = "arg.settingsInput"

        operator fun invoke(input: SettingsInput) = PasswordManagementFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
