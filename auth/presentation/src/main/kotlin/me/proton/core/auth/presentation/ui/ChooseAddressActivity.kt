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
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityChooseAddressBinding
import me.proton.core.auth.presentation.entity.ChooseAddressInput
import me.proton.core.auth.presentation.entity.ChooseAddressResult
import me.proton.core.auth.presentation.viewmodel.ChooseAddressViewModel
import me.proton.core.auth.presentation.viewmodel.ChooseAddressViewModel.State
import me.proton.core.domain.entity.UserId
import me.proton.core.observability.domain.metrics.LoginScreenViewTotal
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.exhaustive

/**
 * Creates a Proton Mail address when needed.
 * Usually a common use case would be a user with external email trying to
 * login into one of Proton services (apps) which require a Proton Mail address.
 * In this flow, the external email address would be set as an account recovery email address.
 *
 * This is the first scree of the flow, which should help the user choose an available username (who's availability is
 * checks with the API.
 */
@AndroidEntryPoint
class ChooseAddressActivity : AuthActivity<ActivityChooseAddressBinding>(
    ActivityChooseAddressBinding::inflate
) {

    private val viewModel by viewModels<ChooseAddressViewModel>()

    private val input: ChooseAddressInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            stopWorkflow()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.apply {
            toolbar.setNavigationOnClickListener { stopWorkflow() }
            cancelButton.onClick(::stopWorkflow)
            nextButton.onClick(::onNextClicked)
            subtitleText.text = String.format(
                getString(R.string.auth_create_address_subtitle),
                input.recoveryEmail,
                input.recoveryEmail
            )
        }

        viewModel.startWorkFlow(UserId(input.userId))
        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is State.Idle -> onIdle()
                    is State.Processing -> onProcessing()
                    is State.Data.Domains -> onDomains(it.domains)
                    is State.Data.UsernameOption -> onUsernameProposalAvailable(it.username)
                    is State.Data.UsernameAlreadySet -> onUsernameAlreadySet(it.username)
                    is State.Error.Start -> onError(it.error.getUserMessage(resources), retry = true)
                    is State.Error.SetUsername -> onError(it.error.getUserMessage(resources), retry = false)
                    is State.AccountSetupResult -> onAccountSetupResult(it.result)
                    is State.Finished -> finish()
                }.exhaustive
            }.launchIn(lifecycleScope)

        launchOnScreenView {
            viewModel.onScreenView(LoginScreenViewTotal.ScreenId.chooseInternalAddress)
        }
    }

    private fun stopWorkflow() {
        viewModel.stopWorkflow(UserId(input.userId))
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            nextButton.setLoading()
            nextButton.isEnabled = false
            usernameInput.isEnabled = false
            domainInput.isEnabled = false
        } else {
            nextButton.setIdle()
            nextButton.isEnabled = true
            usernameInput.isEnabled = true
            domainInput.isEnabled = true
        }
    }

    private fun onNextClicked() {
        with(binding) {
            hideKeyboard()
            usernameInput.validateUsername()
                .onFailure { usernameInput.setInputError(getString(R.string.presentation_field_required)) }
                .onSuccess {
                    viewModel.setUsername(
                        UserId(input.userId),
                        username = it,
                        password = input.password,
                        domain = domainInput.text.toString().replace("@", ""),
                        isTwoPassModeNeeded = input.isTwoPassModeNeeded
                    )
                }
        }
    }

    private fun onIdle() {
        showLoading(false)
    }

    private fun onProcessing() {
        showLoading(true)
    }

    fun onError(
        message: String?,
        retry: Boolean,
    ) {
        showLoading(false)
        binding.nextButton.isEnabled = false
        when {
            retry -> showError(message, getString(R.string.presentation_retry), { onRetryClicked() }, false)
            else -> showError(message)
        }
    }

    private fun onDomains(domains: List<Domain>) {
        binding.domainInput.apply {
            val items = domains.map { "@$it" }
            text = items.firstOrNull()
            setAdapter(ArrayAdapter(context, R.layout.list_item_domain, R.id.title, items))
        }
    }

    private fun onRetryClicked() {
        viewModel.startWorkFlow(UserId(input.userId))
    }

    private fun onUsernameProposalAvailable(username: String) {
        showLoading(false)
        binding.usernameInput.text = username
    }

    private fun onUsernameAlreadySet(username: String) {
        showLoading(false)
        binding.usernameInput.text = username
        binding.usernameInput.isEnabled = false
    }

    private fun onAccountSetupResult(result: PostLoginAccountSetup.Result) {
        when (result) {
            is PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError -> onUnlockUserError(result.error)
            is PostLoginAccountSetup.Result.Error.UserCheckError -> onUserCheckError(result.error)

            is PostLoginAccountSetup.Result.Need.ChangePassword,
            is PostLoginAccountSetup.Result.Need.ChooseUsername,
            is PostLoginAccountSetup.Result.Need.SecondFactor,
            is PostLoginAccountSetup.Result.Need.TwoPassMode,
            is PostLoginAccountSetup.Result.UserUnlocked -> onSuccess()
        }
    }

    private fun onSuccess() {
        setResultAndFinish(ChooseAddressResult.Success(input.userId))
    }

    private fun onUserCheckError(error: PostLoginAccountSetup.UserCheckResult.Error) {
        onUserCheckFailed(error, useToast = true)
        setResultAndFinish(ChooseAddressResult.UserCheckError(error.localizedMessage))
    }

    private fun setResultAndFinish(result: ChooseAddressResult) {
        val intent = Intent().putExtra(ARG_RESULT, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val ARG_INPUT = "arg.input"
        const val ARG_RESULT = "arg.result"
    }
}
