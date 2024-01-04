package me.proton.core.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.presentation.databinding.ProtonWebviewActivityBinding
import me.proton.core.presentation.ui.webview.ProtonWebViewClient
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.ByteArrayInputStream
import javax.inject.Inject

@AndroidEntryPoint
class ProtonWebViewActivity : ProtonSecureActivity<ProtonWebviewActivityBinding>(
    ProtonWebviewActivityBinding::inflate
) {

    @Inject
    @BaseProtonApiUrl
    lateinit var baseApiUrl: HttpUrl

    @Inject
    lateinit var networkPrefs: NetworkPrefs

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    private val input: Input by lazy { requireNotNull(intent?.extras?.getParcelable(ARG_INPUT)) }
    private val successUrlRegex by lazy { input.successUrlRegex?.toRegex() }
    private val errorUrlRegex by lazy { input.errorUrlRegex?.toRegex() }

    private var pageLoadErrorCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { onCancel() }
        binding.webView.setAllowForceDark()
        binding.webView.setupWebViewClient()
    }

    private fun WebView.setupWebViewClient() {
        with(settings) {
            javaScriptEnabled = input.javaScriptEnabled
            domStorageEnabled = input.domStorageEnabled
        }
        with(CookieManager.getInstance()) {
            if (input.removeAllCookies) { removeAllCookies(null) }
            setAcceptCookie(input.acceptCookie)
        }
        webViewClient = CustomWebViewClient(
            ::shouldInterceptRequest,
            ::onPageLoadSuccess,
            ::onPageLoadError,
        ).apply {
            shouldOpenLinkInBrowser = input.shouldOpenLinkInBrowser
        }
        val inputUrl = input.url.toHttpUrl()
        val alternativeUrl = networkPrefs.activeAltBaseUrl?.toHttpUrl()
        val url = when {
            !input.shouldUseAlternativeUrl -> input.url
            alternativeUrl == null -> input.url
            inputUrl.host != baseApiUrl.host -> input.url
            else -> inputUrl.newBuilder().host(alternativeUrl.host).build().toString()
        }
        loadUrl(url, input.extraHeaders)
    }

    private fun shouldInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
        val url = request.url.toString()
        var match = 0
        successUrlRegex?.takeIf { url.contains(it) }?.let { onSuccess(url); match++ }
        errorUrlRegex?.takeIf { url.contains(it) }?.let { onError(url); match++ }
        return if (match > 0)
            WebResourceResponse("", "", ByteArrayInputStream(byteArrayOf()))
        else null
    }

    private fun onPageLoadSuccess() = Unit

    private fun onPageLoadError(errorCode: Int?) {
        pageLoadErrorCode = errorCode
    }

    private fun onCancel() {
        setResultAndFinish(Result.Cancel(pageLoadErrorCode))
    }

    private fun onSuccess(url: String) {
        setResultAndFinish(Result.Success(url = url, pageLoadErrorCode))
    }

    private fun onError(url: String) {
        setResultAndFinish(Result.Error(url = url, pageLoadErrorCode))
    }

    private fun setResultAndFinish(result: Result) {
        setResult(RESULT_OK, Intent().putExtra(ARG_RESULT, result))
        finish()
    }

    inner class CustomWebViewClient(
        private val shouldInterceptRequest: (request: WebResourceRequest) -> WebResourceResponse?,
        private val onPageLoadSuccess: () -> Unit,
        private val onPageLoadError: (Int?) -> Unit,
    ) : ProtonWebViewClient(networkPrefs, extraHeaderProvider) {

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return shouldInterceptRequest.invoke(request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val overrideUrl = shouldInterceptRequest.invoke(request) != null
            return overrideUrl || super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.progress.isVisible = true
            binding.webView.isVisible = false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.progress.isVisible = false
            binding.webView.isVisible = true
            onPageLoadSuccess()
        }

        override fun onReceivedError(v: WebView?, r: WebResourceRequest?, e: WebResourceError?) {
            super.onReceivedError(v, r, e)
            onPageLoadError(e?.errorCode)
        }

        override fun onReceivedHttpError(v: WebView?, r: WebResourceRequest?, e: WebResourceResponse?) {
            super.onReceivedHttpError(v, r, e)
            onPageLoadError(e?.statusCode)
        }
    }

    @Parcelize
    data class Input(
        val url: String,
        val successUrlRegex: String? = null,
        val errorUrlRegex: String? = null,
        val extraHeaders: Map<String, String>,
        val javaScriptEnabled: Boolean = false,
        val domStorageEnabled: Boolean = false,
        val removeAllCookies: Boolean = false,
        val acceptCookie: Boolean = true,
        val shouldUseAlternativeUrl: Boolean = true,
        val shouldOpenLinkInBrowser: Boolean = true,
    ) : Parcelable

    @Parcelize
    sealed class Result(
        open val pageLoadErrorCode: Int?
    ) : Parcelable {
        data class Cancel(
            override val pageLoadErrorCode: Int?
        ) : Result(pageLoadErrorCode)

        data class Success(
            val url: String,
            override val pageLoadErrorCode: Int?
        ) : Result(pageLoadErrorCode = null)

        data class Error(
            val url: String,
            override val pageLoadErrorCode: Int?
        ) : Result(pageLoadErrorCode)

    }

    companion object {
        const val ARG_INPUT = "arg.protonWebViewActivityInput"
        const val ARG_RESULT = "arg.protonWebViewActivityResult"

        object ResultContract : ActivityResultContract<Input, Result?>() {

            override fun createIntent(context: Context, input: Input) =
                Intent(context, ProtonWebViewActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(ARG_INPUT, input)
                }

            override fun parseResult(resultCode: Int, intent: Intent?): Result? {
                if (resultCode != Activity.RESULT_OK) return null
                return intent?.getParcelableExtra(ARG_RESULT)
            }
        }
    }
}
