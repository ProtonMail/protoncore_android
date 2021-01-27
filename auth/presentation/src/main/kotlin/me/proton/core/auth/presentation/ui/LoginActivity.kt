/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.auth.presentation.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginBinding
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.NextStep
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.auth.presentation.viewmodel.LoginViewModel
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validatePassword
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.util.kotlin.exhaustive

/**
 * Login Activity which allows users to Login to any Proton client application.
 */
@AndroidEntryPoint
class LoginActivity : AuthActivity<ActivityLoginBinding>() {

    private val viewModel by viewModels<LoginViewModel>()

    private val input: LoginInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun layoutId(): Int = R.layout.activity_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick {
                finish()
            }

            helpButton.onClick {
                startActivity(Intent(this@LoginActivity, AuthHelpActivity::class.java))
            }

            signInButton.onClick(::onSignInClicked)
            usernameInput.setOnFocusLostListener { _, _ ->
                usernameInput.validateUsername()
                    .onFailure { usernameInput.setInputError() }
                    .onSuccess { usernameInput.clearInputError() }
            }
            passwordInput.setOnFocusLostListener { _, _ ->
                passwordInput.validatePassword()
                    .onFailure { passwordInput.setInputError() }
                    .onSuccess { passwordInput.clearInputError() }
            }
        }

        viewModel.loginState.observeData {
            when (it) {
                is LoginViewModel.State.Processing -> showLoading(true)
                is LoginViewModel.State.Success.UserUnLocked -> onSuccess(it.sessionInfo, NextStep.None)
                is LoginViewModel.State.Need.SecondFactor -> onSuccess(it.sessionInfo, NextStep.SecondFactor)
                is LoginViewModel.State.Need.TwoPassMode -> onSuccess(it.sessionInfo, NextStep.TwoPassMode)
                is LoginViewModel.State.Need.ChooseUsername -> onSuccess(it.sessionInfo, NextStep.ChooseAddress)
                is LoginViewModel.State.Need.ChangePassword -> onChangePassword()
                is LoginViewModel.State.Error.CannotUnlockPrimaryKey -> onUnlockUserError(it.error)
                is LoginViewModel.State.Error.Message -> onError(true, it.message)
            }.exhaustive
        }
    }

    private fun onChangePassword() {
        showLoading(false)
        supportFragmentManager.showPasswordChangeDialog(this)
    }

    private fun onSuccess(
        sessionInfo: SessionInfo,
        nextStep: NextStep
    ) {
        val intent = Intent().putExtra(
            ARG_RESULT,
            LoginResult(
                password = binding.passwordInput.text.toString().toByteArray(),
                session = SessionResult.from(sessionInfo),
                requiredUserType = input.requiredUserType,
                nextStep = nextStep
            )
        )
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onError(triggerValidation: Boolean, message: String?) {
        if (triggerValidation) {
            binding.apply {
                usernameInput.setInputError()
                passwordInput.setInputError()
            }
        }
        showError(message)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            signInButton.setLoading()
        } else {
            signInButton.setIdle()
        }
        usernameInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
    }

    private fun onSignInClicked() {
        with(binding) {
            hideKeyboard()
            usernameInput.validateUsername()
                .onFailure { usernameInput.setInputError() }
                .onSuccess(::onUsernameValidationSuccess)
        }
    }

    private fun onUsernameValidationSuccess(username: String) {
        with(binding) {
            passwordInput.validatePassword()
                .onFailure { passwordInput.setInputError() }
                .onSuccess { password ->
                    signInButton.setLoading()
                    viewModel.startLoginWorkflow(username, password.toByteArray(), input.requiredUserType)
                }
        }
    }

    companion object {
        const val ARG_INPUT = "arg.loginInput"
        const val ARG_RESULT = "arg.loginResult"
    }
}
