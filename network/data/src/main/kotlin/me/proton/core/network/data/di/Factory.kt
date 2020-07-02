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
package me.proton.core.network.data.di

import android.content.Context
import android.net.Uri
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import me.proton.core.network.data.NetworkManagerImpl
import me.proton.core.network.data.ProtonApiBackend
import me.proton.core.network.data.initPinning
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiManagerImpl
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.ProtonForceUpdateHandler
import me.proton.core.network.domain.RefreshTokenHandler
import me.proton.core.network.domain.UserData
import me.proton.core.util.kotlin.ProtonCoreConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Factory for creating [ApiManager] instances. There should be a single instance per [baseUrl].
 *
 * @param baseUrl Base url for the api e.g. "https://api.protonvpn.ch/"
 */
class ApiFactory(
    private val baseUrl: String,
    private val apiClient: ApiClient,
    private val networkManager: NetworkManager,
    scope: CoroutineScope
) {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainScope = scope + newSingleThreadContext("core.network.main")

    /**
     * Instantiates ApiManager for given [Api] interface and user.
     *
     * @param Api Retrofit interface defined by the client, must inherit from [BaseRetrofitApi].
     * @param userData [UserData] to be used in the [ApiManager].
     * @param interfaceClass Kotlin class for [Api] interface.
     * @param clientErrorHandlers Extra error handlers provided by the client.
     * @param certificatePins Overrides [Constants.DEFAULT_PINS]
     * @return [ApiManager] instance.
     */
    fun <Api : BaseRetrofitApi> ApiManager(
        userData: UserData,
        interfaceClass: KClass<Api>,
        clientErrorHandlers: List<ApiErrorHandler<Api>> = emptyList(),
        certificatePins: Array<String> = Constants.DEFAULT_PINS
    ): ApiManager<Api> {
        val pinningStrategy = { builder: OkHttpClient.Builder ->
            initPinning(builder, Uri.parse(baseUrl).host!!, certificatePins)
        }
        val primaryBackend = ProtonApiBackend(
            baseUrl.toString(),
            apiClient,
            userData,
            baseOkHttpClient,
            listOf(jsonConverter),
            interfaceClass,
            networkManager,
            pinningStrategy
        )
        val dohProvider = DohProvider()
        val errorHandlers =
            createBaseErrorHandlers<Api>(userData, ::javaMonoClockMs, mainScope) + clientErrorHandlers
        return ApiManagerImpl(apiClient, primaryBackend, dohProvider, networkManager, errorHandlers, ::javaMonoClockMs)
    }

    internal val jsonConverter =
        ProtonCoreConfig.defaultJsonStringFormat.asConverterFactory("application/json".toMediaType())

    internal val baseOkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
        if (apiClient.enableDebugLogging) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        builder.build()
    }

    internal fun <Api> createBaseErrorHandlers(
        userData: UserData,
        monoClockMs: () -> Long,
        networkMainScope: CoroutineScope
    ) = listOf(
        RefreshTokenHandler<Api>(userData, monoClockMs, networkMainScope),
        ProtonForceUpdateHandler(apiClient)
    )

    private fun javaMonoClockMs(): Long =
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
}

/**
 * Factory method for [NetworkManager] allowing tracking of connectivity changes.
 */
fun NetworkManager(context: Context): NetworkManager =
    NetworkManagerImpl(context.applicationContext)
