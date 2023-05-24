/*
 * Copyright (c) 2023 Proton Technologies AG
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

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginSsoBinding
import me.proton.core.auth.presentation.entity.LoginSsoInput
import me.proton.core.auth.presentation.entity.LoginSsoResult
import me.proton.core.auth.presentation.viewmodel.LoginSsoViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validateEmail
import javax.inject.Inject

@AndroidEntryPoint
class LoginSsoActivity : AuthActivity<ActivityLoginSsoBinding>(ActivityLoginSsoBinding::inflate) {

    @Inject
    lateinit var requiredAccountType: AccountType

    private val viewModel by viewModels<LoginSsoViewModel>()

    private val input: LoginSsoInput? by lazy {
        intent?.extras?.getParcelable(ARG_INPUT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            setActionBarAuthMenu(toolbar)
            toolbar.setNavigationOnClickListener { finish() }
            signInButton.onClick(::onSignInClicked)
            emailInput.text = input?.email
            signInWithPasswordButton.onClick { finish() }
        }

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .onEach {
                when (it) {
                    is LoginSsoViewModel.State.Error -> onError(it)
                    is LoginSsoViewModel.State.Idle -> showLoading(false)
                    is LoginSsoViewModel.State.Processing -> showLoading(true)
                    is LoginSsoViewModel.State.SignInWithSrp -> onSignInWithSrp(it)
                    is LoginSsoViewModel.State.StartToken -> onStarToken(it)
                    is LoginSsoViewModel.State.Success -> onSuccess(it)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            signInButton.setLoading()
        } else {
            signInButton.setIdle()
        }
        emailInput.isEnabled = !loading
    }

    private fun onSignInClicked() {
        with(binding) {
            hideKeyboard()
            emailInput.clearInputError()
            emailInput.validateEmail()
                .onFailure { emailInput.setInputError(getString(R.string.auth_login_sso_assistive_text)) }
                .onSuccess { viewModel.startLoginWorkflow(it) }
        }
    }

    private fun onError(error: LoginSsoViewModel.State.Error) {
        showError(error.error.getUserMessage(resources))
    }

    private fun onSignInWithSrp(state: LoginSsoViewModel.State.SignInWithSrp) {
        showError(message = state.error.getUserMessage(resources), useToast = true)
        finish()
    }

    private fun onStarToken(state: LoginSsoViewModel.State.StartToken) {
        TODO("Start WebView using: ${state.token}")
    }

    private fun onIdentityProvided(email: String, token: String) {
        viewModel.createSessionWithToken(email, token, requiredAccountType)
    }

    private fun onSuccess(state: LoginSsoViewModel.State.Success) {
        setResultAndFinish(state.userId)
    }

    private fun setResultAndFinish(userId: UserId) {
        setResult(RESULT_OK, Intent().putExtra(ARG_RESULT, LoginSsoResult(userId.id)))
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.loginSsoInput"
        const val ARG_RESULT = "arg.loginSsoResult"
    }
}
