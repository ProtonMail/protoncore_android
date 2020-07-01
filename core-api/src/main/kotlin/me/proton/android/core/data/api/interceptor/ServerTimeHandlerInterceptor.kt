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

import me.proton.android.core.data.api.entity.ServerTime
import me.proton.android.core.domain.crypto.IOpenPGP
import me.proton.android.core.domain.network.ServerTimeHandler
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.lang.Exception
import java.text.ParseException

/**
 * Created by dinokadrikj on 4/6/20.
 *
 * Implementation of the [ServerTimeHandler] abstract class.
 */
class ServerTimeHandlerInterceptor(openPgp: IOpenPGP) : ServerTimeHandler(openPgp), Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        try {
            response = chain.proceed(request)
            handleResponse(response)
        } catch (exception: IOException) {
            // TODO: check later if we are going to support live network status monitor and tracking.
            //  We will need to update the UI and probably hold this information here so that we do
            //  not ddos our API. This should be postponed later when DoH implementation is moved in
            //  this SDK. We might throw an exception so that the upper layers know better.
        } catch (exception: Exception) {
            // noop
        }

        if (response == null) {
            return chain.proceed(request)
        }
        return response
    }

    private fun handleResponse(response: Response) {
        val dateString = response.header("date", null) ?: return
        try {
            val date = RFC_1123_FORMAT.parse(dateString)
            openPgp.updateTime(date.time / 1000)
            ServerTime.updateServerTime(date.time)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}