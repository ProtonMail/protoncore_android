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
import me.proton.core.auth.presentation.databinding.FragmentSignupChooseExternalEmailBinding
import me.proton.core.auth.presentation.ui.onLongState
import me.proton.core.auth.presentation.viewmodel.signup.ChooseExternalEmailViewModel
import me.proton.core.auth.presentation.viewmodel.signup.ChooseExternalEmailViewModel.State
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.observability.domain.metrics.SignupScreenViewTotalV1
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.showToast
import me.proton.core.presentation.utils.validateEmail
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
@ProductMetrics(
    group = "account.android.signup",
    flow = "mobile_signup_full"
)
@ViewClicked(
    event = "user.signup.clicked",
    viewIds = ["nextButton", "switchButton"]
)
@ViewFocused(
    event = "user.signup.focused",
    viewIds = ["emailInput"]
)
class ChooseExternalEmailFragment : SignupFragment(R.layout.fragment_signup_choose_external_email) {

    private val viewModel by viewModels<ChooseExternalEmailViewModel>()
    private val signupViewModel by activityViewModels<SignupViewModel>()
    private val binding by viewBinding(FragmentSignupChooseExternalEmailBinding::bind)

    private val creatableAccountType by lazy {
        AccountType.valueOf(requireNotNull(requireArguments().getString(ARG_INPUT_ACCOUNT_TYPE)))
    }

    override fun onBackPressed() {
        signupViewModel.onFinish()
        activity?.finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            emailInput.apply {
                setOnFocusLostListener { _, _ ->
                    validateEmail()
                        .onFailure { setInputError() }
                        .onSuccess { clearInputError() }
                }
            }

            nextButton.onClick(::onNextClicked)
            switchButton.onClick(::onSwitchInternal)
        }

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is State.Idle -> showLoading(false)
                    is State.Processing -> showLoading(true)
                    is State.SwitchInternal -> onSwitchInternal(it.username, it.domain)
                    is State.Success -> onExternalEmailAvailable(it.email)
                    is State.Error.Message -> onError(it.error.getUserMessage(resources))
                }.exhaustive
            }
            .onLongState(State.Processing) {
                requireContext().showToast(getString(R.string.auth_long_signup))
            }
            .launchIn(lifecycleScope)

        launchOnScreenView {
            signupViewModel.onScreenView(SignupScreenViewTotalV1.ScreenId.chooseExternalEmail)
        }
    }

    private fun onNextClicked() {
        with(binding.emailInput) {
            lifecycleScope.launch { flush() }
            hideKeyboard()
            validateEmail()
                .onFailure { setInputError() }
                .onSuccess { email -> viewModel.checkExternalEmail(email) }
        }
    }

    private fun onSwitchInternal(username: String? = null, domain: String? = null) {
        parentFragmentManager.replaceByInternalEmailChooser(creatableAccountType, username, domain)
    }

    private fun onExternalEmailAvailable(email: String) {
        showLoading(false)
        binding.nextButton.isEnabled = true
        signupViewModel.currentAccountType = AccountType.External
        signupViewModel.externalEmail = email
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
        const val ARG_INPUT_ACCOUNT_TYPE = "arg.accountType"

        operator fun invoke(
            creatableAccountType: AccountType
        ) = ChooseExternalEmailFragment().apply {
            arguments = bundleOf(
                ARG_INPUT_ACCOUNT_TYPE to creatableAccountType.name
            )
        }
    }
}
