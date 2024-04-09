/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.configuration.configurator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.configuration.configurator.domain.EnvironmentConfigurationUseCase
import me.proton.core.configuration.configurator.entity.AppConfig
import me.proton.core.test.quark.v2.QuarkCommand
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.time.toJavaDuration

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideQuarkCommand(client: OkHttpClient): QuarkCommand = QuarkCommand(client)

    @Singleton
    @Provides
    fun provideOkHttpClient(appConfig: AppConfig): OkHttpClient {
        val clientTimeout = appConfig.quarkTimeout.toJavaDuration()
        return OkHttpClient.Builder().connectTimeout(clientTimeout)
            .readTimeout(clientTimeout)
            .writeTimeout(clientTimeout)
            .callTimeout(clientTimeout)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Singleton
    @Provides
    fun provideContentResolverConfigurationUseCase(
        contentResolverConfigManager: ContentResolverConfigManager,
        appConfig: AppConfig,
        quark: QuarkCommand
    ): ConfigurationUseCase = EnvironmentConfigurationUseCase(quark, contentResolverConfigManager, appConfig)
}
