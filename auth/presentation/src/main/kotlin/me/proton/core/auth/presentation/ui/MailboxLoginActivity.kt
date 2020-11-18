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
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.usecase.PerformUserSetup
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityMailboxLoginBinding
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.viewmodel.MailboxLoginViewModel
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.presentation.utils.validatePassword
import me.proton.core.util.kotlin.exhaustive

/**
 * Mailbox Login Activity which allows users to unlock their Mailbox.
 * Note that this is only valid for accounts which are 2 password accounts (they use separate password for login and
 * mailbox).
 */
@AndroidEntryPoint
class MailboxLoginActivity : AuthActivity<ActivityMailboxLoginBinding>() {

    private val sessionId: SessionId by lazy {
        SessionId(requireNotNull(intent?.extras?.getString(ARG_SESSION_ID)))
    }

    private val requiredAccountType: AccountType by lazy {
        AccountType.valueOf(requireNotNull(intent?.extras?.getString(ARG_REQUIRED_ACCOUNT_TYPE)))
    }

    private val viewModel by viewModels<MailboxLoginViewModel>()

    override fun layoutId(): Int = R.layout.activity_mailbox_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick {
                onBackPressed()
            }

            forgotPasswordButton.onClick {
                openBrowserLink(getString(R.string.forgot_password_link))
            }

            unlockButton.onClick(::onUnlockClicked)
            mailboxPasswordInput.setOnFocusLostListener { _, _ ->
                mailboxPasswordInput.validatePassword()
                    .onFailure { mailboxPasswordInput.setInputError() }
                    .onSuccess { mailboxPasswordInput.clearInputError() }
            }
        }

        viewModel.mailboxLoginState.observeData {
            when (it) {
                is PerformUserSetup.State.Processing -> showLoading(true)
                is PerformUserSetup.State.Success -> onSuccess(it.user)
                is PerformUserSetup.State.Error.Message -> onError(false, it.message)
                is PerformUserSetup.State.Error.EmptyCredentials -> onError(
                    true,
                    getString(R.string.auth_mailbox_empty_credentials)
                )
                is PerformUserSetup.State.Error.NoPrimaryKey -> onError(
                    false,
                    getString(R.string.auth_mailbox_login_error_no_primary_key)
                )
                is PerformUserSetup.State.Error.NoKeySaltsForPrimaryKey -> onError(
                    false,
                    getString(R.string.auth_mailbox_login_error_primary_key_error)
                )
                is PerformUserSetup.State.Error.PrimaryKeyInvalidPassphrase -> onError(
                    false,
                    getString(R.string.auth_mailbox_login_error_invalid_passphrase)
                )
            }.exhaustive
        }
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            unlockButton.setLoading()
        } else {
            unlockButton.setIdle()
        }
        mailboxPasswordInput.isEnabled = !loading
    }

    override fun onBackPressed() {
        viewModel.stopMailboxLoginFlow(sessionId).invokeOnCompletion {
            finish()
        }
    }

    /**
     * Invoked on successful completed mailbox login operation.
     */
    private fun onSuccess(user: User) {
        val intent = Intent().apply {
            putExtra(
                ARG_MAILBOX_LOGIN_RESULT,
                TwoPassModeResult(sessionId.id, UserResult.from(user, requiredAccountType))
            )
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onError(triggerValidation: Boolean, message: String?) {
        if (triggerValidation) {
            binding.mailboxPasswordInput.setInputError()
        }
        showError(message)
    }

    private fun onUnlockClicked() {
        hideKeyboard()
        with(binding) {
            mailboxPasswordInput.validatePassword()
                .onFailure {
                    mailboxPasswordInput.setInputError()
                }
                .onSuccess {
                    viewModel.startUserSetup(sessionId, it.toByteArray())
                }
        }
    }

    companion object {
        const val ARG_SESSION_ID = "arg.sessionId"
        const val ARG_REQUIRED_ACCOUNT_TYPE = "arg.requiredAccountType"
        const val ARG_MAILBOX_LOGIN_RESULT = "arg.mailboxLoginResult"
    }
}
