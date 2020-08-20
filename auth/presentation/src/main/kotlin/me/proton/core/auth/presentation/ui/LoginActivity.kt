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

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import kotlinx.android.synthetic.main.activity_login.*
import me.proton.android.core.presentation.utils.onClick
import me.proton.android.core.presentation.utils.validate
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginBinding
import me.proton.core.auth.presentation.viewmodel.LoginViewModel

/**
 * Login Activity which allows users to Login to any Proton client application.
 * @author Dino Kadrikj.
 */
class LoginActivity : ProtonAuthActivity<ActivityLoginBinding>() {

    private val viewModel by viewModels<LoginViewModel>()

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

            signInButton.onClick(::validateAndAttemptLogin)
        }
    }

    /**
     * Attempts to log the user in, but before that it validates the input fields and shows an error in case of
     * invalid input.
     */
    private fun validateAndAttemptLogin() {
        usernameInput.text.validate(
            onValidationFailed = { usernameInput.setInputError() },
            onValidationSuccess = ::validatePasswordAndLogin
        )
    }

    /**
     * If the username is valid, this function will try to validate the password and to execute the login.
     */
    private fun validatePasswordAndLogin(username: String) {
        passwordInput.text.validate(
            onValidationFailed = { passwordInput.setInputError() },
            onValidationSuccess = {
                signInButton.setLoading()
                viewModel.doLogin(username, it)
            }
        )
    }
}
