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

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.network.data.client.ExtraHeaderProviderImpl
import me.proton.core.network.data.di.AlternativeApiPins
import me.proton.core.network.data.di.BaseProtonApiUrl
import me.proton.core.network.data.di.CertificatePins
import me.proton.core.network.data.di.Constants
import me.proton.core.network.data.di.DohProviderUrls
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.util.kotlin.takeIfNotBlank
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton
import me.proton.core.network.data.di.Constants as NetWorkDataConstants

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
    @Provides
    @Singleton
    fun provideExtraHeaderProvider(envConfig: EnvironmentConfiguration): ExtraHeaderProvider =
        ExtraHeaderProviderImpl().apply {
            envConfig.proxyToken.takeIfNotBlank()?.let { addHeaders("X-atlas-secret" to it) }
        }
}

@Module
@InstallIn(SingletonComponent::class)
class NetworkConstantsModule {

    @Provides
    @BaseProtonApiUrl
    fun provideProtonApiUrl(envConfig: EnvironmentConfiguration): HttpUrl = envConfig.baseUrl.toHttpUrl()

    @DohProviderUrls
    @Provides
    fun provideDohProviderUrls(): Array<String> = NetWorkDataConstants.DOH_PROVIDERS_URLS

    @CertificatePins
    @Provides
    fun provideCertificatePins(envConfig: EnvironmentConfiguration) =
        if (envConfig.useDefaultPins) Constants.DEFAULT_SPKI_PINS else emptyArray()

    @AlternativeApiPins
    @Provides
    fun provideAlternativeApiPins(envConfig: EnvironmentConfiguration) =
        if (envConfig.useDefaultPins) Constants.ALTERNATIVE_API_SPKI_PINS else emptyList()
}

@Module
@InstallIn(SingletonComponent::class)
class NetworkCallbacksModule {
    @Provides
    @Singleton
    fun provideDohAlternativesListener(): DohAlternativesListener? = null
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindsModule {
    @Binds
    @Singleton
    abstract fun provideApiClient(coreExampleApiClient: CoreExampleApiClient): ApiClient
}
