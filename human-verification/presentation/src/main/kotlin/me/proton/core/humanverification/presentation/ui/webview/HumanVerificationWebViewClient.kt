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

package me.proton.core.humanverification.presentation.ui.webview

import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.util.kotlin.CoreLogger

/** Used to override HTTP headers to access captcha iframe on debug from outside the VPN */
class HumanVerificationWebViewClient(
    private val apiHost: String,
    private val extraHeaders: List<Pair<String, String>>,
    private val alternativeUrl: String?,
    private val networkRequestOverrider: NetworkRequestOverrider,
    private val onResourceLoadingError: () -> Unit,
    private val onWebLocationChanged: (String) -> Unit,
) : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val needsExtraHeaderForAPI = extraHeaders.isNotEmpty() && request.url.host == apiHost
        val usesDoH = alternativeUrl != null && request.url.isAlternativeUrl()
        return when {
            usesDoH -> overrideForDoH(request, extraHeaders)
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
        // favicon is failing to load in HV2 and triggers the error callback
        if (request?.url?.lastPathSegment == "favicon.ico") return
        onResourceLoadingError()
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        super.onReceivedSslError(view, handler, error)
        onResourceLoadingError()
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        onResourceLoadingError()
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
        // TODO: remove when it's fixed in web frontend, needed to load captcha with DoH
        val url = with(request.url) {
            if (isLoadCaptchaUrl() && host?.contains("-api") == true) {
                toString().replace("-api", "")
            } else toString()
        }
        // This allows a custom redirection to HumanVerificationApiHost Url from the DoH one
        // Must be skipped for the internal captcha request
        val dohHeader = if (!request.url.isLoadCaptchaUrl()) {
            "X-PM-DoH-Host" to "verify.protonmail.com"
        } else null
        return overrideRequest(
            url,
            request.method,
            headers = request.requestHeaders.toList() + extraHeaders + listOfNotNull(dohHeader),
            acceptSelfSignedCertificates = true,
        ).also {
            if (it?.statusCode !in 200 until 400) {
                onResourceLoadingError()
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
        WebResourceResponse(response.mimeType, response.encoding, response.contents)
    }.onFailure {
        CoreLogger.e(TAG, it)
    }.getOrNull()

    private fun Uri.isLoadCaptchaUrl() = path == "/core/v4/captcha"
    private fun Uri.isAlternativeUrl() = host?.endsWith("compute.amazonaws.com") == true

    companion object {
        const val TAG = "HumanVerificationWebViewClient"
    }
}
