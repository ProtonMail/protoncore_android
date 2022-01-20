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
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginBinding
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.NextStep
import me.proton.core.auth.presentation.viewmodel.LoginViewModel
import me.proton.core.domain.entity.UserId
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
class LoginActivity : AuthActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {

    private val viewModel by viewModels<LoginViewModel>()

    private val input: LoginInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.register(this)
        viewModel.observeHumanVerification(this)
        binding.apply {
            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }

            helpButton.onClick {
                startActivity(Intent(this@LoginActivity, AuthHelpActivity::class.java))
            }

            signInButton.onClick(::onSignInClicked)
            usernameInput.text = input.username
            usernameInput.setOnFocusLostListener { _, _ ->
                usernameInput.validateUsername()
                    .onFailure { usernameInput.setInputError(getString(R.string.auth_login_assistive_text)) }
                    .onSuccess { usernameInput.clearInputError() }
            }
            passwordInput.text = input.password
            passwordInput.setOnFocusLostListener { _, _ ->
                passwordInput.validatePassword()
                    .onFailure { passwordInput.setInputError() }
                    .onSuccess { passwordInput.clearInputError() }
            }

            if (input.password != null) onSignInClicked()
        }

        viewModel.state.onEach {
            when (it) {
                is LoginViewModel.State.Idle -> showLoading(false)
                is LoginViewModel.State.Processing -> showLoading(true)
                is LoginViewModel.State.AccountSetupResult -> onAccountSetupResult(it.result)
                is LoginViewModel.State.ErrorMessage -> onError(true, it.message)
            }.exhaustive
        }.launchIn(lifecycleScope)
    }

    private fun onAccountSetupResult(result: PostLoginAccountSetup.Result) {
        when (result) {
            is PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError -> onUnlockUserError(result.error)
            is PostLoginAccountSetup.Result.Error.UserCheckError -> onUserCheckFailed(result.error)
            is PostLoginAccountSetup.Result.Need.ChangePassword -> onChangePassword()
            is PostLoginAccountSetup.Result.Need.ChooseUsername -> onSuccess(result.userId, NextStep.ChooseAddress)
            is PostLoginAccountSetup.Result.Need.SecondFactor -> onSuccess(result.userId, NextStep.SecondFactor)
            is PostLoginAccountSetup.Result.Need.TwoPassMode -> onSuccess(result.userId, NextStep.TwoPassMode)
            is PostLoginAccountSetup.Result.UserUnlocked -> onSuccess(result.userId, NextStep.None)
        }.exhaustive
    }

    private fun onChangePassword() {
        showLoading(false)
        binding.passwordInput.text = ""
        supportFragmentManager.showPasswordChangeDialog(context = this)
    }

    override fun onBackPressed() {
        viewModel.stopLoginWorkflow()
            .invokeOnCompletion { finish() }
    }

    private fun onSuccess(
        userId: UserId,
        nextStep: NextStep
    ) {
        val intent = Intent()
            .putExtra(ARG_RESULT, LoginResult(userId = userId.id, nextStep = nextStep))
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
                .onFailure { usernameInput.setInputError(getString(R.string.auth_login_assistive_text)) }
                .onSuccess(::onUsernameValidationSuccess)
        }
    }

    private fun onUsernameValidationSuccess(username: String) {
        with(binding) {
            passwordInput.validatePassword()
                .onFailure { passwordInput.setInputError() }
                .onSuccess { password ->
                    viewModel.startLoginWorkflow(username, password, input.requiredAccountType)
                }
        }
    }

    companion object {
        const val ARG_INPUT = "arg.loginInput"
        const val ARG_RESULT = "arg.loginResult"
    }
}
