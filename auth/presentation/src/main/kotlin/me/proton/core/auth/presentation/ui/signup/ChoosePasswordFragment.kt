/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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
import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePasswordMatch
import me.proton.core.presentation.utils.validatePasswordMinLength
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused

@AndroidEntryPoint
@ProductMetrics(
    group = "account.any.signup",
    flow = "mobile_signup_full"
)
@ScreenDisplayed(event = "fe.signup_password.displayed")
@ScreenClosed(event = "user.signup_password.closed")
@ViewClicked(
    event = "user.signup_password.clicked",
    viewIds = ["nextButton"]
)
@ViewFocused(
    event = "user.signup_password.focused",
    viewIds = ["passwordInput", "confirmPasswordInput"]
)
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

        launchOnScreenView {
            signupViewModel.onScreenView(SignupScreenViewTotalV1.ScreenId.createPassword)
        }
    }

    private fun onNextClicked() {
        hideKeyboard()
        binding.passwordInput.apply {
            val result = validatePasswordMinLength()
                .onFailure { setInputError(getString(R.string.auth_signup_validation_password_length)) }
                .onSuccess { password -> validateConfirmPasswordField(password) }
            signupViewModel.onInputValidationResult(result)
        }
    }

    private fun validateConfirmPasswordField(password: String) = with(binding) {
        val confirmedPassword = confirmPasswordInput.text.toString()
        val result = passwordInput.validatePasswordMatch(confirmedPassword)
            .onFailure {
                showError(getString(R.string.auth_signup_error_passwords_do_not_match))
                passwordInput.setInputError(" ")
                confirmPasswordInput.setInputError(" ")
            }
            .onSuccess {
                onInputValidationSuccess()
            }
        signupViewModel.onInputValidationResult(result)
    }

    private fun onInputValidationSuccess() = with(binding) {
        signupViewModel.setPassword(confirmPasswordInput.text.toString())
        when (signupViewModel.currentAccountType) {
            AccountType.External -> signupViewModel.skipRecoveryMethod()
            AccountType.Username -> parentFragmentManager.showRecoveryMethodChooser()
            AccountType.Internal -> parentFragmentManager.showRecoveryMethodChooser()
        }
    }
}
