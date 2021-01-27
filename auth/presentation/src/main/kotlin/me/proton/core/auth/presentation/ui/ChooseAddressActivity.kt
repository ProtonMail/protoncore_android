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
import me.proton.core.auth.presentation.databinding.ActivityChooseAddressBinding
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.CreateAddressInput
import me.proton.core.auth.presentation.viewmodel.ChooseAddressViewModel
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validate
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.user.domain.entity.firstOrDefault
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
class ChooseAddressActivity : AuthActivity<ActivityChooseAddressBinding>() {

    private val input: ChooseAddressInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val startForResult = registerForActivityResult(StartCreateAddress()) { result ->
        if (result != null) {
            setResult(Activity.RESULT_OK, Intent())
            finish()
        }
    }

    private val viewModel by viewModels<ChooseAddressViewModel>()

    override fun layoutId(): Int = R.layout.activity_choose_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick { finish() }
            nextButton.onClick(::onNextClicked)
        }

        viewModel.domainsState.observeData {
            when (it) {
                is ChooseAddressViewModel.DomainState.Processing -> Unit
                is ChooseAddressViewModel.DomainState.Success -> onDomainAvailable(it)
                is ChooseAddressViewModel.DomainState.Error.Message -> showError(it.message)
                is ChooseAddressViewModel.DomainState.Error.NoAvailableDomains -> {
                    showError(getString(R.string.auth_create_address_error_no_available_domain))
                }
            }.exhaustive
        }

        viewModel.usernameState.observeData {
            when (it) {
                is ChooseAddressViewModel.UsernameState.Processing -> showLoading(true)
                is ChooseAddressViewModel.UsernameState.Success -> {
                    if (it.available)
                        onUsernameAvailable(it.username, viewModel.domain)
                    else
                        onUsernameUnavailable(getString(R.string.auth_create_address_error_username_unavailable), true)
                }
                is ChooseAddressViewModel.UsernameState.Error.Message -> onUsernameUnavailable(it.message, false)
            }.exhaustive
        }
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

    private fun onDomainAvailable(it: ChooseAddressViewModel.DomainState.Success) {
        with(binding.usernameInput) {
            suffixText = "@${it.domains.firstOrDefault()}"
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
            CreateAddressInput(input.sessionId, username, domain)
        )
    }

    private fun onUsernameUnavailable(message: String? = null, invalidInput: Boolean) {
        if (invalidInput) binding.usernameInput.setInputError()
        showError(message)
    }

    companion object {
        const val ARG_INPUT = "arg.input"
        const val ARG_RESULT = "arg.result"
    }
}
