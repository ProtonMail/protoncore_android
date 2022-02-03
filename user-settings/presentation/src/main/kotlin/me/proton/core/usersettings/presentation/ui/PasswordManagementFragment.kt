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
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonSecureFragment
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.ui.view.ProtonProgressButton
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.getLocalizedMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePassword
import me.proton.core.presentation.utils.validatePasswordMinLength
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.databinding.FragmentPasswordManagementBinding
import me.proton.core.usersettings.presentation.entity.PasswordManagementResult
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel
import me.proton.core.util.kotlin.exhaustive

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as PasswordManagementActivity).binding.toolbar.apply {
            setNavigationOnClickListener { finish() }
        }
        viewModel.init(userId)
        binding.apply {
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

        viewModel.state.onEach {
            when (it) {
                is PasswordManagementViewModel.State.Idle -> Unit
                is PasswordManagementViewModel.State.Mode -> {
                    with(binding) {
                        progress.visibility = View.GONE
                        loginPasswordGroup.visibility = View.VISIBLE
                        mailboxPasswordGroup.visibility = if (it.twoPasswordMode) View.VISIBLE else View.GONE
                    }
                }
                is PasswordManagementViewModel.State.Error.General -> showError(it.error.getLocalizedMessage(resources))
                is PasswordManagementViewModel.State.Error.UpdatingSinglePassModePassword,
                is PasswordManagementViewModel.State.Error.UpdatingMailboxPassword ->
                    showError(getString(R.string.settings_change_password_error))
                is PasswordManagementViewModel.State.UpdatingLoginPassword ->
                    binding.saveLoginPasswordButton.showLoading(true)
                is PasswordManagementViewModel.State.UpdatingMailboxPassword ->
                    binding.saveMailboxPasswordButton.showLoading(true)
                is PasswordManagementViewModel.State.UpdatingSinglePassModePassword ->
                    binding.saveLoginPasswordButton.showLoading(true)
                is PasswordManagementViewModel.State.Success.UpdatingSinglePassModePassword,
                is PasswordManagementViewModel.State.Success.UpdatingLoginPassword -> {
                    resetLoginPasswordInput()
                    showSuccess()
                }
                is PasswordManagementViewModel.State.Success.UpdatingMailboxPassword -> {
                    resetMailboxPasswordInput()
                    showSuccess()
                }
            }.exhaustive
        }.launchIn(lifecycleScope)
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

    private fun onSaveLoginPasswordClicked() {
        hideKeyboard()
        with(binding) {
            currentLoginPasswordInput.validatePassword()
                .onFailure {
                    currentLoginPasswordInput.setInputError(getString(R.string.auth_signup_validation_password))
                }
                .onSuccess { onLoginPasswordValidationSuccess() }
        }
    }

    private fun onLoginPasswordValidationSuccess() = with(binding) {
        newLoginPasswordInput.validatePasswordMinLength()
            .onFailure {
                newLoginPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length))
            }
            .onSuccess { password ->
                when (val confirmedPassword = confirmNewLoginPasswordInput.text.toString()) {
                    password -> onLoginPasswordConfirmed(confirmedPassword)
                    else -> confirmNewLoginPasswordInput.setInputError(
                        getString(R.string.auth_signup_error_passwords_do_not_match)
                    )
                }
            }
    }

    private fun onLoginPasswordConfirmed(confirmedPassword: String) = with(binding) {
        if (viewModel.secondFactorEnabled == true) {
            childFragmentManager.apply {
                registerShowPasswordDialogResultLauncher(
                    this@PasswordManagementFragment
                ) { result ->
                    if (result != null) {
                        viewModel.updateLoginPassword(
                            userId = userId,
                            password = binding.currentLoginPasswordInput.text.toString(),
                            newPassword = binding.confirmNewLoginPasswordInput.text.toString(),
                            secondFactorCode = result.twoFA
                        )
                    }
                }.show(ShowPasswordInput(showPassword = false, showTwoFA = true))
            }
        } else {
            viewModel.updateLoginPassword(
                userId = userId,
                password = currentLoginPasswordInput.text.toString(),
                newPassword = confirmedPassword
            )
        }
    }

    private fun onSaveMailboxPasswordClicked() {
        hideKeyboard()
        with(binding) {
            currentMailboxPasswordInput.validatePassword()
                .onFailure {
                    currentMailboxPasswordInput.setInputError(getString(R.string.auth_signup_validation_password))
                }
                .onSuccess { onMailboxPasswordValidationSuccess() }
        }
    }

    private fun onMailboxPasswordValidationSuccess() = with(binding) {
        newMailboxPasswordInput.validatePasswordMinLength()
            .onFailure {
                newMailboxPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length))
            }
            .onSuccess { password ->
                when (val confirmedPassword = confirmNewMailboxPasswordInput.text.toString()) {
                    password -> onMailboxPasswordConfirmed(confirmedPassword)
                    else -> confirmNewMailboxPasswordInput.setInputError(
                        getString(R.string.auth_signup_error_passwords_do_not_match)
                    )
                }
            }
    }

    private fun onMailboxPasswordConfirmed(confirmedPassword: String) = with(binding) {
        if (viewModel.secondFactorEnabled == true) {
            childFragmentManager.apply {
                registerShowPasswordDialogResultLauncher(
                    this@PasswordManagementFragment,
                    onResultMailboxPassword = { result ->
                        if (result != null) {
                            viewModel.updateMailboxPassword(
                                userId = userId,
                                loginPassword = binding.currentMailboxPasswordInput.text.toString(),
                                newMailboxPassword = binding.confirmNewMailboxPasswordInput.text.toString(),
                                secondFactorCode = result.twoFA
                            )
                        }
                    }
                ).show(ShowPasswordInput(showPassword = false, showTwoFA = true))
            }
        } else {
            viewModel.updateMailboxPassword(
                userId = userId,
                loginPassword = currentMailboxPasswordInput.text.toString(),
                newMailboxPassword = confirmedPassword
            )
        }
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
