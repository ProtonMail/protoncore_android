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
import me.proton.core.network.data.di.Constants
import me.proton.core.network.domain.ApiClient
import me.proton.core.util.kotlin.Logger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal fun OkHttpClient.Builder.initLogging(client: ApiClient, logger: Logger): OkHttpClient.Builder {
    if (client.enableDebugLogging) {
        addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
    }
    addInterceptor { chain ->
        val request = chain.request()
        val auth = request.header("Authorization").formatToken(client)
        logger.i(Constants.LOG_TAG, with(request) {
            "--> $method $url (auth $auth)"
        })

        val startMs = SystemClock.elapsedRealtime()
        val response = chain.proceed(request)
        val durationMs = SystemClock.elapsedRealtime() - startMs
        logger.i(Constants.LOG_TAG, with(response) {
            "<-- $code $message ${request.method} ${request.url} (${durationMs}ms)"
        })

        response
    }
    return this
}

internal fun String?.formatToken(client: ApiClient) = when {
    this == null -> "[none]"
    client.enableDebugLogging -> this
    else -> "${take(TOKEN_PREFIX_LENGTH)}..."
}

const val TOKEN_PREFIX_LENGTH = 5
