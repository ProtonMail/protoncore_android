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

@AndroidEntryPoint
class ChoosePasswordFragment : SignupFragment<FragmentSignupChoosePasswordBinding>() {

    private val signupViewModel by activityViewModels<SignupViewModel>()

    override fun layoutId() = R.layout.fragment_signup_choose_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            closeButton.onClick {
                parentFragmentManager.popBackStackImmediate()
            }

            nextButton.onClick(::onNextClicked)

            passwordInput.setOnFocusLostListener { _, _ ->
                passwordInput.validatePasswordMinLength()
                    .onFailure { passwordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
                    .onSuccess { passwordInput.clearInputError() }
            }

            confirmPasswordInput.setOnFocusLostListener { _, _ ->
                confirmPasswordInput.validatePasswordMinLength()
                    .onFailure { confirmPasswordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
                    .onSuccess { confirmPasswordInput.clearInputError() }
            }
        }
    }

    private fun onNextClicked() {
        hideKeyboard()
        with(binding) {
            passwordInput.validatePasswordMinLength()
                .onFailure { passwordInput.setInputError(getString(R.string.auth_signup_validation_password_length)) }
                .onSuccess { password ->
                    validateConfirmPasswordField(password)
                }
        }
    }

    private fun validateConfirmPasswordField(password: String) = with(binding) {
        val confirmedPassword = confirmPasswordInput.text.toString()
        if (password == confirmedPassword) {
            onInputValidationSuccess()
        } else {
            confirmPasswordInput.setInputError(getString(R.string.auth_signup_error_passwords_match))
        }
    }

    private fun onInputValidationSuccess() = with(binding) {
        signupViewModel.password = confirmPasswordInput.text.toString()
        if (signupViewModel.currentAccountType == AccountType.External) {
            parentFragmentManager.showExternalAccountEnterCode(destination = signupViewModel.externalEmail!!)
        } else {
            parentFragmentManager.showRecoveryMethodChooser()
        }
    }
}
