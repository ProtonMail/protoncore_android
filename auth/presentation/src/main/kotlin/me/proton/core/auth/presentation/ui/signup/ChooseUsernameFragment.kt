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
import me.proton.core.auth.presentation.viewmodel.signup.ChooseUsernameViewModel.State
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.showToast
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
class ChooseUsernameFragment : SignupFragment(R.layout.fragment_signup_choose_username) {

    private val viewModel by viewModels<ChooseUsernameViewModel>()
    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupChooseUsernameBinding::bind)

    private val cancellable: Boolean by lazy { requireArguments().getBoolean(ARG_INPUT_CANCELLABLE) }

    override fun onBackPressed() {
        if (cancellable) {
            signupViewModel.onFinish()
            activity?.finish()
        } else {
            showError(getString(R.string.auth_signup_error_create_to_continue))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationIcon(R.drawable.ic_proton_close, cancellable)
            toolbar.setNavigationOnClickListener { onBackPressed() }

            usernameInput.apply {
                setOnFocusLostListener { _, _ ->
                    validateUsername()
                        .onFailure { setInputError() }
                        .onSuccess { clearInputError() }
                }
            }

            nextButton.onClick(::onNextClicked)
        }

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is State.Idle -> showLoading(false)
                    is State.Processing -> showLoading(true)
                    is State.Success -> onUsernameAvailable(it.username)
                    is State.Error.Message -> onError(it.error.getUserMessage(resources))
                }.exhaustive
            }
            .onLongState(State.Processing) {
                requireContext().showToast(getString(R.string.auth_long_signup))
            }
            .launchIn(lifecycleScope)

        launchOnScreenView {
            signupViewModel.onScreenView(SignupScreenViewTotalV1.ScreenId.chooseUsername)
        }
    }

    private fun onNextClicked() {
        with(binding.usernameInput) {
            lifecycleScope.launch { flush() }
            hideKeyboard()
            validateUsername()
                .onFailure { setInputError(getString(R.string.auth_signup_error_username_blank)) }
                .onSuccess { username -> viewModel.checkUsername(username) }
        }
    }

    private fun onUsernameAvailable(username: String) {
        showLoading(false)
        binding.nextButton.isEnabled = true
        signupViewModel.currentAccountType = AccountType.Username
        signupViewModel.username = username
        parentFragmentManager.showPasswordChooser()
    }

    private fun onError(message: String?) {
        binding.nextButton.setIdle()
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
        const val ARG_INPUT_CANCELLABLE = "arg.cancellable"

        operator fun invoke(
            cancellable: Boolean = true,
        ) = ChooseUsernameFragment().apply {
            arguments = bundleOf(
                ARG_INPUT_CANCELLABLE to cancellable
            )
        }
    }
}
