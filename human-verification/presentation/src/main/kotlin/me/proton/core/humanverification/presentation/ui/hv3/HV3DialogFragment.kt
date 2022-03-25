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
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.animation.AnimationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.humanverification.presentation.BuildConfig
import me.proton.core.humanverification.presentation.HumanVerificationApiHost
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.DialogHumanVerificationV3Binding
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.humanverification.presentation.ui.REQUEST_KEY
import me.proton.core.humanverification.presentation.ui.RESULT_HUMAN_VERIFICATION
import me.proton.core.humanverification.presentation.ui.hv3.HV3ResponseMessage.MessageType
import me.proton.core.humanverification.presentation.ui.hv3.HV3ResponseMessage.Type
import me.proton.core.humanverification.presentation.ui.webview.HumanVerificationWebViewClient
import me.proton.core.humanverification.presentation.utils.showHelp
import me.proton.core.humanverification.presentation.viewmodel.hv3.HV3ViewModel
import me.proton.core.humanverification.presentation.viewmodel.hv3.HV3ExtraParams
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
class HV3DialogFragment : ProtonDialogFragment(R.layout.dialog_human_verification_v3) {

    private val viewModel by viewModels<HV3ViewModel>()
    private val binding by viewBinding(DialogHumanVerificationV3Binding::bind)

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
    lateinit var humanVerificationBaseUrl: String

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var networkRequestOverrider: NetworkRequestOverrider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navigationIconId = if (parsedArgs.isPartOfFlow)
            R.drawable.ic_proton_arrow_back
        else
            R.drawable.ic_proton_close

        if (savedInstanceState == null) {
            setLoading(true)
        }

        with(binding) {
            toolbar.apply {
                navigationIcon = AppCompatResources.getDrawable(requireContext(), navigationIconId)
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

        observeNestedScroll()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        val overriddenUrl = viewModel.activeAltUrlForDoH ?: parsedArgs.baseUrl
        val baseUrl = overriddenUrl ?: humanVerificationBaseUrl

        webView.webViewClient = HumanVerificationWebViewClient(
            Uri.parse(baseUrl).host!!,
            extraHeaderProvider.headers,
            viewModel.activeAltUrlForDoH,
            networkRequestOverrider,
            onResourceLoadingError = { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) { setLoading(false) }
            },
            onWebLocationChanged = {}
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

    private fun handleVerificationResponse(response: HV3ResponseMessage) {
        when (response.type) {
            Type.Success -> {
                val token = requireNotNull(response.payload?.token)
                val tokenType = requireNotNull(response.payload?.type)
                setResultAndDismiss(HumanVerificationToken(token, tokenType))
            }
            Type.Notification -> {
                val message = requireNotNull(response.payload?.text)
                val messageType = requireNotNull(response.payload?.type?.let { MessageType.map[it] })
                handleNotification(message, messageType)
            }
            Type.Loaded -> setLoading(false)
            Type.Resize -> {} // No action needed
        }
    }

    private fun setLoading(loading: Boolean) {
        with(binding) {
            humanVerificationWebView.isVisible = !loading
            progress.isVisible = loading
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
        with (binding.humanVerificationWebView) {
            if (canGoBack()) goBack()
            else setResultAndDismiss(token = null)
        }
    }

    private fun setResultAndDismiss(token: HumanVerificationToken?) {
        val result = HumanVerificationResult(
            clientId = clientId.id,
            clientIdType = sessionId?.let { ClientIdType.SESSION.value } ?: ClientIdType.COOKIE.value,
            token = token
        )
        val resultBundle = Bundle().apply {
            putParcelable(RESULT_HUMAN_VERIFICATION, result)
        }
        setFragmentResult(REQUEST_KEY, resultBundle)

        lifecycleScope.launch {
            viewModel.onHumanVerificationResult(clientId, token)
            // Extra delay for better UX while replacing underlying fragments
            delay(100)
            dismissAllowingStateLoss()
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
            val verificationResponse = response.deserializeOrNull<HV3ResponseMessage>()
            verificationResponse?.let {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                    handleVerificationResponse(it)
                }
            }
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
        val isPartOfFlow: Boolean,
    ) : Parcelable

    companion object {

        private const val ARGS_KEY = "args"

        private const val JS_INTERFACE_NAME = "AndroidInterface"

        operator fun invoke(
            clientId: String,
            clientIdType: String,
            baseUrl: String?,
            startToken: String,
            verificationMethods: List<String>,
            recoveryEmail: String?,
            isPartOfFlow: Boolean,
        ) = HV3DialogFragment().apply {
            arguments = bundleOf(
                ARGS_KEY to Args(
                    clientId,
                    clientIdType,
                    baseUrl,
                    startToken,
                    verificationMethods,
                    recoveryEmail,
                    isPartOfFlow,
                )
            )
        }
    }
}

private fun Configuration.isUsingDarkMode(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
