/*
 * Copyright (c) 2023 Proton AG
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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.feature.IsSsoEnabled
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginBinding
import me.proton.core.auth.presentation.entity.AuthHelpResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.LoginSsoInput
import me.proton.core.auth.presentation.viewmodel.LoginViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.presentation.utils.enableProtonEdgeToEdge
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.normToast
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.presentation.utils.showToast
import me.proton.core.presentation.utils.validatePassword
import me.proton.core.presentation.utils.validateUsername
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.UiComponentProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

/**
 * Login Activity which allows users to Login to any Proton client application.
 */
@AndroidEntryPoint
@ScreenDisplayed(
    event = "fe.signin.displayed",
    priority = TelemetryPriority.Immediate
)
@ScreenClosed(
    event = "user.signin.closed",
    priority = TelemetryPriority.Immediate
)
@ViewClicked(
    event = "user.signin.clicked",
    viewIds = ["signInButton"],
    priority = TelemetryPriority.Immediate
)
@ViewFocused(
    event = "user.signin.focused",
    viewIds = ["usernameInput", "passwordInput"],
    priority = TelemetryPriority.Immediate
)
class LoginActivity : AuthActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate),
    UiComponentProductMetricsDelegateOwner {

    @Inject
    lateinit var helpOptionHandler: HelpOptionHandler

    @Inject
    lateinit var isSsoEnabled: IsSsoEnabled

    private val viewModel by viewModels<LoginViewModel>()

    private val input: LoginInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(ARG_INPUT))
    }

    private val loginSsoResultLauncher = registerForActivityResult(StartLoginSso) {
        if (it != null) onSuccess(UserId(it.userId))
    }

    private val showLongLogin: Boolean by lazy {
        applicationContext.resources.getBoolean(R.bool.core_feature_auth_signin_show_long_login_toast)
    }

    private val showTroubleshootButton: Boolean by lazy {
        applicationContext.resources.getBoolean(R.bool.core_feature_auth_signin_show_troubleshoot_button)
    }

    override val productMetricsDelegate: ProductMetricsDelegate get() = viewModel

    override val shouldShowQrLoginHelpOption: Boolean get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding.apply {
            setActionBarAuthMenu(toolbar)
            toolbar.setNavigationOnClickListener { onBackPressed() }

            addOnBackPressedCallback {
                viewModel.stopLoginWorkflow().invokeOnCompletion { finish() }
            }

            signInButton.onClick(::onSignInClicked)
            signInWithSsoButton.isVisible = viewModel.isSsoEnabled
            signInWithSsoButton.onClick {
                loginSsoResultLauncher.launch(LoginSsoInput(usernameInput.text.toString()))
            }

            usernameInput.text = input.username
            usernameInput.setOnFocusLostListener { _, _ ->
                usernameInput.validateUsername()
                    .onFailure { usernameInput.setInputError(getString(R.string.auth_login_assistive_text)) }
                    .onSuccess { usernameInput.clearInputError() }
            }
            passwordInput.setOnFocusLostListener { _, _ ->
                passwordInput.validatePassword()
                    .onFailure { passwordInput.setInputError() }
                    .onSuccess { passwordInput.clearInputError() }
            }
            passwordInput.setOnDoneActionListener { onSignInClicked() }

            blockingHelpButton.onClick { helpOptionHandler.onTroubleshoot(this@LoginActivity) }
            blockingHelpButton.setText(R.string.auth_login_troubleshhot)
        }

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onLongState<LoginViewModel.State.Processing> {
                if (showLongLogin) showToast(getString(R.string.auth_long_login))
            }
            .launchIn(lifecycleScope)

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is LoginViewModel.State.Idle -> showLoading(false)
                    is LoginViewModel.State.Processing -> showLoading(true)
                    is LoginViewModel.State.SignInWithSso -> onSignInWithSso(it)
                    is LoginViewModel.State.AccountSetupResult -> onAccountSetupResult(it.result)
                    is LoginViewModel.State.Error -> onError(
                        true,
                        it.error.getUserMessage(resources),
                        it.isPotentialBlocking
                    )

                    is LoginViewModel.State.InvalidPassword -> onWrongPassword(it.error.getUserMessage(resources))
                    is LoginViewModel.State.ExternalAccountNotSupported -> onExternalAccountNotSupported()
                }.exhaustive
            }
            .launchIn(lifecycleScope)
    }

    override fun onAuthHelpResult(authHelpResult: AuthHelpResult?) {
        super.onAuthHelpResult(authHelpResult)
        when (authHelpResult) {
            is AuthHelpResult.SignedInWithEdm -> onSuccess(UserId(authHelpResult.userId))
            is AuthHelpResult.PasswordChangeNeededAfterEdm -> onChangePassword()
            null -> Unit
        }
    }

    private fun onAccountSetupResult(result: PostLoginAccountSetup.Result) {
        when (result) {
            is PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError -> onUnlockUserError(result.error)
            is PostLoginAccountSetup.Result.Error.UserCheckError -> onUserCheckFailed(result.error)
            is PostLoginAccountSetup.Result.Need.DeviceSecret -> error("Unexpected")
            is PostLoginAccountSetup.Result.Need.ChangePassword -> onChangePassword()
            is PostLoginAccountSetup.Result.Need.ChooseUsername -> onSuccess(result.userId)
            is PostLoginAccountSetup.Result.Need.SecondFactor -> onSuccess(result.userId)
            is PostLoginAccountSetup.Result.Need.TwoPassMode -> onSuccess(result.userId)
            is PostLoginAccountSetup.Result.AccountReady -> onSuccess(result.userId)
        }.exhaustive
    }

    private fun onChangePassword() {
        showLoading(false)
        binding.passwordInput.text = ""
        supportFragmentManager.showPasswordChangeDialog(context = this)
    }

    private fun onSuccess(userId: UserId) {
        val intent = Intent().putExtra(ARG_RESULT, LoginResult(userId = userId.id))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onWrongPassword(message: String?) {
        binding.passwordInput.text = null
        onError(triggerValidation = true, message = message, isPotentialBlocking = false)
    }

    private fun onSignInWithSso(result: LoginViewModel.State.SignInWithSso) {
        showLoading(false)
        if (isSsoEnabled()) {
            normToast(result.error.getUserMessage(resources) ?: getString(R.string.auth_login_general_error))
            loginSsoResultLauncher.launch(LoginSsoInput(result.email))
        } else {
            onExternalSsoNotSupported()
        }
    }

    private fun onExternalAccountNotSupported() {
        showLoading(false)

        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.auth_login_external_account_unsupported_title)
            .setMessage(R.string.auth_login_external_account_unsupported_message)
            .setPositiveButton(R.string.auth_login_external_account_unsupported_help_action) { _, _ ->
                showExternalAccountHelpPage()
            }
            .setNegativeButton(me.proton.core.presentation.R.string.presentation_alert_cancel, null)
            .show()
    }

    private fun onExternalSsoNotSupported() {
        MaterialAlertDialogBuilder(this)
            .setCancelable(true)
            .setTitle(R.string.auth_login_external_sso_unsupported_title)
            .setMessage(R.string.auth_login_external_sso_unsupported_message)
            .setPositiveButton(R.string.auth_login_external_sso_unsupported_action, null)
            .show()
    }

    private fun showExternalAccountHelpPage() {
        openBrowserLink(getString(R.string.external_account_help_link))
    }

    override fun onError(triggerValidation: Boolean, message: String?, isPotentialBlocking: Boolean) {
        if (triggerValidation) {
            binding.apply {
                usernameInput.setInputError()
                passwordInput.setInputError()
            }
        }
        if (isPotentialBlocking && showTroubleshootButton) {
            binding.blockingHelpButton.isVisible = true
        }
        showError(message)
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            signInButton.setLoading()
        } else {
            signInButton.setIdle()
        }
        usernameInput.isEnabled = !loading
        passwordInput.isEnabled = !loading
    }

    private fun onSignInClicked() {
        with(binding) {
            hideKeyboard()
            usernameInput.clearInputError()
            passwordInput.clearInputError()
            blockingHelpButton.isVisible = false
            lifecycleScope.launch {
                binding.usernameInput.flush()
            }
            usernameInput.validateUsername()
                .onFailure { usernameInput.setInputError(getString(R.string.auth_login_assistive_text)) }
                .onSuccess(::onUsernameValidationSuccess)
        }
    }

    private fun onUsernameValidationSuccess(username: String) {
        with(binding) {
            passwordInput.validatePassword()
                .onFailure { passwordInput.setInputError() }
                .onSuccess { password ->
                    viewModel.startLoginWorkflow(
                        username = username,
                        password = password,
                        loginMetricData = null,
                        unlockUserMetricData = null,
                        userCheckMetricData = null
                    )
                }
        }
    }

    companion object {
        const val ARG_INPUT = "arg.loginInput"
        const val ARG_RESULT = "arg.loginResult"
    }
}
