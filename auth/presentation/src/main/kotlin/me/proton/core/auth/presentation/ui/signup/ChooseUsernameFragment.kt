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
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentSignupChooseUsernameBinding
import me.proton.core.auth.presentation.ui.onLongState
import me.proton.core.auth.presentation.viewmodel.signup.ChooseUsernameViewModel
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.showToast
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.user.domain.entity.Domain
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class ChooseUsernameFragment : SignupFragment(R.layout.fragment_signup_choose_username) {

    private val viewModel by viewModels<ChooseUsernameViewModel>()
    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupChooseUsernameBinding::bind)

    override fun onBackPressed() {
        signupViewModel.onFinish()
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
            }

            nextButton.onClick(::onNextClicked)
            onAccountTypeSelection()
            useCurrentEmailButton.apply {
                onClick { viewModel.onUserSwitchAccountType() }
            }
        }

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onLongState(ChooseUsernameViewModel.State.Processing) {
                requireContext().showToast(getString(R.string.auth_long_signup))
            }
            .launchIn(lifecycleScope)

        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is ChooseUsernameViewModel.State.Idle -> showLoading(false)
                    is ChooseUsernameViewModel.State.Processing -> showLoading(true)
                    is ChooseUsernameViewModel.State.UsernameAvailable -> onUsernameAvailable(
                        it.username,
                        it.domain,
                        it.currentAccountType
                    )
                    is ChooseUsernameViewModel.State.AvailableDomains -> onDomains(it.domains)
                    is ChooseUsernameViewModel.State.Error.Message -> onError(it.error.getUserMessage(resources))
                    is ChooseUsernameViewModel.State.Error.DomainsNotAvailable ->
                        onError(getString(R.string.auth_create_address_error_no_available_domain))
                    is ChooseUsernameViewModel.State.Error.UsernameNotAvailable ->
                        onUsernameUnavailable(getString(R.string.auth_create_address_error_username_unavailable))
                    is ChooseUsernameViewModel.State.ExternalAccountTokenSent -> onExternalAccountTokenSent(it.email)
                }.exhaustive
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onAccountTypeSelection() {
        viewModel.selectedAccountTypeState.onEach { state ->
            when (state) {
                is ChooseUsernameViewModel.AccountTypeState.NewAccountType -> with(binding) {
                    when (state.type) {
                        AccountType.Username -> {
                            internalAccountGroup.visibility = View.GONE
                            useCurrentEmailButton.visibility = View.GONE
                            separatorView.visibility = View.GONE
                        }
                        AccountType.Internal -> {
                            usernameInput.apply {
                                labelText = getString(R.string.auth_signup_username)
                                suffixText = null
                            }
                            viewModel.domains?.let {
                                onDomains(it)
                            }
                            useCurrentEmailButton.text = getString(R.string.auth_signup_current_email)
                            footnoteText.text = getString(R.string.auth_signup_internal_footnote)
                            internalAccountGroup.visibility = View.VISIBLE
                            useCurrentEmailButton.visibility =
                                if (state.minimalAccountType == AccountType.Internal) View.GONE else View.VISIBLE
                        }
                        AccountType.External -> {
                            usernameInput.apply {
                                labelText = getString(R.string.auth_signup_email)
                                suffixText = null
                            }
                            useCurrentEmailButton.text = getString(R.string.auth_signup_create_secure_proton_address)
                            footnoteText.text = getString(R.string.auth_signup_external_footnote)
                            internalAccountGroup.visibility = View.GONE
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
                .onSuccess { username ->
                    val domain = binding.domainInput.text?.toString()?.replace("@", "")
                    viewModel.checkUsername(username, domain)
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

    private fun onUsernameAvailable(username: String, domain: String, currentAccountType: AccountType) {
        showLoading(false)
        binding.nextButton.isEnabled = true

        signupViewModel.username = username
        signupViewModel.domain = domain
        signupViewModel.currentAccountType = currentAccountType
        lifecycleScope.launch {
            binding.usernameInput.flush()
        }
        parentFragmentManager.showPasswordChooser()
    }

    private fun onDomains(domains: List<Domain>) {
        showLoading(false)
        with(binding) {
            nextButton.isEnabled = true
            useCurrentEmailButton.isEnabled = true

            if (viewModel.domains?.count() == 1) {
                usernameInput.suffixText = "@${domains.first()}"
                usernameInput.setOnDoneActionListener { onNextClicked() }
            } else {
                domainInput.isVisible = true
            }

            domainInput.apply {
                val items = domains.map { "@$it" }
                text = items.firstOrNull()
                setAdapter(ArrayAdapter(context, R.layout.list_item_domain, R.id.title, items))
            }
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

        operator fun invoke(requiredAccountType: AccountType) = ChooseUsernameFragment().apply {
            arguments = bundleOf(
                ARG_INPUT to requiredAccountType.ordinal
            )
        }
    }
}
