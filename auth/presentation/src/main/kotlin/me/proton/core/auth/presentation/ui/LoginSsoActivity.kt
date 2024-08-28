/*
 * Copyright (c) 2023 Proton Technologies AG
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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Browser
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_DARK
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.usecase.IsSsoCustomTabEnabled
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.ActivityLoginSsoBinding
import me.proton.core.auth.presentation.entity.LoginSsoInput
import me.proton.core.auth.presentation.entity.LoginSsoResult
import me.proton.core.auth.presentation.viewmodel.LoginSsoViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.network.presentation.ui.ProtonWebViewActivity
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.observability.domain.metrics.LoginScreenViewTotal
import me.proton.core.network.presentation.ui.ProtonWebViewActivity.Companion.ResultContract
import me.proton.core.network.presentation.ui.ProtonWebViewActivity.Result
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.onSuccess
import me.proton.core.presentation.utils.validateEmail
import okhttp3.HttpUrl
import javax.inject.Inject

@AndroidEntryPoint
class LoginSsoActivity : AuthActivity<ActivityLoginSsoBinding>(ActivityLoginSsoBinding::inflate) {

    @Inject
    @BaseProtonApiUrl
    lateinit var baseApiUrl: HttpUrl

    @Inject
    lateinit var isSsoCustomTabEnabled: IsSsoCustomTabEnabled

    @Inject
    lateinit var networkPrefs: NetworkPrefs

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var sessionProvider: SessionProvider

    private val viewModel by viewModels<LoginSsoViewModel>()

    private val input: LoginSsoInput? by lazy {
        intent?.extras?.getParcelable(ARG_INPUT)
    }

    // Handling result from WebView.
    private val webViewResultLauncher = registerForActivityResult(ResultContract) { result ->
        when (result) {
            null -> viewModel.onIdentityProviderCancel()
            else -> {
                viewModel.onIdentityProviderPageLoad(result.pageLoadErrorCode)
                when (result) {
                    is Result.Cancel -> viewModel.onIdentityProviderCancel()
                    is Result.Error -> viewModel.onIdentityProviderError()
                    is Result.Success -> {
                        val email = binding.emailInput.text.toString()
                        viewModel.onIdentityProviderSuccess(email, result.url)
                    }
                }
            }
        }
    }

    // Handling result from CustomTabs.
    private val customTabResultLauncher =
        registerForActivityResult(CustomTabResultContract) { result ->
            when (result) {
                false -> viewModel.onIdentityProviderCancel()
                else -> Unit
            }
        }

    private var onNewIntentCallback: Consumer<Intent> = Consumer<Intent> { intent ->
        val email = binding.emailInput.text.toString()
        val url = intent?.data
        viewModel.onIdentityProviderSuccess(email, url?.toString().orEmpty())
    }
    private var customTabClient: CustomTabsClient? = null
    private var customTabSession: CustomTabsSession? = null
    private var callback: CustomTabsCallback = object : CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            when (navigationEvent) {
                NAVIGATION_ABORTED -> viewModel.onIdentityProviderCancel()
                NAVIGATION_FAILED -> viewModel.onIdentityProviderError()
                NAVIGATION_FINISHED -> viewModel.onIdentityProviderPageLoad(null)
            }
        }
    }

    private val connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabClient = client
            customTabClient?.warmup(0)
            customTabSession = client.newSession(callback)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            customTabClient = null
            customTabSession = null
        }
    }

    private fun bindCustomTabService(context: Context) {
        if (customTabClient != null) return
        val packageName: String = CustomTabsClient.getPackageName(context, null) ?: return
        CustomTabsClient.bindCustomTabsService(context, packageName, connection)
        addOnNewIntentListener(onNewIntentCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindCustomTabService(this)
        binding.apply {
            setActionBarAuthMenu(toolbar)
            toolbar.setNavigationOnClickListener { finish() }
            signInButton.onClick(::onSignInClicked)
            emailInput.text = input?.email
            signInWithPasswordButton.onClick { finish() }
        }

        viewModel.state.flowWithLifecycle(lifecycle).onEach {
            when (it) {
                is LoginSsoViewModel.State.Error -> onError(it)
                is LoginSsoViewModel.State.Idle -> showLoading(false)
                is LoginSsoViewModel.State.Processing -> showLoading(true)
                is LoginSsoViewModel.State.SignInWithSrp -> onSignInWithSrp(it)
                is LoginSsoViewModel.State.StartToken -> onStartToken(it)
                is LoginSsoViewModel.State.AccountSetupResult -> onAccountSetupResult(it)
            }
        }.launchIn(lifecycleScope)

        launchOnScreenView {
            viewModel.onScreenView(LoginScreenViewTotal.ScreenId.signInWithSso)
        }
    }

    private fun onAccountSetupResult(result: LoginSsoViewModel.State.AccountSetupResult) {
        when (result.result) {
            is PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError -> showError(null)
            is PostLoginAccountSetup.Result.Error.UserCheckError -> showError(result.result.error.localizedMessage)
            is PostLoginAccountSetup.Result.Need.ChangePassword -> error("Unexpected")
            is PostLoginAccountSetup.Result.Need.ChooseUsername -> error("Unexpected")
            is PostLoginAccountSetup.Result.Need.SecondFactor -> error("Unexpected")
            is PostLoginAccountSetup.Result.Need.TwoPassMode -> error("Unexpected")
            is PostLoginAccountSetup.Result.Need.DeviceSecret -> onSuccess(result.userId)
            is PostLoginAccountSetup.Result.AccountReady -> onSuccess(result.userId)
        }
    }

    override fun showLoading(loading: Boolean) = with(binding) {
        if (loading) {
            signInButton.setLoading()
        } else {
            signInButton.setIdle()
        }
        emailInput.isEnabled = !loading
    }

    private fun onSignInClicked() {
        with(binding) {
            hideKeyboard()
            emailInput.clearInputError()
            emailInput.validateEmail()
                .onFailure { emailInput.setInputError(getString(R.string.auth_login_sso_assistive_text)) }
                .onSuccess { viewModel.startLoginWorkflow(it) }
        }
    }

    private fun onError(error: LoginSsoViewModel.State.Error) {
        showError(error.error.getUserMessage(resources))
    }

    private fun onSignInWithSrp(state: LoginSsoViewModel.State.SignInWithSrp) {
        showError(message = state.error.getUserMessage(resources), useToast = true)
        finish()
    }

    private fun onStartToken(state: LoginSsoViewModel.State.StartToken) = lifecycleScope.launch {
        val sessionId = requireNotNull(sessionProvider.getSessionId(userId = null))
        val session = requireNotNull(sessionProvider.getSession(sessionId))
        val scheme = getString(R.string.core_feature_auth_sso_redirect_scheme)
        val host = baseApiUrl.toUri().host
        val url = "$baseApiUrl$AUTH_SSO_URL${state.token}?$REDIRECT_BASE_URL=$scheme://$host"
        val uidHeader = UID_HEADER to session.sessionId.id
        val authHeader = AUTH_HEADER to "$BEARER_HEADER ${session.accessToken}"

        if (isSsoCustomTabEnabled() && customTabClient != null) {
            customTabResultLauncher.launch(
                CustomTabResultContract.Input(
                    url = url,
                    headers = bundleOf(uidHeader, authHeader),
                    session = customTabSession
                )
            )
        } else {
            webViewResultLauncher.launch(
                ProtonWebViewActivity.Input(
                    url = url,
                    successUrlRegex = PREFIX_LOGIN_TOKEN,
                    errorUrlRegex = PREFIX_LOGIN_ERROR,
                    extraHeaders = mapOf(uidHeader, authHeader),
                    javaScriptEnabled = true,
                    domStorageEnabled = true,
                    shouldOpenLinkInBrowser = false
                )
            )
        }
        viewModel.onScreenView(LoginScreenViewTotal.ScreenId.ssoIdentityProvider)
        viewModel.onIdentityProviderStarted()
    }

    private fun onSuccess(userId: UserId) {
        setResultAndFinish(userId)
    }

    private fun setResultAndFinish(userId: UserId) {
        setResult(RESULT_OK, Intent().putExtra(ARG_RESULT, LoginSsoResult(userId.id)))
        finish()
    }

    companion object {
        private const val UID_HEADER = "x-pm-uid"
        private const val AUTH_SSO_URL = "auth/sso/"
        private const val REDIRECT_BASE_URL = "FinalRedirectBaseUrl"
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER_HEADER = "Bearer"
        private const val TOKEN = "token"
        private const val PREFIX_LOGIN_TOKEN = "login#$TOKEN"
        private const val PREFIX_LOGIN_ERROR = "login#error"
        const val ARG_INPUT = "arg.loginSsoInput"
        const val ARG_RESULT = "arg.loginSsoResult"
    }

    object CustomTabResultContract :
        ActivityResultContract<CustomTabResultContract.Input, Boolean>() {
        data class Input(val url: String, val headers: Bundle, val session: CustomTabsSession?)

        override fun createIntent(context: Context, input: Input): Intent =
            CustomTabsIntent.Builder().apply {
                input.session?.let { setSession(it) }
                setShowTitle(false)
                setBookmarksButtonEnabled(false)
                setDownloadButtonEnabled(false)
                setUrlBarHidingEnabled(false)
                setColorScheme(COLOR_SCHEME_DARK)
                setShareState(SHARE_STATE_OFF)
            }.build().apply {
                intent.putExtra(Browser.EXTRA_HEADERS, input.headers)
                intent.data = input.url.toUri()
            }.intent

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
            return resultCode == Activity.RESULT_OK
        }
    }
}
