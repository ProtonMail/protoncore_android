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
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.entity.CreateAddressInput
import me.proton.core.auth.presentation.viewmodel.ChooseAddressViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.user.domain.entity.Domain
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
            onSuccess(result.success)
        }
    }

    private val viewModel by viewModels<ChooseAddressViewModel>()

    override fun layoutId(): Int = R.layout.activity_choose_address

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            closeButton.onClick(::onBackPressed)
            nextButton.onClick(::onNextClicked)
            subtitleText.text = String.format(getString(R.string.auth_create_address_subtitle, input.recoveryEmail))
        }

        viewModel.setUserId(UserId(input.userId))
        viewModel.state.observeData {
            when (it) {
                is ChooseAddressViewModel.State.Processing -> showLoading(true)
                is ChooseAddressViewModel.State.Success -> onUsernameAvailable(it.username, it.domain)
                is ChooseAddressViewModel.State.Data -> onData(it.username, it.domains)
                is ChooseAddressViewModel.State.Error.Message -> showError(it.message)
                is ChooseAddressViewModel.State.Error.DomainsNotAvailable ->
                    showError(getString(R.string.auth_create_address_error_no_available_domain))
                is ChooseAddressViewModel.State.Error.UsernameNotAvailable ->
                    onUsernameUnavailable(getString(R.string.auth_create_address_error_username_unavailable))
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

    override fun onBackPressed() {
        viewModel.stopChooseAddressWorkflow(UserId(input.userId))
            .invokeOnCompletion { finish() }
    }

    private fun onNextClicked() {
        with(binding.usernameInput) {
            hideKeyboard()
            validateUsername()
                .onFailure { setInputError() }
                .onSuccess { viewModel.checkUsername(it, suffixText.toString().replace("@", "")) }
        }
    }

    private fun onData(username: String?, domains: List<Domain>) {
        with(binding.usernameInput) {
            suffixText = "@${domains.firstOrDefault()}"
            text = username
            isEnabled = username == null
        }
        binding.nextButton.isEnabled = true
        showLoading(false)
    }

    private fun onUsernameAvailable(username: String, domain: String) {
        with(binding.nextButton) {
            setIdle()
            isEnabled = true
        }
        startForResult.launch(
            CreateAddressInput(input.userId, input.password, username, domain, input.recoveryEmail)
        )
    }

    private fun onUsernameUnavailable(message: String? = null) {
        binding.usernameInput.setInputError()
        showError(message)
    }

    private fun onSuccess(success: Boolean) {
        val intent = Intent().putExtra(ARG_RESULT, ChooseAddressResult(userId = input.userId, success = success))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.input"
        const val ARG_RESULT = "arg.result"
    }
}
