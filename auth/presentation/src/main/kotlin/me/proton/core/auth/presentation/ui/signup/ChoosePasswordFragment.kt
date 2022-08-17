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

package me.proton.core.auth.presentation.ui.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupChoosePasswordBinding
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePasswordMinLength
import me.proton.core.presentation.utils.viewBinding

@AndroidEntryPoint
class ChoosePasswordFragment : SignupFragment(R.layout.fragment_signup_choose_password) {

    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupChoosePasswordBinding::bind)

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            nextButton.onClick(::onNextClicked)

            passwordInput.apply {
                setOnFocusLostListener { _, _ ->
                    validatePasswordMinLength()
                        .onFailure { setInputError(getString(R.string.auth_signup_validation_password_length)) }
                        .onSuccess { clearInputError() }
                }
                setOnNextActionListener { binding.confirmPasswordInput.requestFocus() }
            }

            confirmPasswordInput.apply {
                setOnFocusLostListener { _, _ ->
                    validatePasswordMinLength()
                        .onFailure { setInputError(getString(R.string.auth_signup_validation_password_length)) }
                        .onSuccess { clearInputError() }
                }
                setOnDoneActionListener { onNextClicked() }
            }
        }
    }

    private fun onNextClicked() {
        hideKeyboard()
        binding.passwordInput.apply {
            validatePasswordMinLength()
                .onFailure { setInputError(getString(R.string.auth_signup_validation_password_length)) }
                .onSuccess { password -> validateConfirmPasswordField(password) }
        }
    }

    private fun validateConfirmPasswordField(password: String) = with(binding) {
        val confirmedPassword = confirmPasswordInput.text.toString()
        if (password == confirmedPassword) {
            onInputValidationSuccess()
        } else {
            showError(getString(R.string.auth_signup_error_passwords_do_not_match))
            passwordInput.setInputError(" ")
            confirmPasswordInput.setInputError(" ")
        }
    }

    private fun onInputValidationSuccess() = with(binding) {
        signupViewModel.setPassword(confirmPasswordInput.text.toString())
        if (signupViewModel.currentAccountType == AccountType.External) {
            parentFragmentManager.showExternalAccountEnterCode(destination = signupViewModel.externalEmail!!)
        } else {
            parentFragmentManager.showRecoveryMethodChooser()
        }
    }
}
