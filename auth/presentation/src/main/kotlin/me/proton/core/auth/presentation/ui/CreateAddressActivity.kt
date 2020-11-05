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
import dagger.hilt.android.AndroidEntryPoint
import me.proton.android.core.presentation.utils.hideKeyboard
import me.proton.android.core.presentation.utils.onClick
import me.proton.android.core.presentation.utils.onFailure
import me.proton.android.core.presentation.utils.onSuccess
import me.proton.android.core.presentation.utils.validateUsername
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UsernameAvailability
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityCreateAddressBinding
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.viewmodel.CreateAddressViewModel
import me.proton.core.network.domain.session.SessionId

/**
 * Creates a ProtonMail address when needed.
 * Usually a common use case would be a user with external email trying to
 * login into one of Proton services (apps) which require a Proton Mail address.
 * In this flow, the external email address would be set as an account recovery email address.
 *
 * This is the first scree of the flow, which should help the user choose an available username (who's availability is
 * checks with the API.
 *
 * @author Dino Kadrikj.
 */
@AndroidEntryPoint
class CreateAddressActivity : AuthActivity<ActivityCreateAddressBinding>() {

    private val externalEmail: String by lazy {
        intent?.extras?.get(ARG_EXTERNAL_EMAIL) as String
    }

    private val sessionId: SessionId by lazy {
        intent?.extras?.get(ARG_SESSION_ID) as SessionId
    }

    private val user: UserResult by lazy {
        intent?.extras?.get(ARG_USER) as UserResult
    }

    private val viewModel by viewModels<CreateAddressViewModel>()

    override fun layoutId(): Int = R.layout.activity_create_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            closeButton.onClick {
                finish()
            }
            nextButton.apply {
                onClick(::onNextClicked)
                isEnabled = false // this will protect from premature next button clicks
            }
            viewModel.domainsState.observeData {
                nextButton.isEnabled = true
                usernameInput.apply {
                    requestFocus()
                    suffixText = (it as AvailableDomains.State.Success).firstDomainOrDefault
                }
            }
        }

        viewModel.state.observeData {
            when (it) {
                is UsernameAvailability.State.Processing -> showLoading(true)
                is UsernameAvailability.State.Success -> {
                    it.domain?.let { domain ->
                        onUsernameAvailable(it.username, domain)
                    } ?: run {
                        showError("Domain must be set!")
                    }
                }
                is UsernameAvailability.State.Error.Message ->
                    onUsernameUnavailable(it.message, false)
                is UsernameAvailability.State.Error.EmptyUsername -> onUsernameUnavailable(
                    invalidInput = true
                )
                is UsernameAvailability.State.Error.UsernameUnavailable -> onUsernameUnavailable(
                    getString(R.string.auth_create_address_error_username_unavailable),
                    true
                )
            }
        }
        viewModel.getAvailableDomains()
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            nextButton.setLoading()
        } else {
            nextButton.setIdle()
        }
    }

    private fun onNextClicked() {
        with(binding.usernameInput) {
            hideKeyboard()
            validateUsername()
                .onFailure { setInputError() }
                .onSuccess {
                    viewModel.checkUsernameAvailability(it)
                }
        }
    }

    private fun onUsernameAvailable(username: String, domain: String) {
        startActivity(Intent(this, CreateAddressResultActivity::class.java).apply {
            putExtra(CreateAddressResultActivity.ARG_SESSION_ID, sessionId.id)
            putExtra(CreateAddressResultActivity.ARG_USERNAME, username)
            putExtra(CreateAddressResultActivity.ARG_EXTERNAL_EMAIL, externalEmail)
            putExtra(CreateAddressResultActivity.ARG_DOMAIN, domain)
            putExtra(CreateAddressResultActivity.ARG_USER, user)
        })
    }

    private fun onUsernameUnavailable(message: String? = null, invalidInput: Boolean) {
        if (invalidInput) {
            binding.apply {
                usernameInput.setInputError()
            }
        }
        showError(message)
    }

    companion object {
        const val ARG_SESSION_ID = "arg.sessionId"
        const val ARG_EXTERNAL_EMAIL = "arg.externalEmail"
        const val ARG_USER = "arg.user"
    }
}
