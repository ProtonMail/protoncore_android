/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation.ui.signup

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupChooseUsernameBinding
import me.proton.core.auth.presentation.entity.signup.SignUpInput
import me.proton.core.auth.presentation.viewmodel.signup.ChooseUsernameViewModel
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.auth.presentation.viewmodel.signup.canSwitchToExternal
import me.proton.core.domain.entity.Product
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class ChooseUsernameFragment : SignupFragment(R.layout.fragment_signup_choose_username) {

    private val viewModel by viewModels<ChooseUsernameViewModel>()
    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupChooseUsernameBinding::bind)

    private val input: SignUpInput by lazy {
        val arguments = requireArguments()
        val requiredAccountType = AccountType.values()[arguments.getInt(ARG_INPUT)]
        viewModel.setClientAppRequiredAccountType(accountType = requiredAccountType)
        SignUpInput(requiredAccountType = requiredAccountType)
    }

    override fun onBackPressed() {
        activity?.finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            usernameInput.apply {
                setOnFocusLostListener { _, _ ->
                    validateUsername()
                        .onFailure { setInputError() }
                        .onSuccess { clearInputError() }
                }
                setOnDoneActionListener { onNextClicked() }
            }

            nextButton.onClick(::onNextClicked)
            onAccountTypeSelection()
            useCurrentEmailButton.apply {
                visibility = if (input.requiredAccountType.canSwitchToExternal()) View.VISIBLE else View.GONE
                onClick { viewModel.onUserSwitchAccountType() }
            }
        }

        viewModel.state.onEach {
            when (it) {
                is ChooseUsernameViewModel.State.Idle -> showLoading(false)
                is ChooseUsernameViewModel.State.Processing -> showLoading(true)
                is ChooseUsernameViewModel.State.UsernameAvailable -> onUsernameAvailable(it.username, it.domain)
                is ChooseUsernameViewModel.State.AvailableDomains -> onDomains(it.domains, it.currentAccountType)
                is ChooseUsernameViewModel.State.Error.Message -> onError(it.error.getUserMessage(resources))
                is ChooseUsernameViewModel.State.Error.DomainsNotAvailable ->
                    onError(getString(R.string.auth_create_address_error_no_available_domain))
                is ChooseUsernameViewModel.State.Error.UsernameNotAvailable ->
                    onUsernameUnavailable(getString(R.string.auth_create_address_error_username_unavailable))
                is ChooseUsernameViewModel.State.ExternalAccountTokenSent -> onExternalAccountTokenSent(it.email)
            }.exhaustive
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onAccountTypeSelection() {
        viewModel.selectedAccountTypeState.onEach { state ->
            when (state) {
                is ChooseUsernameViewModel.AccountTypeState.NewAccountType -> with(binding) {
                    when (state.type) {
                        AccountType.Username,
                        AccountType.Internal -> {
                            usernameInput.apply {
                                labelText = getString(R.string.auth_signup_username)
                                suffixText = null
                            }
                            viewModel.domains?.let {
                                onDomains(it, state.type)
                                useCurrentEmailButton.text = getString(R.string.auth_signup_current_email)
                            }
                        }
                        AccountType.External -> {
                            usernameInput.apply {
                                labelText = getString(R.string.auth_signup_email)
                                suffixText = null
                            }
                            useCurrentEmailButton.text = getString(R.string.auth_signup_create_secure_proton_address)
                        }
                    }.exhaustive
                }
            }.exhaustive
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onNextClicked() {
        with(binding.usernameInput) {
            hideKeyboard()
            validateUsername()
                .onFailure { setInputError(getString(R.string.auth_signup_error_username_blank)) }
                .onSuccess {
                    signupViewModel.currentAccountType = viewModel.currentAccountType
                    viewModel.checkUsername(it, suffixText?.toString()?.replace("@", ""))
                }
        }
    }

    private fun onUsernameUnavailable(message: String? = null) {
        showLoading(false)
        binding.usernameInput.setInputError(message)
        showError(message)
    }

    private fun onExternalAccountTokenSent(destinationEmail: String) {
        showLoading(false)
        binding.nextButton.isEnabled = true

        signupViewModel.externalEmail = destinationEmail
        parentFragmentManager.showPasswordChooser()
    }

    private fun onUsernameAvailable(username: String, domain: String) {
        showLoading(false)
        binding.nextButton.isEnabled = true

        signupViewModel.username = username
        signupViewModel.domain = domain
        parentFragmentManager.showPasswordChooser()
    }

    private fun onDomains(domains: List<Domain>, accountType: AccountType) {
        showLoading(false)
        with(binding) {
            nextButton.isEnabled = true
            useCurrentEmailButton.isEnabled = true
            if (accountType == AccountType.Internal || accountType == AccountType.Username) {
                usernameInput.suffixText = "@${domains.first()}"
            }
            usernameInput.isSuffixTextVisible = accountType == AccountType.Internal
        }
    }

    private fun onError(message: String?) {
        binding.nextButton.setIdle()
        if (viewModel.domains.isNullOrEmpty()) {
            binding.nextButton.isEnabled = false
        }
        showError(message)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            nextButton.setLoading()
        } else {
            nextButton.setIdle()
        }
    }

    companion object {
        const val ARG_INPUT = "arg.chooseUsernameInput"
        const val ARG_PRODUCT = "arg.product"

        operator fun invoke(requiredAccountType: AccountType) = ChooseUsernameFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to requiredAccountType.ordinal
            )
        }
    }
}
