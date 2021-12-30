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

package me.proton.core.network.data.interceptor

import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpCookie
import java.net.URI

class DoHCookieInterceptor(
    private val networkPrefs: NetworkPrefs,
    private val cookieStore: ProtonCookieStore,
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Won't be null if DoH is working
        val baseURI = networkPrefs.activeAltBaseUrl?.let {
            runCatching { URI.create(networkPrefs.activeAltBaseUrl) }
                .onFailure { CoreLogger.e(TAG, it) }
                .getOrNull()
        }
        val url = chain.request().url

        if (baseURI == null || url.host != baseURI.host)
            return chain.proceed(chain.request())

        val cookies = cookieStore.get(baseURI)
        // Add all cookies from the default baseUrl to request that go to the alternative base url
        val response = chain.proceed(
            chain.request().newBuilder()
                .apply { cookies.forEach { addHeader("Cookie", it.toString()) } }
                .build()
        )

        // Save any cookies received from the alternative base url
        response.headers("Set-Cookie")
            .mapNotNull { runCatching { HttpCookie.parse(it) }.getOrNull() }
            .flatten()
            .forEach { cookie -> cookieStore.add(baseURI, cookie) }

        return response
    }

    companion object {
        const val TAG = "DoHCookieInterceptor"
    }
}
