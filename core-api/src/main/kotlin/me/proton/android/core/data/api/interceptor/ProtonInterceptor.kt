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

package me.proton.android.core.data.api.interceptor

import me.proton.android.core.data.api.ResponseCodes
import me.proton.android.core.data.api.service.ProtonPublicService
import me.proton.android.core.data.api.manager.TokenManager
import me.proton.android.core.data.api.manager.UserManager
import me.proton.android.core.data.api.entity.response.ApiErrorResponseBody
import ch.protonmail.libs.core.utils.deserialize
import ch.protonmail.libs.core.utils.safe
import me.proton.android.core.data.api.entity.response.ApiError
import okhttp3.Interceptor
import okhttp3.Response

/**
 * A custom [Interceptor] to handle custom call failures
 *
 * Map [Response] to contain the failure description if [Response.body] is [ApiError] with an error
 * code
 *
 * Does nothing if:
 * * [Response] is not successful
 * * [Response.body] is not applicable to [ApiError]
 * * [ApiError.code] is success code ( this probably will never happen, since that means we have
 *   an error in the [Response.body] even with a success code )
 *
 *
 * @author Davide Farella
 */
class ProtonInterceptor(
    userManager: UserManager,
    tokenManager: TokenManager,
    appVersion: String,
    userAgent: String,
    locale: String,
    api: ProtonPublicService
) : BaseProtonApiInterceptor(appVersion, userAgent, locale, userManager, tokenManager, api) {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Ignore failed requests
        if (!response.isSuccessful) {
            return response
        }

        // Try to intercept API error
        // FIXME: this might not be right, because for every successful api call it will go
        //  inside catch block and we know try/catch is expensive
        val apiError = safe {
            val bodyString = response.body()!!.string()
            bodyString.deserialize(ApiError.serializer()).takeIf { it.code != ResponseCodes.OK }
        }

        // Edit response to failure if an ApiError is found
        return if (apiError != null) response.newBuilder()
            .code(apiError.code)
            .message(apiError.error)
            .body(
                ApiErrorResponseBody(
                    apiError.details
                )
            )
            .build()

        // else return the original response
        else response
    }
}
