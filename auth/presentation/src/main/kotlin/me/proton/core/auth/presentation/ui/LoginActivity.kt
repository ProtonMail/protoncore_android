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
import me.proton.android.core.presentation.ui.ProtonActivity
import me.proton.android.core.presentation.utils.hideKeyboard
import me.proton.android.core.presentation.utils.onClick
import me.proton.android.core.presentation.utils.onFailure
import me.proton.android.core.presentation.utils.onSuccess
import me.proton.android.core.presentation.utils.validatePassword
import me.proton.android.core.presentation.utils.validateUsername
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.usecase.PerformLogin
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginBinding
import me.proton.core.auth.presentation.entity.SessionResult
import me.proton.core.auth.presentation.viewmodel.LoginViewModel
import me.proton.core.util.kotlin.exhaustive

/**
 * Login Activity which allows users to Login to any Proton client application.
 */
@AndroidEntryPoint
class LoginActivity : ProtonActivity<ActivityLoginBinding>(),
    AuthActivityComponent<ActivityLoginBinding> by AuthActivityDelegate() {

    private val viewModel by viewModels<LoginViewModel>()
    private val authOrchestrator = AuthOrchestrator()

    override fun layoutId(): Int = R.layout.activity_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAuth(this)
        authOrchestrator.register(this)

        binding.apply {
            closeButton.onClick {
                finish()
            }

            helpButton.onClick {
                startActivity(Intent(this@LoginActivity, AuthHelpActivity::class.java))
            }

            signInButton.onClick(::onSignInClicked)
        }

        viewModel.loginState.observeData {
            when (it) {
                is PerformLogin.LoginState.Processing -> showLoading(true)
                is PerformLogin.LoginState.Success -> onSuccess(it.sessionInfo)
                is PerformLogin.LoginState.Error.Message -> onError(it.validation, it.message)
                is PerformLogin.LoginState.Error.EmptyCredentials -> onError(
                    true,
                    getString(R.string.auth_login_empty_credentials)
                )
            }.exhaustive
        }
    }

    /**
     * Invoked on successful completed login operation.
     */
    private fun onSuccess(sessionInfo: SessionInfo) {
        val intent = Intent().putExtra(ARG_SESSION_RESULT, SessionResult.from(sessionInfo))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * Invoked on error result from login operation.
     */
    private fun onError(triggerValidation: Boolean, message: String?) {
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
                    viewModel.startLoginWorkflow(username, password.toByteArray())
                }
        }
    }

    companion object {
        const val ARG_REQUIRED_FEATURES = "arg.requiredFeatures"
        const val ARG_SESSION_RESULT = "arg.sessionResult"
    }
}
