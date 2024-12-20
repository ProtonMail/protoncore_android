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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.usecase.UserCheckAction
import me.proton.core.auth.domain.usecase.UserCheckAction.OpenUrl
import me.proton.core.auth.presentation.HelpOptionHandler
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.compose.LoginInputUsernameAction
import me.proton.core.auth.presentation.compose.LoginRoutes
import me.proton.core.auth.presentation.compose.LoginRoutes.addLoginInputPasswordScreen
import me.proton.core.auth.presentation.compose.LoginRoutes.addLoginInputUsernameScreen
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.compose.theme.AppTheme
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.LoginScreenViewTotal
import me.proton.core.observability.domain.metrics.LoginSsoIdentityProviderPageLoadTotal
import me.proton.core.observability.domain.metrics.LoginSsoIdentityProviderResultTotal
import me.proton.core.observability.domain.metrics.LoginSsoIdentityProviderResultTotal.Status
import me.proton.core.presentation.utils.SnackbarLength
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.openBrowserLink
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.compose.LocalProductMetricsDelegateOwner
import okhttp3.HttpUrl
import javax.inject.Inject

@AndroidEntryPoint
class LoginTwoStepActivity : WebPageListenerActivity(), ProductMetricsDelegateOwner {

    @Inject
    lateinit var appTheme: AppTheme

    @Inject
    @BaseProtonApiUrl
    lateinit var baseApiUrl: HttpUrl

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var sessionProvider: SessionProvider

    @Inject
    lateinit var helpOptionHandler: HelpOptionHandler

    @Inject
    lateinit var observabilityManager: ObservabilityManager

    @Inject
    lateinit var telemetryManager: TelemetryManager

    override val productMetricsDelegate = object: ProductMetricsDelegate {
        override val telemetryManager: TelemetryManager get() = this@LoginTwoStepActivity.telemetryManager
        override val productGroup: String = "account.any.signup"
        override val productFlow: String = "mobile_signup_full"
    }

    private val input: LoginInput by lazy {
        requireNotNull(intent?.extras?.getParcelable(LoginActivity.ARG_INPUT))
    }

    private val loginAction = MutableSharedFlow<LoginInputUsernameAction>(replay = 1)

    private fun emitAction(action: LoginInputUsernameAction) {
        lifecycleScope.launch { loginAction.emit(action) }
    }

    override fun onWebPageLoad(errorCode: Int?) {
        observabilityManager.enqueue(LoginSsoIdentityProviderPageLoadTotal(errorCode))
    }

    override fun onWebPageCancel() {
        observabilityManager.enqueue(LoginSsoIdentityProviderResultTotal(Status.cancel))
    }

    override fun onWebPageError() {
        observabilityManager.enqueue(LoginSsoIdentityProviderResultTotal(Status.error))
    }

    override fun onWebPageSuccess(url: String) {
        observabilityManager.enqueue(LoginSsoIdentityProviderResultTotal(Status.success))
        // Ex: url = "proton://app-api.proton.domain/sso/login#token=token"
        val params = url.toUri().fragment?.split("&")?.associate { param ->
            val (key, value) = param.split("=")
            Pair(key, value)
        }
        val token = requireNotNull(params?.getValue("token"))
        emitAction(LoginInputUsernameAction.SetToken(token))
    }

    private fun onOpenWebPage(info: AuthInfo.Sso) = lifecycleScope.launch {
        val sessionId = requireNotNull(sessionProvider.getSessionId(userId = null))
        val session = requireNotNull(sessionProvider.getSession(sessionId))
        val scheme = getString(R.string.core_app_scheme)
        val host = baseApiUrl.toUri().host
        val url = "$baseApiUrl$AUTH_SSO_URL${info.token}?$REDIRECT_BASE_URL=$scheme://$host"
        val uidHeader = UID_HEADER to session.sessionId.id
        val authHeader = AUTH_HEADER to "$BEARER_HEADER ${session.accessToken}"
        openWebPageUrl(
            url = url,
            successUrlRegex = PREFIX_LOGIN_TOKEN,
            errorUrlRegex = PREFIX_LOGIN_ERROR,
            extraHeaders = arrayOf(uidHeader, authHeader),
        )
        observabilityManager.enqueue(LoginScreenViewTotal(LoginScreenViewTotal.ScreenId.ssoIdentityProvider))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback { onClose() }

        setContent {
            appTheme {
                val navController = rememberNavController()
                CompositionLocalProvider(LocalProductMetricsDelegateOwner provides this@LoginTwoStepActivity) {
                    NavHost(
                        navController = navController,
                        startDestination = LoginRoutes.Route.Login.Deeplink
                    ) {
                        addLoginInputUsernameScreen(
                            username = input.username,
                            navController = navController,
                            onClose = { onBackPressed() },
                            onErrorMessage = { message, action -> onErrorMessage(message, action) },
                            onSuccess = { onSuccess(it) },
                            onNavigateToHelp = { onHelpClicked() },
                            onNavigateToSso = { onOpenWebPage(it) },
                            onNavigateToForgotUsername = { onForgotUsername() },
                            onNavigateToTroubleshoot = { onTroubleshoot() },
                            onNavigateToExternalEmailNotSupported = { onExternalEmailNotSupported() },
                            onNavigateToExternalSsoNotSupported = { onExternalSsoNotSupported() },
                            onNavigateToChangePassword = { onChangePassword() },
                            externalAction = loginAction
                        )
                        addLoginInputPasswordScreen(
                            navController = navController,
                            onErrorMessage = { message, action -> onErrorMessage(message, action) },
                            onSuccess = { onSuccess(it) },
                            onNavigateToHelp = { onHelpClicked() },
                            onNavigateToForgotPassword = { onForgotPassword() },
                            onNavigateToTroubleshoot = { onTroubleshoot() },
                            onNavigateToExternalEmailNotSupported = { onExternalEmailNotSupported() },
                            onNavigateToExternalSsoNotSupported = { onExternalSsoNotSupported() },
                            onNavigateToChangePassword = { onChangePassword() }
                        )
                    }
                }
            }
        }
    }

    private fun onClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun onErrorMessage(message: String?, action: UserCheckAction?) {
        when (action) {
            null -> errorToast(message ?: getString(R.string.auth_login_general_error))
            is OpenUrl -> errorAction(message, action.name) { openBrowserLink(action.url) }
        }
    }

    private fun onSuccess(userId: UserId) {
        val intent = Intent().putExtra(LoginActivity.ARG_RESULT, LoginResult(userId.id))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onHelpClicked() {
        startActivity(Intent(this, AuthHelpActivity::class.java))
    }

    private fun onForgotUsername() {
        helpOptionHandler.onForgotUsername(this)
    }

    private fun onForgotPassword() {
        helpOptionHandler.onForgotPassword(this)
    }

    private fun onTroubleshoot() {
        helpOptionHandler.onTroubleshoot(this)
    }

    private fun onChangePassword() {
        supportFragmentManager.showPasswordChangeDialog(context = this)
    }

    private fun onExternalEmailNotSupported() {
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(R.string.auth_login_external_account_unsupported_title)
            .setMessage(R.string.auth_login_external_account_unsupported_message)
            .setPositiveButton(R.string.auth_login_external_account_unsupported_help_action) { _, _ ->
                openBrowserLink(getString(R.string.external_account_help_link))
            }
            .setNegativeButton(R.string.presentation_alert_cancel, null)
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

    private fun errorAction(
        message: String?,
        action: String,
        actionOnClick: (() -> Unit)
    ) {
        getContentView()?.errorSnack(
            message = message ?: getString(R.string.auth_login_general_error),
            action = action,
            actionOnClick = actionOnClick,
            length = SnackbarLength.INDEFINITE
        )
    }

    private fun getContentView(): View? = window.decorView
        .findViewById<ViewGroup>(android.R.id.content)
        .getChildAt(0)

    companion object {
        private const val UID_HEADER = "x-pm-uid"
        private const val AUTH_SSO_URL = "auth/sso/"
        private const val REDIRECT_BASE_URL = "FinalRedirectBaseUrl"
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER_HEADER = "Bearer"
        private const val TOKEN = "token"
        private const val PREFIX_LOGIN_TOKEN = "login#$TOKEN"
        private const val PREFIX_LOGIN_ERROR = "login#error"
    }
}
