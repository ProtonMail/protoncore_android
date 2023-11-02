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

import me.proton.core.network.domain.LogTag
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.util.kotlin.CoreLogger
import okhttp3.Interceptor
import okhttp3.Response

class ServerTimeInterceptor(
    private val serverTimeListener: ServerTimeListener
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.isSuccessful) {
            val serverUtc = response.headers.getDate("date")
            if (serverUtc != null) {
                serverTimeListener.onServerTimeMillisUpdated(serverUtc.time)
            } else {
                CoreLogger.e(LogTag.SERVER_TIME_PARSE_ERROR, "Could not parse 'date' from response headers")
            }
        }
        return response
    }
}
