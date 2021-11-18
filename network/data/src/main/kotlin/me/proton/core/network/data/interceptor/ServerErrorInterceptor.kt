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

import me.proton.core.network.data.ProtonErrorException
import me.proton.core.network.data.protonApi.ProtonErrorData
import me.proton.core.network.domain.exception.NetworkException
import me.proton.core.util.kotlin.deserializeOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ServerErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            // every IO exception is possible potential blocking of the API
            with(request.url) {
                throw NetworkException(encodedPath, query, e)
            }
        }
        if (!response.isSuccessful) {
            val errorBody = response.peekBody(MAX_ERROR_BYTES).string()
            val protonError = errorBody.deserializeOrNull(ProtonErrorData.serializer())?.apiResultData
            if (protonError != null) {
                throw ProtonErrorException(response, protonError)
            }
        }
        return response
    }

    companion object {
        private const val MAX_ERROR_BYTES = 1_000_000L
    }
}
