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

package me.proton.core.humanverification.presentation.ui.common

import android.annotation.SuppressLint
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.humanverification.presentation.HumanVerificationApiHost
import me.proton.core.humanverification.presentation.LogTag.HV_REQUEST_ERROR
import me.proton.core.humanverification.presentation.utils.getCompatX509Cert
import me.proton.core.network.data.LeafSPKIPinningTrustManager
import me.proton.core.network.data.di.Constants
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.takeIfNotBlank

/** Used to override HTTP headers to access captcha iframe on debug from outside the VPN */
class HumanVerificationWebViewClient(
    private val apiHost: String,
    private val extraHeaders: List<Pair<String, String>>,
    private val alternativeUrl: String?,
    private val networkRequestOverrider: NetworkRequestOverrider,
    private val onResourceLoadingError: (request: WebResourceRequest?, response: WebResponseError?) -> Unit,
    private val onWebLocationChanged: (String) -> Unit,
    @HumanVerificationApiHost private val verifyAppUrl: String
) : WebViewClient() {
    private val rootDomain: String = when {
        apiHost.isIpAddress() -> apiHost
        else -> apiHost.split(".").takeLast(2).joinToString(".")
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val needsExtraHeaderForAPI = extraHeaders.isNotEmpty() && request.url.matchesRootDomain()
        return when {
            request.method != "GET" -> null
            request.url.isAlternativeUrl() -> overrideForDoH(request, extraHeaders)
            needsExtraHeaderForAPI -> overrideWithExtraHeaders(request, extraHeaders)
            else -> null
        }
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        onWebLocationChanged(url)
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        val logMessage = "Request failed: ${request?.method} ${request?.url} with " +
            "status ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase}"
        CoreLogger.log(HV_REQUEST_ERROR, logMessage)
        onResourceLoadingError(request, errorResponse?.let { WebResponseError.Http(it) })
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        CoreLogger.log(HV_REQUEST_ERROR, "SSL error: ${error.url} ${error.primaryError}")
        if (tryAllowingSelfSignedDoHCert(error)) {
            handler.proceed()
        } else {
            handler.cancel()
            onResourceLoadingError(null, WebResponseError.Ssl(error))
        }
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        val logMessage = "Request failed: ${request?.method} ${request?.url} with " +
            "code ${error?.errorCode} ${error?.description}"
        CoreLogger.log(HV_REQUEST_ERROR, logMessage)
        onResourceLoadingError(request, error?.let { WebResponseError.Resource(it) })
    }

    private fun Uri.matchesRootDomain(): Boolean {
        if (host == apiHost) return true
        return host?.endsWith(rootDomain) == true
    }

    private fun tryAllowingSelfSignedDoHCert(error: SslError): Boolean {
        return if (error.primaryError == SslError.SSL_UNTRUSTED && error.url.toUri().isAlternativeUrl()) {
            val x509Certificate = error.certificate.getCompatX509Cert()
            if (x509Certificate == null) {
                false
            } else {
                val trustManager = LeafSPKIPinningTrustManager(Constants.ALTERNATIVE_API_SPKI_PINS)
                runCatching { trustManager.checkServerTrusted(arrayOf(x509Certificate), "generic") }.isSuccess
            }
        } else false
    }

    private fun overrideWithExtraHeaders(
        request: WebResourceRequest,
        extraHeaders: List<Pair<String, String>>,
    ): WebResourceResponse? =
        overrideRequest(
            request.url.toString(),
            request.method,
            headers = request.requestHeaders.toList() + extraHeaders,
            acceptSelfSignedCertificates = false,
        )

    private fun overrideForDoH(
        request: WebResourceRequest,
        extraHeaders: List<Pair<String, String>>,
    ): WebResourceResponse? {
        // This allows a custom redirection to HumanVerificationApiHost Url from the DoH one
        // Must be skipped for the internal captcha request
        val dohHeader = if (!request.url.isLoadCaptchaUrl()) {
            "X-PM-DoH-Host" to verifyAppUrl.toUri().host
        } else null
        return overrideRequest(
            request.url.toString(),
            request.method,
            headers = request.requestHeaders.toList() + extraHeaders + listOfNotNull(dohHeader),
            acceptSelfSignedCertificates = true,
        ).also {
            if (it?.statusCode !in 200 until 400) {
                onResourceLoadingError(request, it?.let { WebResponseError.Http(it) })
            }
        }
    }

    private fun overrideRequest(
        url: String,
        method: String,
        headers: List<Pair<String, String>>,
        acceptSelfSignedCertificates: Boolean
    ): WebResourceResponse? = runCatching {
        val response = networkRequestOverrider.overrideRequest(url, method, headers, acceptSelfSignedCertificates)

        if (response.httpStatusCode !in 200 until 400) {
            val logMessage = "Request with override failed: $method $url with " +
                "code ${response.httpStatusCode} ${response.reasonPhrase}"
            CoreLogger.log(HV_REQUEST_ERROR, logMessage)
        }

        // We need to remove the CSP header for DoH to work
        val needsCspRemoval = Uri.parse(url).isAlternativeUrl() && response.responseHeaders.containsKey(CSP_HEADER)
        val filteredHeaders = if (needsCspRemoval) {
            response.responseHeaders.toMutableMap().also { it.remove(CSP_HEADER) }
        } else {
            response.responseHeaders
        }

        // HTTP/2 removed Reason-Phrase from the spec, but the constructor
        // for WebResourceResponse would throw if it received a blank string.
        val reasonPhrase = response.reasonPhrase.takeIfNotBlank() ?: "UNKNOWN"

        WebResourceResponse(
            response.mimeType,
            response.encoding,
            response.httpStatusCode,
            reasonPhrase,
            filteredHeaders,
            response.contents
        )
    }.onFailure {
        CoreLogger.e(TAG, it)
    }.getOrNull()

    private fun Uri.isLoadCaptchaUrl() = path?.endsWith("/core/v4/captcha") == true
    private fun Uri.isAlternativeUrl() = alternativeUrl?.toUri()?.let { alternativeUri ->
        host == alternativeUri.host
    } ?: false

    companion object {
        const val TAG = "HumanVerificationWebViewClient"

        private const val CSP_HEADER = "content-security-policy"

        private val ipv4Regex = Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")
        private val ipv6Regex = Regex("[0-9a-fA-F:/]+")

        private fun String.isIpAddress(): Boolean = ipv4Regex.matches(this) || ipv6Regex.matches(this)
    }
}

sealed class WebResponseError {
    data class Http(val response: WebResourceResponse) : WebResponseError()
    data class Ssl(val error: SslError) : WebResponseError()
    data class Resource(val error: WebResourceError) : WebResponseError()
}
