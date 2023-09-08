/*
 * Copyright (c) 2020 Proton Technologies AG
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
package me.proton.core.network.data

import android.os.SystemClock
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal fun OkHttpClient.Builder.initLogging(client: ApiClient): OkHttpClient.Builder {
    // Generate log messages with our custom format.
    addInterceptor { chain ->
        val request = chain.request()
        val auth = request.header("Authorization").formatToken(client)
        CoreLogger.i(
            LogTag.API_REQUEST,
            with(request) { "$method $url (auth $auth)" },
        )

        val startMs = SystemClock.elapsedRealtime()
        val response = chain.proceed(request)
        val durationMs = SystemClock.elapsedRealtime() - startMs

        // Unsuccessful responses are handled by ServerErrorInterceptor and later reported as errors.
        if (response.isSuccessful) {
            CoreLogger.i(
                LogTag.API_RESPONSE,
                with(response) { "$code $message ${request.method} ${request.url} (${durationMs}ms)" }
            )
        }
        response
    }
    if (client.enableDebugLogging) {
        // HttpLoggingInterceptor generate log messages and forward them into provided Logger.
        addInterceptor(
            HttpLoggingInterceptor(
                logger = object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) = CoreLogger.d(LogTag.DEFAULT, message)
                }
            ).apply { level = HttpLoggingInterceptor.Level.BODY }
        )
    }
    return this
}

internal fun String?.formatToken(client: ApiClient) = when {
    this == null -> "[none]"
    client.enableDebugLogging -> this
    else -> "${removePrefix("Bearer ").take(TOKEN_PREFIX_LENGTH)}..."
}

internal const val TOKEN_PREFIX_LENGTH = 5
