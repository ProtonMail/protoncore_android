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
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.ui.view.ProtonProgressButton
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePassword
import me.proton.core.presentation.utils.validatePasswordMinLength
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.databinding.FragmentPasswordManagementBinding
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class PasswordManagementFragment : ProtonFragment<FragmentPasswordManagementBinding>() {
    private val viewModel by viewModels<PasswordManagementViewModel>()

    private val input: SettingsInput by lazy {
        requireArguments().get(ARG_INPUT) as SettingsInput
    }

    private val userId: UserId by lazy { input.user }

    override fun layoutId() = R.layout.fragment_password_management

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.addOnBackPressedCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as PasswordManagementActivity).binding.toolbar.apply {
            setNavigationOnClickListener {
                finish()
            }
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
                is PasswordManagementViewModel.State.Idle -> {
                }
                is PasswordManagementViewModel.State.Error.Message -> showError(it.message)
                is PasswordManagementViewModel.State.Mode -> {
                    binding.mailboxPasswordGroup.visibility = if (it.twoPasswordMode) View.VISIBLE else View.GONE
                }
                is PasswordManagementViewModel.State.UpdatingLoginPassword ->
                    binding.saveLoginPasswordButton.showLoading(true)
                is PasswordManagementViewModel.State.Success.UpdatingLoginPassword ->
                    binding.saveLoginPasswordButton.showLoading(false)
                is PasswordManagementViewModel.State.UpdatingMailboxPassword ->
                    binding.saveMailboxPasswordButton.showLoading(true)
                is PasswordManagementViewModel.State.Success.UpdatingMailboxPassword ->
                    binding.saveMailboxPasswordButton.showLoading(false)
                is PasswordManagementViewModel.State.Error.UpdatingMailboxPassword ->
                    showError(getString(R.string.settings_change_password_error))
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    private fun onSaveLoginPasswordClicked() {
        hideKeyboard()
        with(binding) {
            newLoginPasswordInput.validatePasswordMinLength()
                .onFailure { newLoginPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
                .onSuccess { password ->
                    val confirmedPassword = confirmNewLoginPasswordInput.text.toString()
                    if (password == confirmedPassword) {
                        viewModel.updateLoginPassword(
                            userId,
                            currentLoginPasswordInput.text.toString(),
                            confirmedPassword,
                            ""
                        )
                    } else {
                        confirmNewLoginPasswordInput.setInputError(getString(R.string.auth_signup_error_passwords_match))
                    }
                }
        }
    }

    private fun onSaveMailboxPasswordClicked() {
        hideKeyboard()
        with(binding) {
            newMailboxPasswordInput.validatePasswordMinLength()
                .onFailure { newMailboxPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
                .onSuccess { password ->
                    val confirmedPassword = confirmNewMailboxPasswordInput.text.toString()
                    if (password == confirmedPassword) {
                        viewModel.updateMailboxPassword(
                            userId,
                            currentMailboxPasswordInput.text.toString(),
                            confirmedPassword
                        )
                    } else {
                        confirmNewMailboxPasswordInput.setInputError(getString(R.string.auth_signup_error_passwords_match))
                    }
                }
        }
    }

    private fun finish() {
        parentFragmentManager.setFragmentResult(
            KEY_UPDATE_RESULT, bundleOf(BUNDLE_KEY_RESULT to null)
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
                setInputError(getString(R.string.auth_signup_validation_password_length))
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

    private fun showError(message: String?) {
        binding.saveLoginPasswordButton.showLoading(false)
        binding.saveMailboxPasswordButton.showLoading(false)
        binding.root.errorSnack(
            message = message ?: getString(R.string.settings_general_error)
        )
    }

    companion object {
        const val KEY_UPDATE_RESULT = "key.update_result"
        const val BUNDLE_KEY_RESULT = "bundle.update_result"
        const val ARG_INPUT = "arg.settingsInput"

        operator fun invoke(input: SettingsInput) = PasswordManagementFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}