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

package me.proton.core.humanverification.presentation.ui.common

import android.net.Uri
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.VisibleForTesting
import androidx.core.net.toUri
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.humanverification.presentation.HumanVerificationApiHost
import me.proton.core.humanverification.presentation.LogTag
import me.proton.core.humanverification.presentation.LogTag.HV_REQUEST_ERROR
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.presentation.ui.webview.ProtonWebViewClient
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import me.proton.core.util.kotlin.takeIfNotBlank

/** Used to override HTTP headers to access captcha iframe on debug from outside the VPN */
class HumanVerificationWebViewClient(
    private val apiHost: String,
    private val extraHeaderProvider: ExtraHeaderProvider,
    private val networkPrefs: NetworkPrefs,
    private val networkRequestOverrider: NetworkRequestOverrider,
    private val onResourceLoadingError: (request: WebResourceRequest?, response: WebResponseError?) -> Unit,
    @HumanVerificationApiHost private val verifyAppUrl: String
) : ProtonWebViewClient(networkPrefs, extraHeaderProvider) {

    private val extraHeaders = extraHeaderProvider.headers

    private val rootDomain: String = when {
        apiHost.isIpAddress() -> apiHost
        else -> apiHost.split(".").takeLast(2).joinToString(".")
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val needsExtraHeaderForAPI = extraHeaders.isNotEmpty() && request.url.matchesRootDomain()
        return when {
            request.method != "GET" -> {
                // It's not possible to override a POST request, because
                // WebResourceRequest doesn't provide access to POST body.
                null
            }

            request.url.isAlternativeHost() -> overrideForDoH(request, extraHeaders)
            needsExtraHeaderForAPI -> overrideWithExtraHeaders(request, extraHeaders)
            else -> null
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        val logMessage = "Request failed: ${request?.method} ${request?.url} with " +
                "status ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase}"
        CoreLogger.i(HV_REQUEST_ERROR, logMessage)
        onResourceLoadingError(request, errorResponse?.let { WebResponseError.Http(it) })
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        val logMessage = "Request failed: ${request?.method} ${request?.url} with " +
                "code ${error?.errorCode} ${error?.description}"
        CoreLogger.i(HV_REQUEST_ERROR, logMessage)
        onResourceLoadingError(request, error?.let { WebResponseError.Resource(it) })
    }

    private fun Uri.matchesRootDomain(): Boolean {
        if (host == apiHost) return true
        return host?.endsWith(rootDomain) == true
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
        // This allows a custom redirection to HumanVerificationApiHost Url from the DoH one.
        // Must be skipped for the internal captcha request and any Proton API requests.
        val dohHeader = if (request.url.isLoadCaptchaUrl() || request.url.isProtonApiUrl()) {
            null
        } else {
            "X-PM-DoH-Host" to verifyAppUrl.toUri().host
        }
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
        val response = networkRequestOverrider.overrideRequest(
            url,
            method,
            headers,
            acceptSelfSignedCertificates
        )

        if (response.httpStatusCode !in 200 until 400) {
            val logMessage = "Request with override failed: $method $url with " +
                    "code ${response.httpStatusCode} ${response.reasonPhrase}"
            CoreLogger.i(HV_REQUEST_ERROR, logMessage)
        }

        // We need to remove the CSP header for DoH to work
        val isAlternativeUrl = Uri.parse(url).isAlternativeHost()
        val needsCspRemoval = isAlternativeUrl && response.responseHeaders.containsKey(CSP_HEADER)
        val filteredHeaders = if (needsCspRemoval) {
            response.responseHeaders.toMutableMap().also { it.remove(CSP_HEADER) }
        } else {
            response.responseHeaders
        }

        // Copy the set-cookie headers from the overridden request into the default cookie manager
        // to ensure they are sent on requests the web app makes
        val cookieHeaders =
            response.responseHeaders.filter { (key) -> key.lowercase() == "set-cookie" }
        val cookieManager = CookieManager.getInstance();
        cookieHeaders.entries.forEach { entry -> cookieManager.setCookie(url, entry.value) }

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
        CoreLogger.e(LogTag.DEFAULT, it)
    }.getOrNull()

    private fun Uri.isLoadCaptchaUrl() = path?.endsWith("/core/v4/captcha") == true
    private fun Uri.isProtonApiUrl() = path?.startsWith("/api/") == true

    companion object {
        private const val CSP_HEADER = "content-security-policy"

        private val ipv4Regex = Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")
        private val hexRegex = Regex("[0-9a-fA-F]+")

        @VisibleForTesting
        internal fun String.isIpAddress(): Boolean = ipv4Regex.matches(this) || isIpV6Address()

        private fun String.isIpV6Address(): Boolean {
            val atLeastTwoColons = count { it == ':' } >= 2
            val filtered = replace(":", "").replace(".", "")
            return atLeastTwoColons && hexRegex.matches(filtered)
        }
    }
}

@ExcludeFromCoverage
sealed class WebResponseError {
    data class Http(val response: WebResourceResponse) : WebResponseError()
    data class Ssl(val error: SslError) : WebResponseError()
    data class Resource(val error: WebResourceError) : WebResponseError()
}
