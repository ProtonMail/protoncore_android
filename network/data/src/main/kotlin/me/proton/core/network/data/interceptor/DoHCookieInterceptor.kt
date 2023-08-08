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

package me.proton.core.network.data.interceptor

import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.domain.LogTag
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class DoHCookieInterceptor(
    private val networkPrefs: NetworkPrefs,
    private val cookieStore: ProtonCookieStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Won't be null if DoH is working
        val baseHttpUrl = networkPrefs.activeAltBaseUrl?.let { url ->
            runCatching { url.toHttpUrl() }
                .onFailure { CoreLogger.e(LogTag.INTERCEPTOR, it) }
                .getOrNull()
        }
        val url = chain.request().url

        if (baseHttpUrl == null || url.host != baseHttpUrl.host)
            return chain.proceed(chain.request())

        val cookies = cookieStore.loadForRequest(baseHttpUrl)
        // Add all cookies from the default baseUrl to request that go to the alternative base url
        val response = chain.proceed(
            chain.request().newBuilder()
                .apply { cookies.forEach { addHeader("Cookie", it.toString()) } }
                .build()
        )

        // Save any cookies received from the alternative base url
        val dohDomain = baseHttpUrl.host
        // With DoH, the Domain value in the Set-Cookie headers won't match the url of the requests
        // and the cookies with this value will be discarded, so we're manually replacing it.
        val setCookieHeaders = response.headers.filter { it.first.lowercase() == "set-cookie" }
            .map { it.second }
            .map { header -> header.replace(Regex("[dD]omain=(.+?);"), "Domain=${dohDomain};") }
        val responseCookies = setCookieHeaders.mapNotNull { Cookie.parse(baseHttpUrl, it) }
        cookieStore.saveFromResponse(baseHttpUrl, responseCookies)

        return response
    }
}
