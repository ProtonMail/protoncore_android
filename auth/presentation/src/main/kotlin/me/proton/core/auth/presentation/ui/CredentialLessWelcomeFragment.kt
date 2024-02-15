/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentCredentialLessWelcomeBinding
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.auth.presentation.entity.AddAccountResult
import me.proton.core.auth.presentation.entity.AddAccountWorkflow
import me.proton.core.auth.presentation.onLoginResult
import me.proton.core.auth.presentation.onOnSignUpResult
import me.proton.core.auth.presentation.ui.signup.showTermsConditions
import me.proton.core.auth.presentation.util.setTextWithAnnotatedLink
import me.proton.core.auth.presentation.viewmodel.CredentialLessViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.SnackbarLength
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.getUserMessage
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import javax.inject.Inject

@AndroidEntryPoint
@ProductMetrics(
    group = "account.any.signup",
    flow = "mobile_signup_full"
)
@ScreenDisplayed(
    event = "fe.add_account.displayed",
    priority = TelemetryPriority.Immediate
)
@ScreenClosed(
    event = "user.add_account.closed",
    priority = TelemetryPriority.Immediate
)
@ViewClicked(
    event = "user.add_account.clicked",
    viewIds = ["sign_in_guest", "sign_in", "sign_up"],
    priority = TelemetryPriority.Immediate
)
internal class CredentialLessWelcomeFragment : ProtonFragment(R.layout.fragment_credential_less_welcome) {

    @Inject
    lateinit var authOrchestrator: AuthOrchestrator

    private val viewModel by viewModels<CredentialLessViewModel>()

    private val binding by viewBinding(FragmentCredentialLessWelcomeBinding::bind)

    private val input by lazy {
        requireNotNull(requireArguments().getParcelable<AddAccountInput>(ARG_ADD_ACCOUNT_INPUT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authOrchestrator.register(this)
        authOrchestrator.onLoginResult {
            if (it != null) onSuccess(it.userId, AddAccountWorkflow.SignIn)
        }
        authOrchestrator.onOnSignUpResult {
            if (it != null) onSuccess(it.userId, AddAccountWorkflow.SignUp)
        }
    }

    override fun onDestroy() {
        authOrchestrator.unregister()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.noLogsButton.onClick {
            requireContext().openBrowserLink(getString(R.string.vpn_no_logs_link))
        }
        binding.signInGuest.onClick {
            viewModel.startLoginLessWorkflow()
        }
        binding.signIn.onClick {
            authOrchestrator.startLoginWorkflow(input.requiredAccountType, input.loginUsername)
        }
        binding.signUp.onClick {
            authOrchestrator.startSignupWorkflow(input.creatableAccountType)
        }
        binding.terms.setTextWithAnnotatedLink(R.string.auth_credentialless_terms, "terms") {
            parentFragmentManager.showTermsConditions()
        }

        viewModel.state.onEach {
            when (it) {
                is CredentialLessViewModel.State.Idle -> showLoading(
                    loading = false
                )

                is CredentialLessViewModel.State.Processing -> showLoading(
                    loading = true
                )

                is CredentialLessViewModel.State.AccountSetupResult -> onAccountSetupResult(
                    userId = it.userId,
                    result = it.result
                )

                is CredentialLessViewModel.State.CredentialLessDisabled -> onCredentialLessDisabled(
                    message = it.error.getUserMessage(resources),
                )

                is CredentialLessViewModel.State.Error -> onError(
                    message = it.error.getUserMessage(resources),
                )
            }
        }.launchInViewLifecycleScope()
    }

    private fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            signInGuest.setLoading()
        } else {
            signInGuest.setIdle()
        }
    }

    private fun showError(
        message: String?,
        action: String? = null,
        actionOnClick: (() -> Unit)? = null
    ) = with(binding) {
        root.errorSnack(
            message = message ?: getString(R.string.auth_login_general_error),
            action = action,
            actionOnClick = actionOnClick,
            length = when {
                action != null && actionOnClick != null -> SnackbarLength.INDEFINITE
                else -> SnackbarLength.LONG
            }
        )
    }

    private fun onCredentialLessDisabled(message: String?) {
        showLoading(false)
        showError(message)
        binding.signInGuest.isVisible = false
        binding.signUp.isVisible = true
    }

    private fun onError(message: String?) {
        showLoading(false)
        showError(message)
    }

    private fun onAccountSetupResult(
        userId: UserId,
        result: PostLoginAccountSetup.UserCheckResult
    ) {
        when (result) {
            is PostLoginAccountSetup.UserCheckResult.Error -> onUserCheckFailed(
                error = result
            )

            is PostLoginAccountSetup.UserCheckResult.Success -> onSuccess(
                userId = userId.id,
                workflow = AddAccountWorkflow.CredentialLess
            )
        }
    }

    private fun onUserCheckFailed(
        error: PostLoginAccountSetup.UserCheckResult.Error,
    ) {
        showLoading(false)
        when (val action = error.action) {
            null -> showError(error.localizedMessage)
            is UserCheckAction.OpenUrl -> showError(
                message = error.localizedMessage,
                action = action.name,
                actionOnClick = { context?.openBrowserLink(action.url) },
            )
        }
    }

    private fun onSuccess(userId: String, workflow: AddAccountWorkflow) {
        val resultBundle = bundleOf(
            ARG_ADD_ACCOUNT_RESULT to AddAccountResult(userId = userId, workflow = workflow)
        )
        setFragmentResult(CREDENTIAL_LESS_REQUEST_KEY, resultBundle)
    }

    companion object {
        const val CREDENTIAL_LESS_REQUEST_KEY = "CREDENTIAL_LESS_REQUEST_KEY"
        const val ARG_ADD_ACCOUNT_RESULT = "ARG_ADD_ACCOUNT_RESULT"

        private const val ARG_ADD_ACCOUNT_INPUT = "ARG_ADD_ACCOUNT_INPUT"

        operator fun invoke(input: AddAccountInput) = CredentialLessWelcomeFragment().apply {
            arguments = bundleOf(ARG_ADD_ACCOUNT_INPUT to input)
        }
    }
}
