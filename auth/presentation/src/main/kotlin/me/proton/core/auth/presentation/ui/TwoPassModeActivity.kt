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
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityMailboxLoginBinding
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.TwoPassModeResult
import me.proton.core.auth.presentation.viewmodel.TwoPassModeViewModel
import me.proton.core.domain.entity.UserId
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
class TwoPassModeActivity : AuthActivity<ActivityMailboxLoginBinding>() {

    private val input: TwoPassModeInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val userId by lazy { UserId(input.userId) }

    private val viewModel by viewModels<TwoPassModeViewModel>()

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
                is TwoPassModeViewModel.State.Processing -> showLoading(true)
                is TwoPassModeViewModel.State.Success.UserUnLocked -> onSuccess(it.userId)
                is TwoPassModeViewModel.State.Error.Message -> onError(false, it.message)
                is TwoPassModeViewModel.State.Error.CannotUnlockPrimaryKey -> onUnlockUserError(it.error)
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
        viewModel.stopMailboxLoginFlow(userId)
            .invokeOnCompletion { finish() }
    }

    private fun onSuccess(userId: UserId) {
        val intent = Intent()
            .putExtra(ARG_RESULT, TwoPassModeResult(userId.id))
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
                .onFailure { mailboxPasswordInput.setInputError() }
                .onSuccess { viewModel.tryUnlockUser(userId, it) }
        }
    }

    companion object {
        const val ARG_INPUT = "arg.input"
        const val ARG_RESULT = "arg.result"
    }
}
