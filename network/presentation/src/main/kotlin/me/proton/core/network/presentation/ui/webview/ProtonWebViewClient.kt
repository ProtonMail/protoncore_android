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

package me.proton.core.network.presentation.ui.webview

import android.annotation.SuppressLint
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.presentation.utils.openBrowserLink

/**
 * Proton WebViewClient supporting:
 * - Proton alternative Ssl Cert pinning.
 * - Proton Extra Headers.
 */
// See: https://commonsware.com/blog/2015/06/11/psa-webview-regression.html.
public open class ProtonWebViewClient(
    private val networkPrefs: NetworkPrefs,
    private val extraHeaderProvider: ExtraHeaderProvider,
) : WebViewClient() {

    private val alternativeUrl: Uri?
        get() = networkPrefs.activeAltBaseUrl?.toUri()

    private var isFinished = false

    public var shouldOpenLinkInBrowser: Boolean = true

    public fun Uri.isAlternativeHost(): Boolean = alternativeUrl?.let { host == it.host } ?: false

    override fun onPageFinished(view: WebView?, url: String?) {
        isFinished = true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (shouldOpenLinkInBrowser) {
            val url = request.url.toString()
            return if (shouldKeepInWebView(url)) {
                view.loadUrl(url, extraHeaderProvider.headers.toMap())
                true
            } else {
                view.context.openBrowserLink(url)
                true
            }
        }
        return false
    }

    public open fun shouldKeepInWebView(url: String): Boolean {
        if (!isFinished) return true
        return false
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        if (error.isAlternativeSelfSignedCert()) {
            handler.proceed()
        } else {
            handler.cancel()
        }
    }

    private fun SslError.isAlternativeSelfSignedCert(): Boolean = when {
        !url.toUri().isAlternativeHost() -> false
        primaryError == SslError.SSL_UNTRUSTED -> certificate.isTrustedByLeafSPKIPinning()
        else -> false
    }
}
