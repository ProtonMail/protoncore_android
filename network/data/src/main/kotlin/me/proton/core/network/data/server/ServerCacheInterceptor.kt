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

package me.proton.core.network.data.server

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Force Local Cache by rewriting Server Cache-Control header (for debug purpose).
 */
class ServerCacheInterceptor(private val maxAgeSeconds: Int) : Interceptor {

    private fun Response.Builder.maxAge(seconds: Int): Response.Builder =
        header("Cache-Control", "max-age=$seconds, stale-if-error=$seconds")

    private fun Response.Builder.removePragma(): Response.Builder =
        removeHeader("Pragma")

    private fun Response.cache(maxAgeSeconds: Int = 0): Response = newBuilder()
        .maxAge(maxAgeSeconds)
        .removePragma()
        .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        return response.cache(maxAgeSeconds)
    }
}
