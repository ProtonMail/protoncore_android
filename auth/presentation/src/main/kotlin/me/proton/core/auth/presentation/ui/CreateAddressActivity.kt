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
import me.proton.core.auth.domain.usecase.AvailableDomains
import me.proton.core.auth.domain.usecase.UsernameAvailability
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityCreateAddressBinding
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.viewmodel.CreateAddressViewModel
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.util.kotlin.exhaustive

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

    private val sessionId: SessionId by lazy {
        SessionId(requireNotNull(intent?.extras?.getString(ARG_SESSION_ID)))
    }

    private val user: UserResult by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_USER))
    }

    private val startForResult = registerForActivityResult(StartAccountUpgrade()) { result ->
        if (result != null) {
            val intent = Intent().apply { putExtra(ARG_USER_RESULT, result) }
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private val viewModel by viewModels<CreateAddressViewModel>()

    override fun layoutId(): Int = R.layout.activity_create_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick { finish() }
            nextButton.onClick(::onNextClicked)
        }

        viewModel.domainsState.observeData {
            when (it) {
                is AvailableDomains.State.Success -> onDomainAvailable(it)
                is AvailableDomains.State.Error.Message -> showError(it.message)
                is AvailableDomains.State.Error.NoAvailableDomains -> {
                    showError(getString(R.string.auth_create_address_error_no_available_domain))
                }
            }.exhaustive
        }

        viewModel.usernameState.observeData {
            when (it) {
                is UsernameAvailability.State.Processing -> showLoading(true)
                is UsernameAvailability.State.Success -> onUsernameAvailable(it.username, viewModel.domain)
                is UsernameAvailability.State.Error.Message -> onUsernameUnavailable(it.message, false)
                is UsernameAvailability.State.Error.EmptyUsername -> onUsernameUnavailable(invalidInput = true)
                is UsernameAvailability.State.Error.UsernameUnavailable -> {
                    onUsernameUnavailable(getString(R.string.auth_create_address_error_username_unavailable), true)
                }
            }.exhaustive
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
                .onSuccess { viewModel.checkUsernameAvailability(it) }
        }
    }

    private fun onDomainAvailable(it: AvailableDomains.State.Success) {
        with(binding.usernameInput) {
            suffixText = "@${it.firstOrDefault}"
            validate()
                .onFailure { setInputError() }
                .onSuccess { clearInputError() }
        }
        binding.nextButton.isEnabled = true
    }

    private fun onUsernameAvailable(username: String, domain: String) {
        with(binding.nextButton) {
            setIdle()
            isEnabled = true
        }
        startForResult.launch(
            UpgradeInput(
                sessionId = sessionId,
                user = user,
                username = username,
                domain = domain
            )
        )
    }

    private fun onUsernameUnavailable(message: String? = null, invalidInput: Boolean) {
        if (invalidInput) binding.usernameInput.setInputError()
        showError(message)
    }

    companion object {
        const val ARG_SESSION_ID = "arg.sessionId"
        const val ARG_EXTERNAL_EMAIL = "arg.externalEmail"
        const val ARG_USER = "arg.user"

        const val ARG_USER_RESULT = "arg.userResult"
    }
}
