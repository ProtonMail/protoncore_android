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
import me.proton.core.auth.presentation.viewmodel.ChooseAddressViewModel.ChooseAddressState
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
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
class ChooseAddressActivity :
    AuthActivity<ActivityChooseAddressBinding>(ActivityChooseAddressBinding::inflate) {

    private val input: ChooseAddressInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            stopWorkflow()
        }
    }

    private val viewModel by viewModels<ChooseAddressViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.apply {
            toolbar.setNavigationOnClickListener {
                stopWorkflow()
            }
            cancelButton.onClick { stopWorkflow() }
            nextButton.onClick(::onNextClicked)
            subtitleText.text = String.format(
                getString(R.string.auth_create_address_subtitle),
                input.recoveryEmail,
                input.recoveryEmail
            )
        }

        viewModel.setUserId(UserId(input.userId))
        viewModel.chooseAddressState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is ChooseAddressState.Idle -> onIdle()
                    is ChooseAddressState.Processing -> onProcessing()
                    is ChooseAddressState.Data.Domains -> onDomains(it.domains)
                    is ChooseAddressState.Data.UsernameProposal ->
                        onUsernameProposalAvailable(it.username)
                    is ChooseAddressState.AccountSetupResult ->
                        onAccountSetupResult(it.result)
                    is ChooseAddressState.Error.Message ->
                        onError(false, it.error.getUserMessage(resources))
                    is ChooseAddressState.Error.DomainsNotAvailable ->
                        onError(
                            false,
                            getString(R.string.auth_create_address_error_no_available_domain)
                        )
                    is ChooseAddressState.Error.UsernameNotAvailable ->
                        onUsernameUnAvailable()
                }.exhaustive
            }
            .launchIn(lifecycleScope)
    }

    private fun stopWorkflow() {
        viewModel.stopChooseAddressWorkflow(UserId(input.userId)).invokeOnCompletion { finish() }
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            nextButton.setLoading()
        } else {
            nextButton.setIdle()
        }
    }

    private fun onNextClicked() {
        with(binding) {
            hideKeyboard()
            usernameInput.validateUsername()
                .onFailure { usernameInput.setInputError() }
                .onSuccess {
                    viewModel.submit(
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
        enableForm()
    }

    private fun onProcessing() {
        showLoading(true)
        binding.usernameInput.isEnabled = false
    }

    override fun onError(
        triggerValidation: Boolean,
        message: String?,
        isPotentialBlocking: Boolean
    ) {
        super.onError(triggerValidation, message, isPotentialBlocking)
        showLoading(false)
        binding.usernameInput.isEnabled = true
        showError(message)
    }

    private fun onDomains(domains: List<Domain>) {
        showLoading(false)
        with(binding) {
            if (domains.count() == 1) {
                usernameInput.setOnDoneActionListener { onNextClicked() }
            }

            domainInput.apply {
                val items = domains.map { "@$it" }
                text = items.firstOrNull()
                setAdapter(ArrayAdapter(context, R.layout.list_item_domain, R.id.title, items))
            }
        }
    }

    private fun onUsernameUnAvailable() {
        showError(getString(R.string.auth_create_address_error_username_unavailable))
        with(binding.usernameInput) {
            setInputError()
            isEnabled = true
        }
    }

    private fun onUsernameProposalAvailable(username: String) {
        with(binding.usernameInput) {
            text = username
            isEnabled = true
        }
        showLoading(false)
        enableForm()
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

    private fun enableForm() {
        with(binding) {
            usernameInput.isEnabled = true
            nextButton.isEnabled = true
            cancelButton.isEnabled = true
            domainInput.isEnabled = true
        }
    }

    companion object {
        const val ARG_INPUT = "arg.input"
        const val ARG_RESULT = "arg.result"
    }
}
