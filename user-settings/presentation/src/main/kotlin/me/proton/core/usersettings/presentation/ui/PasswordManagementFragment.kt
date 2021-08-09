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
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.ui.view.ProtonInput
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePasswordMinLength
import me.proton.core.usersettings.presentation.R
import me.proton.core.usersettings.presentation.databinding.FragmentUpdatePasswordBinding
import me.proton.core.usersettings.presentation.entity.SettingsInput
import me.proton.core.usersettings.presentation.viewmodel.PasswordManagementViewModel

@AndroidEntryPoint
class PasswordManagementFragment : ProtonFragment<FragmentUpdatePasswordBinding>() {
    private val viewModel by viewModels<PasswordManagementViewModel>()

    private val input: SettingsInput by lazy {
        requireArguments().get(ARG_INPUT) as SettingsInput
    }

    private val userId: UserId by lazy { input.user }

    override fun layoutId() = R.layout.fragment_update_password

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
        binding.apply {
            currentLoginPasswordInput.validatePassword()
            newLoginPasswordInput.validatePassword()
            confirmNewLoginPasswordInput.validatePassword()
            currentMailboxPasswordInput.validatePassword()
            newMailboxPasswordInput.validatePassword()
            confirmNewMailboxPasswordInput.validatePassword()

            saveLoginPasswordButton.onClick {
                onSaveLoginPasswordClicked()
            }
            saveMailboxPasswordButton.onClick {
                onSaveMailboxPasswordClicked()
            }
        }
    }

    private fun onSaveLoginPasswordClicked() {
        hideKeyboard()
        with(binding) {
            newLoginPasswordInput.validatePasswordMinLength()
                .onFailure { newLoginPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
                .onSuccess { password ->
                    val confirmedPassword = confirmNewLoginPasswordInput.text.toString()
                    if (password == confirmedPassword) {
                        viewModel.updateLoginPassword(userId, currentLoginPasswordInput.text.toString(), confirmedPassword, "")
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
                        viewModel.updateMailboxPassword(userId, "", "", "")
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

    private fun ProtonInput.validatePassword() {
        setOnFocusLostListener { _, _ ->
            validatePasswordMinLength()
                .onFailure {
                    setInputError(getString(R.string.auth_signup_validation_password_length))
                }
                .onSuccess { clearInputError() }
        }
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