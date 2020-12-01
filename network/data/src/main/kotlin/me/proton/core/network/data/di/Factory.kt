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
import androidx.annotation.VisibleForTesting
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import me.proton.core.network.data.NetworkManagerImpl
import me.proton.core.network.data.NetworkPrefsImpl
import me.proton.core.network.data.ProtonApiBackend
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.data.doh.DnsOverHttpsProviderRFC8484
import me.proton.core.network.data.initPinning
import me.proton.core.network.data.initSPKIleafPinning
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiManagerImpl
import me.proton.core.network.domain.DohApiHandler
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.handlers.HumanVerificationHandler
import me.proton.core.network.domain.handlers.ProtonForceUpdateHandler
import me.proton.core.network.domain.handlers.RefreshTokenHandler
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.Logger
import me.proton.core.util.kotlin.ProtonCoreConfig
import okhttp3.JavaNetCookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URI
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
    private val logger: Logger,
    private val networkManager: NetworkManager,
    private val prefs: NetworkPrefs,
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener,
    private val cookieStore: ProtonCookieStore,
    scope: CoroutineScope
) {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainScope = scope + newSingleThreadContext("core.network.main")

    init {
        requireNotNull(URI(baseUrl).host)
    }

    /**
     * Instantiates ApiManager for given [Api] interface and user.
     *
     * @param Api Retrofit interface defined by the client, must inherit from [BaseRetrofitApi].
     * @param sessionId [SessionId] to be used in the [create].
     * @param interfaceClass Kotlin class for [Api] interface.
     * @param clientErrorHandlers Extra error handlers provided by the client.
     * @param certificatePins Overrides [Constants.DEFAULT_SPKI_PINS]
     * @param alternativeApiPins Overrides [Constants.ALTERNATIVE_API_SPKI_PINS]
     * @return Created instance.
     */
    fun <Api : BaseRetrofitApi> create(
        sessionId: SessionId? = null,
        interfaceClass: KClass<Api>,
        clientErrorHandlers: List<ApiErrorHandler<Api>> = emptyList(),
        certificatePins: Array<String> = Constants.DEFAULT_SPKI_PINS,
        alternativeApiPins: List<String> = Constants.ALTERNATIVE_API_SPKI_PINS
    ): ApiManager<Api> {
        val pinningStrategy = { builder: OkHttpClient.Builder ->
            initPinning(builder, URI(baseUrl).host, certificatePins)
        }
        val primaryBackend = ProtonApiBackend(
            baseUrl,
            apiClient,
            logger,
            sessionId,
            sessionProvider,
            baseOkHttpClient,
            listOf(jsonConverter),
            interfaceClass,
            networkManager,
            pinningStrategy
        )

        val errorHandlers =
            createBaseErrorHandlers<Api>(sessionId, ::javaMonoClockMs, mainScope) + clientErrorHandlers

        val alternativePinningStrategy = { builder: OkHttpClient.Builder ->
            initSPKIleafPinning(builder, alternativeApiPins)
        }
        val dohApiHandler = DohApiHandler(
            apiClient,
            primaryBackend,
            dohProvider,
            prefs,
            ::javaWallClockMs,
            ::javaMonoClockMs
        ) { baseUrl ->
            ProtonApiBackend(
                baseUrl,
                apiClient,
                logger,
                sessionId,
                sessionProvider,
                baseOkHttpClient,
                listOf(jsonConverter),
                interfaceClass,
                networkManager,
                alternativePinningStrategy
            )
        }

        return ApiManagerImpl(
            apiClient, primaryBackend, dohApiHandler, networkManager, errorHandlers, ::javaMonoClockMs
        )
    }

    internal val jsonConverter =
        ProtonCoreConfig.defaultJsonStringFormat.asConverterFactory("application/json".toMediaType())

    @VisibleForTesting
    val baseOkHttpClient by lazy {
        val cookieManager = CookieManager(
            cookieStore,
            CookiePolicy.ACCEPT_ALL
        )
        CookieManager.setDefault(cookieManager)
        val builder = OkHttpClient.Builder()
            .cookieJar(JavaNetCookieJar(cookieManager))
            .connectTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
        builder.build()
    }

    internal fun <Api> createBaseErrorHandlers(
        sessionId: SessionId?,
        monoClockMs: () -> Long,
        networkMainScope: CoroutineScope
    ) = listOf(
        RefreshTokenHandler<Api>(
            sessionId,
            sessionProvider,
            sessionListener,
            monoClockMs,
            networkMainScope
        ),
        ProtonForceUpdateHandler(apiClient),
        HumanVerificationHandler(
            sessionId,
            sessionProvider,
            sessionListener,
            networkMainScope
        )
    )

    private val dohProvider by lazy {
        val dohServices = Constants.DOH_PROVIDERS_URLS.map { serviceUrl ->
            DnsOverHttpsProviderRFC8484(baseOkHttpClient, serviceUrl, apiClient, networkManager, logger)
        }
        DohProvider(baseUrl, apiClient, dohServices, mainScope, prefs, ::javaMonoClockMs)
    }

    private fun javaMonoClockMs(): Long =
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime())

    private fun javaWallClockMs(): Long =
        System.currentTimeMillis()
}

/**
 * Factory method for [NetworkManager] allowing tracking of connectivity changes.
 */
fun NetworkManager(context: Context): NetworkManager =
    NetworkManagerImpl(context.applicationContext)

/**
 * Factory method to create persistent storage of preferences for network module.
 */
fun NetworkPrefs(context: Context): NetworkPrefs =
    NetworkPrefsImpl(context.applicationContext)
