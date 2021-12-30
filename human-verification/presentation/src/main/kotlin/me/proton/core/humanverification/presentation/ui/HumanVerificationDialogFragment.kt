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

package me.proton.core.humanverification.presentation.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.humanverification.presentation.BuildConfig
import me.proton.core.humanverification.presentation.HumanVerificationApiHost
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.DialogHumanVerificationBinding
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.humanverification.presentation.ui.VerificationResponseMessage.MessageType
import me.proton.core.humanverification.presentation.ui.VerificationResponseMessage.Type
import me.proton.core.humanverification.presentation.ui.webview.HumanVerificationWebViewClient
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationExtraParams
import me.proton.core.humanverification.presentation.viewmodel.HumanVerificationViewModel
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdType
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.client.getId
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.normSnack
import me.proton.core.presentation.utils.successSnack
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.deserializeOrNull
import me.proton.core.util.kotlin.exhaustive
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class HumanVerificationDialogFragment : ProtonDialogFragment(R.layout.dialog_human_verification) {

    private val viewModel by viewModels<HumanVerificationViewModel>()
    private val binding by viewBinding(DialogHumanVerificationBinding::bind)

    private val parsedArgs by lazy { requireArguments().get(ARGS_KEY) as Args }

    private val clientIdType: ClientIdType by lazy { ClientIdType.getByValue(parsedArgs.clientIdType) }
    private val clientId: ClientId by lazy { clientIdType.getId(parsedArgs.clientId) }
    private val sessionId: SessionId? by lazy {
        when (clientIdType) {
            ClientIdType.SESSION -> SessionId(clientId.id)
            ClientIdType.COOKIE -> null
        }.exhaustive
    }

    @Inject
    @HumanVerificationApiHost
    lateinit var humanVerificationApiHost: String

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var networkRequestOverrider: NetworkRequestOverrider

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.window?.setWindowAnimations(android.R.style.Animation_Dialog)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.apply {
                setNavigationOnClickListener {
                    setResultAndDismiss(token = null)
                }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_help -> {
                            childFragmentManager.showHelp()
                            true
                        }
                        else -> false
                    }
                }
            }
            setupWebView(humanVerificationWebView)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        webView.webViewClient = HumanVerificationWebViewClient(
            extraHeaderProvider.headers,
            viewModel.activeAltUrlForDoH,
            networkRequestOverrider,
        )
        webView.webChromeClient = HumanVerificationWebChromeClient()
        webView.addJavascriptInterface(VerificationJSInterface(), JS_INTERFACE_NAME)
        // Workaround to get transparent webview background
        webView.setBackgroundColor(Color.argb(1, 255, 255, 255))

        lifecycleScope.launch {
            val verificationParams = viewModel.getHumanVerificationExtraParams()
            loadWebView(webView, verificationParams)
        }
    }

    private fun loadWebView(webView: WebView, params: HumanVerificationExtraParams?) {
        // At the moment, this is enough to properly load the Captcha with the extra headers.
        // This behavior could change and we might need to implement a WebViewClient to act as an interceptor.
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val extraHeaders = extraHeaderProvider.headers.toMap().toMutableMap()
        val overriddenUrl = viewModel.activeAltUrlForDoH ?: parsedArgs.baseUrl
        val baseUrl = overriddenUrl ?: humanVerificationApiHost
        val darkMode = context?.resources?.configuration?.isUsingDarkMode() ?: false
        val url = buildUrl(
            baseUrl,
            parsedArgs.startToken,
            parsedArgs.verificationMethods,
            parsedArgs.recoveryEmail,
            params,
            darkMode,
        )
        webView.loadUrl(url, extraHeaders)
    }

    private fun buildUrl(
        baseURL: String,
        verificationToken: String,
        verificationMethods: List<String>,
        recoveryEmail: String?,
        params: HumanVerificationExtraParams?,
        useDarkMode: Boolean,
    ): String {
        val defaultEmail = recoveryEmail?.let { "defaultEmail" to it }
        val recoveryPhone = params?.recoveryPhone?.let { "defaultPhone" to it }
        val locale = params?.locale?.let { "locale" to it }
        val defaultCountry = params?.defaultCountry?.let { "defaultCountry" to it }
        val parameters = listOfNotNull(
            "embed" to "true",
            "token" to verificationToken,
            "methods" to verificationMethods.joinToString(","),
            defaultEmail,
            recoveryPhone,
            locale,
            defaultCountry,
            "theme" to if (useDarkMode) "1" else "2"
        ).joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
        return "$baseURL?$parameters"
    }

    private fun handleVerificationResponse(response: VerificationResponseMessage) {
        when (response.type) {
            Type.Success -> {
                val token = requireNotNull(response.payload.token)
                val tokenType = requireNotNull(response.payload.type)
                setResultAndDismiss(HumanVerificationToken(token, tokenType))
            }
            Type.Notification -> {
                val message = requireNotNull(response.payload.text)
                val messageType = requireNotNull(response.payload.type?.let { MessageType.map[it] })
                handleNotification(message, messageType)
            }
            Type.Resize -> {} // No action needed
        }
    }

    private fun handleNotification(message: String, messageType: MessageType) {
        val view = this.view ?: return
        when (messageType) {
            MessageType.Success -> view.successSnack(message)
            MessageType.Error -> view.errorSnack(message)
            else -> view.normSnack(message)
        }
    }

    override fun onBackPressed() {
        setResultAndDismiss(token = null)
    }

    private fun setResultAndDismiss(token: HumanVerificationToken?) = viewLifecycleOwner.lifecycleScope.launch {
        viewModel.onHumanVerificationResult(clientId, token).invokeOnCompletion {
            val result = HumanVerificationResult(
                clientId = clientId.id,
                clientIdType = sessionId?.let { ClientIdType.SESSION.value } ?: ClientIdType.COOKIE.value,
                token = token
            )
            val resultBundle = Bundle().apply {
                putParcelable(RESULT_HUMAN_VERIFICATION, result)
            }
            setFragmentResult(REQUEST_KEY, resultBundle)
            dismissAllowingStateLoss()
        }
    }

    /** Automatically shows or hides the progress view according to loading status of the WebView. */
    inner class HumanVerificationWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (isAdded) {
                if (newProgress == MAX_PROGRESS && isAdded) {
                    binding.progress.visibility = View.GONE
                } else {
                    binding.progress.visibility = View.VISIBLE
                }
            }
        }
    }

    /** JS Interface used for communication between the WebView contents and the client. */
    inner class VerificationJSInterface {
        /** Used as callback by all verification methods once the challenge is solved. */
        @JavascriptInterface
        fun dispatch(response: String) {
            val verificationResponse = response.deserializeOrNull<VerificationResponseMessage>()
            verificationResponse?.let { handleVerificationResponse(it) }
        }
    }

    @Parcelize
    data class Args(
        val clientId: String,
        val clientIdType: String,
        val baseUrl: String?,
        val startToken: String,
        val verificationMethods: List<String>,
        val recoveryEmail: String?,
    ) : Parcelable

    companion object {

        private const val TAG = "HumanVerificationDialogFragment"

        private const val ARGS_KEY = "args"
        private const val MAX_PROGRESS = 100

        private const val JS_INTERFACE_NAME = "AndroidInterface"
        internal const val RESULT_HUMAN_VERIFICATION = "result.HumanVerificationResult"
        internal const val REQUEST_KEY = "HumanVerificationDialogFragment.requestKey"

        operator fun invoke(
            clientId: String,
            clientIdType: String,
            baseUrl: String?,
            startToken: String,
            verificationMethods: List<String>,
            recoveryEmail: String?,
        ) = HumanVerificationDialogFragment().apply {
            arguments = bundleOf(
                ARGS_KEY to Args(clientId, clientIdType, baseUrl, startToken, verificationMethods, recoveryEmail)
            )
        }
    }
}

private fun Configuration.isUsingDarkMode(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
