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
package me.proton.core.network.data

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.plus
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.doh.DnsOverHttpsProviderRFC8484
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiErrorHandler
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.ApiManagerImpl
import me.proton.core.network.domain.DohApiHandler
import me.proton.core.network.domain.DohProvider
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.handlers.ApiConnectionHandler
import me.proton.core.network.domain.handlers.HumanVerificationInvalidHandler
import me.proton.core.network.domain.handlers.HumanVerificationNeededHandler
import me.proton.core.network.domain.handlers.MissingScopeHandler
import me.proton.core.network.domain.handlers.ProtonForceUpdateHandler
import me.proton.core.network.domain.handlers.RefreshTokenHandler
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.serverconnection.ApiConnectionListener
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.ProtonCoreConfig
import okhttp3.Cache
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
 * @param cookieStore The cookie store. If set to null, a default InMemory cookie store will be used. Otherwise, for
 * permanent Cookie Store please use instance of [ProtonCookieStore].
 * @param cache [Cache] shared across all user, session, api or call.
 */
class ApiManagerFactory(
    private val baseUrl: String,
    private val apiClient: ApiClient,
    private val clientIdProvider: ClientIdProvider,
    private val serverTimeListener: ServerTimeListener,
    private val networkManager: NetworkManager,
    private val prefs: NetworkPrefs,
    private val sessionProvider: SessionProvider,
    private val sessionListener: SessionListener,
    private val humanVerificationProvider: HumanVerificationProvider,
    private val humanVerificationListener: HumanVerificationListener,
    private val missingScopeListener: MissingScopeListener,
    private val cookieStore: ProtonCookieStore?,
    scope: CoroutineScope,
    private val certificatePins: Array<String> = Constants.DEFAULT_SPKI_PINS,
    private val alternativeApiPins: List<String> = Constants.ALTERNATIVE_API_SPKI_PINS,
    private val cache: () -> Cache? = { null },
    private val extraHeaderProvider: ExtraHeaderProvider? = null,
    private val apiConnectionListener: ApiConnectionListener?
) {

    @OptIn(ObsoleteCoroutinesApi::class)
    private val mainScope = scope + newSingleThreadContext("core.network.main")

    init {
        requireNotNull(URI(baseUrl).host)
    }

    internal val jsonConverter = ProtonCoreConfig
        .defaultJsonStringFormat
        .asConverterFactory("application/json".toMediaType())

    @VisibleForTesting
    val baseOkHttpClient by lazy {
        require(apiClient.timeoutSeconds >= ApiClient.MIN_TIMEOUT_SECONDS) {
            "Minimum timeout for ApiClient is ${ApiClient.MIN_TIMEOUT_SECONDS} seconds."
        }
        val builder = OkHttpClient.Builder()
            .cache(cache())
            .connectTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(apiClient.timeoutSeconds, TimeUnit.SECONDS)

        if (cookieStore != null) {
            val cookieManager = CookieManager(
                cookieStore,
                CookiePolicy.ACCEPT_ALL
            )
            CookieManager.setDefault(cookieManager)
            builder.cookieJar(JavaNetCookieJar(cookieManager))
        }
        builder.build()
    }

    private val dohProvider by lazy {
        val dohServices = Constants.DOH_PROVIDERS_URLS.map { serviceUrl ->
            DnsOverHttpsProviderRFC8484({ baseOkHttpClient }, serviceUrl, apiClient, networkManager)
        }
        DohProvider(baseUrl, apiClient, dohServices, mainScope, prefs, ::javaMonoClockMs)
    }

    private fun javaMonoClockMs(): Long = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
    private fun javaWallClockMs(): Long = System.currentTimeMillis()

    internal fun <Api> createBaseErrorHandlers(
        sessionId: SessionId?,
        monoClockMs: () -> Long,
        dohApiHandler: DohApiHandler<Api>,
    ): List<ApiErrorHandler<Api>> {
        val refreshTokenHandler = RefreshTokenHandler<Api>(sessionId, sessionProvider, sessionListener, monoClockMs)
        val missingScopeHandler =
            MissingScopeHandler<Api>(sessionId, sessionProvider, missingScopeListener)
        val forceUpdateHandler = ProtonForceUpdateHandler<Api>(apiClient)
        val serverConnectionHandler = ApiConnectionHandler<Api>(apiConnectionListener)
        val humanVerificationNeededHandler =
            HumanVerificationNeededHandler<Api>(sessionId, clientIdProvider, humanVerificationListener, monoClockMs)
        val humanVerificationInvalidHandler =
            HumanVerificationInvalidHandler<Api>(sessionId, clientIdProvider, humanVerificationListener)

        return listOf(
            dohApiHandler,
            serverConnectionHandler,
            missingScopeHandler,
            refreshTokenHandler,
            forceUpdateHandler,
            humanVerificationInvalidHandler,
            humanVerificationNeededHandler,

        )
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
        certificatePins: Array<String> = this@ApiManagerFactory.certificatePins,
        alternativeApiPins: List<String> = this@ApiManagerFactory.alternativeApiPins
    ): ApiManager<Api> {
        val pinningStrategy = { builder: OkHttpClient.Builder ->
            initPinning(builder, URI(baseUrl).host, certificatePins)
        }
        val primaryBackend = ProtonApiBackend(
            baseUrl,
            apiClient,
            clientIdProvider,
            serverTimeListener,
            sessionId,
            sessionProvider,
            humanVerificationProvider,
            { baseOkHttpClient },
            listOf(jsonConverter),
            interfaceClass,
            networkManager,
            pinningStrategy,
            ::javaWallClockMs,
            prefs,
            cookieStore,
            extraHeaderProvider,
        )

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
                clientIdProvider,
                serverTimeListener,
                sessionId,
                sessionProvider,
                humanVerificationProvider,
                { baseOkHttpClient },
                listOf(jsonConverter),
                interfaceClass,
                networkManager,
                alternativePinningStrategy,
                ::javaWallClockMs,
                prefs,
                cookieStore,
                extraHeaderProvider,
            )
        }
        val errorHandlers = createBaseErrorHandlers(sessionId, ::javaMonoClockMs, dohApiHandler) + clientErrorHandlers

        return ApiManagerImpl(
            apiClient, primaryBackend, errorHandlers, ::javaMonoClockMs
        )
    }
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
