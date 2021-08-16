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

import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.session.SessionId
import okhttp3.Interceptor
import okhttp3.Response
import org.jetbrains.annotations.TestOnly

class TooManyRequestInterceptor(
    private val sessionId: SessionId?,
    private val wallClockMs: () -> Long
) : Interceptor {

    private fun nowSeconds() = wallClockMs().div(1000).toInt()

    data class BlockedRequest(
        val response: Response,
        val timestampSeconds: Int,
        val retryAfterSeconds: Int
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Check if this route for this session is blocked.
        val key = "${sessionId?.id}:${request.url.encodedPath}"
        blockedRequestMap[key]?.let { banned ->
            val elapsedDeltaSeconds = nowSeconds() - banned.timestampSeconds
            if (elapsedDeltaSeconds < banned.retryAfterSeconds) {
                return banned.response.newBuilder()
                    .header("Retry-After", "${banned.retryAfterSeconds - elapsedDeltaSeconds}")
                    .build()
            } else {
                blockedRequestMap.remove(key)
            }
        }

        val response = chain.proceed(request)
        if (response.code == ApiResult.HTTP_TOO_MANY_REQUESTS) {
            val retryAfter = response.headers["Retry-After"]?.toIntOrNull() ?: 0
            blockedRequestMap[key] = BlockedRequest(response, nowSeconds(), retryAfter)
        }
        return response
    }

    companion object {
        private val blockedRequestMap = mutableMapOf<String, BlockedRequest>()

        @TestOnly
        fun reset() = blockedRequestMap.clear()
    }
}
