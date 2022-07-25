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

package me.proton.core.humanverification.data.utils

import androidx.annotation.VisibleForTesting
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.network.data.LogTag
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.SharedOkHttpClient
import me.proton.core.network.data.initSPKIleafPinning
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import javax.inject.Inject

class NetworkRequestOverriderImpl @Inject constructor(
    @SharedOkHttpClient private val okHttpClient: OkHttpClient,
) : NetworkRequestOverrider {

    private val insecureClient: OkHttpClient by lazy {
        with(okHttpClient.newBuilder()) {
            initSPKIleafPinning(this, Constants.ALTERNATIVE_API_SPKI_PINS)
            addInterceptor(
                HttpLoggingInterceptor { message ->
                    CoreLogger.d(LogTag.DEFAULT, message)
                }.apply { level = HttpLoggingInterceptor.Level.BODY }
            )
        }.build()
    }

    override fun overrideRequest(
        url: String,
        method: String,
        headers: List<Pair<String, String>>,
        acceptSelfSignedCertificates: Boolean,
        body: InputStream?,
        bodyType: String?,
    ): NetworkRequestOverrider.Result {
        val request = createRequest(url, method, headers, body, bodyType)
        val client = if (acceptSelfSignedCertificates) insecureClient else okHttpClient
        val response = client.newCall(request).execute()
        val responseBody = response.body
        val mimeType = response.header("content-type", responseBody?.contentType()?.type)
            ?.substringBefore(";")
        val encoding = response.header("content-encoding", "utf-8")
        return NetworkRequestOverrider.Result(
            mimeType = mimeType,
            encoding = encoding,
            contents = responseBody?.byteStream(),
            httpStatusCode = response.code,
            reasonPhrase = response.message,
            responseHeaders = response.headers.toMap(),
        )
    }

    @VisibleForTesting
    internal fun createRequest(
        url: String,
        method: String,
        headers: List<Pair<String, String>>,
        body: InputStream? = null,
        bodyType: String? = null,
    ): Request {
        val preparedBody = body?.readBytes()?.toRequestBody(bodyType?.toMediaTypeOrNull())
        return Request.Builder()
            .url(url)
            .method(method, preparedBody)
            .apply { headers.forEach { addHeader(it.first, it.second) } }
            .build()
    }

    companion object {
        const val TAG = "NetworkRequestOverriderImpl"
    }
}
