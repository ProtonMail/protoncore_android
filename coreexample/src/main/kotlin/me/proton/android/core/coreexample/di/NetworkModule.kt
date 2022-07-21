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

package me.proton.android.core.coreexample.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.proton.android.core.coreexample.BuildConfig
import me.proton.android.core.coreexample.Constants
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.data.client.ClientIdProviderImpl
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.client.ClientVersionValidator
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.takeIfNotBlank
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Singleton
import me.proton.core.network.data.di.Constants as NetWorkDataConstants

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideClientIdProvider(@BaseApiUrl baseApiUrl: String, cookieStore: ProtonCookieStore): ClientIdProvider =
        ClientIdProviderImpl(baseApiUrl, cookieStore)

    @Provides
    @Singleton
    fun provideExtraHeaderProvider(): ExtraHeaderProvider = ExtraHeaderProviderImpl().apply {
        BuildConfig.PROXY_TOKEN?.takeIfNotBlank()?.let { addHeaders("X-atlas-secret" to it) }
    }

    @Provides
    @Singleton
    fun provideApiFactory(
        @ApplicationContext context: Context,
        apiClient: ApiClient,
        clientIdProvider: ClientIdProvider,
        serverTimeListener: ServerTimeListener,
        networkManager: NetworkManager,
        networkPrefs: NetworkPrefs,
        cookieStore: ProtonCookieStore,
        sessionProvider: SessionProvider,
        sessionListener: SessionListener,
        humanVerificationProvider: HumanVerificationProvider,
        humanVerificationListener: HumanVerificationListener,
        missingScopeListener: MissingScopeListener,
        extraHeaderProvider: ExtraHeaderProvider,
        clientVersionValidator: ClientVersionValidator,
        dohAlternativesListener: DohAlternativesListener? = null,
        @BaseApiUrl baseApiUrl: String,
        @DohProviderUrls dohProviderUrls: Array<String>,
        @CertificatePins certificatePins: Array<String>,
        @AlternativeApiPins alternativeApiPins: List<String>
    ): ApiManagerFactory {
        return ApiManagerFactory(
            baseApiUrl,
            apiClient,
            clientIdProvider,
            serverTimeListener,
            networkManager,
            networkPrefs,
            sessionProvider,
            sessionListener,
            humanVerificationProvider,
            humanVerificationListener,
            missingScopeListener,
            cookieStore,
            CoroutineScope(Job() + Dispatchers.Default),
            certificatePins,
            alternativeApiPins,
            cache = {
                Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = 10L * 1024L * 1024L // 10 MiB
                )
            },
            extraHeaderProvider = extraHeaderProvider,
            clientVersionValidator = clientVersionValidator,
            dohAlternativesListener = dohAlternativesListener,
            dohProviderUrls = dohProviderUrls
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
class NetworkConstantsModule {
    @BaseApiUrl
    @Provides
    fun provideBaseApiUrl(): String = Constants.BASE_URL

    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = NetWorkDataConstants.DOH_PROVIDERS_URLS

    @CertificatePins
    @Provides
    fun provideCertificatePins() = if (BuildConfig.USE_DEFAULT_PINS) {
        NetWorkDataConstants.DEFAULT_SPKI_PINS
    } else {
        emptyArray()
    }

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins() = if (BuildConfig.USE_DEFAULT_PINS) {
        NetWorkDataConstants.ALTERNATIVE_API_SPKI_PINS
    } else {
        emptyList()
    }
}

@Module
@InstallIn(SingletonComponent::class)
class NetworkCallbacksModule {
    @Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null

    @Provides
    @Singleton
    fun provideServerTimeListener(context: CryptoContext) = object : ServerTimeListener {
        override fun onServerTimeUpdated(epochSeconds: Long) {
            context.pgpCrypto.updateTime(epochSeconds)
        }
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindsModule {
    @Binds
    @Singleton
    abstract fun provideApiClient(coreExampleApiClient: CoreExampleApiClient): ApiClient
}
