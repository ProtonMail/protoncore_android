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

package me.proton.android.core.data.api.service

import me.proton.android.core.data.api.interceptor.ProtonInterceptor
import me.proton.android.core.data.api.interceptor.ServerTimeHandlerInterceptor
import ch.protonmail.libs.core.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import okhttp3.logging.HttpLoggingInterceptor.Level

/**
 * Created by dinokadrikj on 4/6/20.
 *
 * Base networking class. the base Url is provided, because it is left on the client products to
 * decide on different
 *
 * @param baseUrl every client should provide their API base url.
 * @param protonApi the product specific retrofit service interface for their api.
 */

const val DEFAULT_TIMEOUT = 10000L // long in ms

class ProtonApi(
    private val builder: OkHttpClient.Builder,
    private val baseUrl: String,
    private val protonApi: ProtonPublicService,
    loggingLevel: Level = Level.NONE,
    serverTimeInterceptor: ServerTimeHandlerInterceptor?,
    defaultInterceptor: ProtonInterceptor
) {

    init {
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
        builder.readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
        builder.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)

        // the default interceptor is a must
        builder.addInterceptor(defaultInterceptor)
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = loggingLevel
            builder.addInterceptor(httpLoggingInterceptor)
        }
        if (serverTimeInterceptor != null) {
            builder.addInterceptor(serverTimeInterceptor)
        }
//        builder.connectionSpecs(connectionSpecs)
    }
}