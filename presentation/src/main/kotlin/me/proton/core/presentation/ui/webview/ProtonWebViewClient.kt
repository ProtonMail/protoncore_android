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

package me.proton.core.presentation.ui.webview

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import me.proton.core.presentation.utils.openBrowserLink

// See: https://commonsware.com/blog/2015/06/11/psa-webview-regression.html.
open class ProtonWebViewClient : WebViewClient() {

    var isFinished = false

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        isFinished = true
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url.toString()
        if (shouldKeepInWebView(url)) {
            view.loadUrl(url)
        } else {
            view.context.openBrowserLink(url)
        }
        return true
    }

    open fun shouldKeepInWebView(url: String): Boolean {
        if (!isFinished) return true
        return false
    }
}
