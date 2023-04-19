/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.ui.hv3

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.annotation.MainThread
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.animation.AnimationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.launch
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.humanverification.presentation.BuildConfig
import me.proton.core.humanverification.presentation.HumanVerificationApiHost
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.DialogHumanVerificationV3Binding
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.humanverification.presentation.ui.common.HumanVerificationWebViewClient
import me.proton.core.humanverification.presentation.ui.common.REQUEST_KEY
import me.proton.core.humanverification.presentation.ui.common.RESULT_HUMAN_VERIFICATION
import me.proton.core.humanverification.presentation.ui.common.WebResponseError
import me.proton.core.humanverification.presentation.ui.hv3.HV3ResponseMessage.MessageType
import me.proton.core.humanverification.presentation.ui.hv3.HV3ResponseMessage.Type
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.utils.toHvPageLoadStatus
import me.proton.core.humanverification.presentation.viewmodel.hv3.HV3ExtraParams
import me.proton.core.humanverification.presentation.viewmodel.hv3.HV3ViewModel
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.client.ClientIdType
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.client.getId
import me.proton.core.observability.domain.metrics.HvPageLoadTotal
import me.proton.core.presentation.ui.ProtonDialogFragment
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.launchOnScreenView
import me.proton.core.presentation.utils.normSnack
import me.proton.core.presentation.utils.successSnack
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.util.kotlin.deserializeOrNull
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class HV3DialogFragment : ProtonDialogFragment(R.layout.dialog_human_verification_v3) {

    @Inject
    @HumanVerificationApiHost
    lateinit var humanVerificationBaseUrl: String

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var networkRequestOverrider: NetworkRequestOverrider

    private val viewModel by viewModels<HV3ViewModel>()
    private val binding by viewBinding(DialogHumanVerificationV3Binding::bind)

    private val parsedArgs by lazy { requireArguments().get(ARGS_KEY) as Args }

    private val clientIdType: ClientIdType by lazy { ClientIdType.getByValue(parsedArgs.clientIdType) }
    private val clientId: ClientId by lazy { clientIdType.getId(parsedArgs.clientId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            setLoading(true)
        }

        with(binding) {
            toolbar.apply {
                setNavigationIcon(R.drawable.ic_proton_close)
                setNavigationOnClickListener {
                    setResult(token = null, cancelled = true)
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

        observeNestedScroll()
        launchOnScreenView { viewModel.onScreenView() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        val overriddenUrl = viewModel.activeAltUrlForDoH
        val baseUrl = overriddenUrl ?: humanVerificationBaseUrl
        val apiHost = requireNotNull(Uri.parse(baseUrl).host)

        webView.webViewClient = HumanVerificationWebViewClient(
            apiHost,
            extraHeaderProvider.headers,
            viewModel.activeAltUrlForDoH,
            networkRequestOverrider,
            onResourceLoadingError = { _, response ->
                lifecycleScope.launch { handleResourceLoadingError(response) }
            },
            verifyAppUrl = humanVerificationBaseUrl
        )
        webView.addJavascriptInterface(VerificationJSInterface(), JS_INTERFACE_NAME)
        // Workaround to get transparent webview background
        webView.setBackgroundColor(Color.argb(1, 255, 255, 255))

        lifecycleScope.launch {
            val verificationParams = viewModel.getHumanVerificationExtraParams()
            loadWebView(webView, baseUrl, verificationParams)
        }
    }

    private fun loadWebView(webView: WebView, baseUrl: String, params: HV3ExtraParams?) {
        // At the moment, this is enough to properly load the Captcha with the extra headers.
        // This behavior could change and we might need to implement a WebViewClient to act as an interceptor.
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        val extraHeaders = extraHeaderProvider.headers.toMap().toMutableMap()
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
        params: HV3ExtraParams?,
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
            "theme" to if (useDarkMode) "1" else "2",
            if (params?.useVPNTheme == true) "vpn" to "true" else null
        ).joinToString("&") { (key, value) ->
            "$key=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
        }
        return "$baseURL?$parameters"
    }

    @MainThread
    private fun handleResourceLoadingError(error: WebResponseError?) {
        this.view ?: return
        view?.errorSnack(R.string.presentation_connectivity_issues)
        setLoading(false)
        viewModel.onPageLoad(error.toHvPageLoadStatus())
    }

    @MainThread
    private fun handleVerificationResponse(response: HV3ResponseMessage) {
        this.view ?: return
        when (response.type) {
            Type.Success -> {
                val token = requireNotNull(response.payload?.token)
                val tokenType = requireNotNull(response.payload?.type)
                setResult(HumanVerificationToken(token, tokenType), cancelled = false)
            }
            Type.Notification -> {
                val message = requireNotNull(response.payload?.text)
                val messageType = requireNotNull(response.payload?.type?.let { MessageType.map[it] })
                handleNotification(message, messageType)
            }
            Type.Loaded -> {
                setLoading(false)
                viewModel.onPageLoad(HvPageLoadTotal.Status.http2xx)
            }
            Type.Resize -> {} // No action needed
        }
    }

    @MainThread
    private fun handleNotification(message: String, messageType: MessageType) {
        val view = this.view ?: return
        when (messageType) {
            MessageType.Success -> view.successSnack(message)
            MessageType.Error -> view.errorSnack(message)
            else -> view.normSnack(message)
        }
    }

    private fun setLoading(loading: Boolean) {
        with(binding) {
            humanVerificationWebView.isVisible = !loading
            progress.isVisible = loading
        }
    }

    override fun onBackPressed() {
        with(binding.humanVerificationWebView) {
            if (canGoBack()) goBack()
            else setResult(token = null, cancelled = true)
        }
    }

    private fun setResult(token: HumanVerificationToken?, cancelled: Boolean) {
        lifecycleScope.launch {
            viewModel.onHumanVerificationResult(clientId, token, cancelled)
        }.invokeOnCompletion { error ->
            setFragmentResult(REQUEST_KEY, Bundle().apply {
                if (error == null) {
                    val result = HumanVerificationResult(clientId.id, clientIdType.value, token)
                    putParcelable(RESULT_HUMAN_VERIFICATION, result)
                }
            })
        }
    }

    private fun observeNestedScroll() {
        // Since liftOnScroll doesn't seem to work properly with WebViews, we'll animate it manually
        var previousAnimator: ValueAnimator? = null
        binding.scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            val context = this.context ?: return@setOnScrollChangeListener
            val valueAnimator = when {
                scrollY <= 0 && oldScrollY > 0 -> ValueAnimator.ofFloat(v.elevation, 0f)
                scrollY > 0 && oldScrollY <= 0 -> {
                    val toValue = context.resources.getDimension(com.google.android.material.R.dimen.design_appbar_elevation)
                    ValueAnimator.ofFloat(v.elevation, toValue)
                }
                else -> return@setOnScrollChangeListener
            }.also {
                it.duration = context.resources.getInteger(
                    com.google.android.material.R.integer.app_bar_elevation_anim_duration
                ).toLong()
                it.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
                it.addUpdateListener {
                    binding.appbar.elevation = it.animatedValue as Float
                    binding.humanVerificationWebView.postInvalidateOnAnimation()
                }
            }
            previousAnimator?.cancel()
            valueAnimator.start()
            previousAnimator = valueAnimator
        }
    }

    /** JS Interface used for communication between the WebView contents and the client. */
    inner class VerificationJSInterface {
        /** Used as callback by all verification methods once the challenge is solved. */
        @JavascriptInterface
        fun dispatch(response: String) {
            response.deserializeOrNull<HV3ResponseMessage>()?.let {
                lifecycleScope.launch { handleVerificationResponse(it) }
            }
        }
    }

    @Parcelize
    data class Args(
        val clientId: String,
        val clientIdType: String,
        val startToken: String,
        val verificationMethods: List<String>,
        val recoveryEmail: String?,
    ) : Parcelable

    companion object {

        private const val ARGS_KEY = "args"

        private const val JS_INTERFACE_NAME = "AndroidInterface"

        operator fun invoke(
            clientId: String,
            clientIdType: String,
            startToken: String,
            verificationMethods: List<String>,
            recoveryEmail: String?,
        ) = HV3DialogFragment().apply {
            arguments = bundleOf(
                ARGS_KEY to Args(
                    clientId,
                    clientIdType,
                    startToken,
                    verificationMethods,
                    recoveryEmail,
                )
            )
        }
    }
}

private fun Configuration.isUsingDarkMode(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
