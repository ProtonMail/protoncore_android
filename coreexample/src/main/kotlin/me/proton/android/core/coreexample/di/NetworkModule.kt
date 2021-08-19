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
import me.proton.android.core.coreexample.Constants
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.network.data.ApiManagerFactory
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.NetworkManager
import me.proton.core.network.data.NetworkPrefs
import me.proton.core.network.data.ProtonCookieStore
import me.proton.core.network.data.client.ClientIdProviderImpl
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.server.ServerTimeListener
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.Logger
import okhttp3.Cache
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager =
        NetworkManager(context)

    @Provides
    @Singleton
    fun provideNetworkPrefs(@ApplicationContext context: Context) =
        NetworkPrefs(context)

    @Provides
    @Singleton
    fun provideProtonCookieStore(@ApplicationContext context: Context): ProtonCookieStore =
        ProtonCookieStore(context)

    @Provides
    @Singleton
    fun provideClientIdProvider(protonCookieStore: ProtonCookieStore): ClientIdProvider =
        ClientIdProviderImpl(Constants.BASE_URL, protonCookieStore)

    @Provides
    @Singleton
    fun provideServerTimeListener(context: CryptoContext) = object : ServerTimeListener {
        override fun onServerTimeUpdated(epochSeconds: Long) {
            context.pgpCrypto.updateTime(epochSeconds)
        }
    }

    @Provides
    @Singleton
    fun provideApiFactory(
        @ApplicationContext context: Context,
        logger: Logger,
        apiClient: ApiClient,
        clientIdProvider: ClientIdProvider,
        serverTimeListener: ServerTimeListener,
        networkManager: NetworkManager,
        networkPrefs: NetworkPrefs,
        protonCookieStore: ProtonCookieStore,
        sessionProvider: SessionProvider,
        sessionListener: SessionListener,
        humanVerificationProvider: HumanVerificationProvider,
        humanVerificationListener: HumanVerificationListener
    ): ApiManagerFactory = ApiManagerFactory(
        Constants.BASE_URL,
        apiClient,
        clientIdProvider,
        serverTimeListener,
        logger,
        networkManager,
        networkPrefs,
        sessionProvider,
        sessionListener,
        humanVerificationProvider,
        humanVerificationListener,
        protonCookieStore,
        CoroutineScope(Job() + Dispatchers.Default),
        emptyArray(), emptyList(),
        cache = Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 10L * 1024L * 1024L // 10 MiB
        )
    )

    @Provides
    @Singleton
    fun provideApiProvider(apiManagerFactory: ApiManagerFactory, sessionProvider: SessionProvider): ApiProvider =
        ApiProvider(apiManagerFactory, sessionProvider)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindsModule {
    @Binds
    abstract fun provideApiClient(coreExampleApiClient: CoreExampleApiClient): ApiClient
}
