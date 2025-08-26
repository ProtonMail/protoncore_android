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

package me.proton.core.configuration.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.FeatureFlagsConfiguration
import me.proton.core.featureflag.domain.FeatureFlagOverrider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public class ContentResolverEnvironmentConfigModule {
    @Provides
    @Singleton
    public fun provideEnvironmentConfig(
        contentResolverConfigManager: ContentResolverConfigManager
    ): EnvironmentConfiguration {
        val staticEnvironmentConfig = EnvironmentConfiguration.fromClass()
        val contentResolverConfigData =
            contentResolverConfigManager.queryAtClassPath(EnvironmentConfiguration::class)
        val featureFlagConfig =
            contentResolverConfigManager.queryAtClassPath(EnvironmentConfiguration::class)
        val combinedConfig: Map<String, Any?>? =
            (contentResolverConfigData.orEmpty() + featureFlagConfig.orEmpty())
                .takeIf { it.isNotEmpty() }
        return EnvironmentConfiguration.fromMap(combinedConfig ?: return staticEnvironmentConfig)
    }

    @Provides
    @Singleton
    public fun provideFeatureFlagOverrider(
        contentResolverConfigManager: ContentResolverConfigManager
    ): FeatureFlagOverrider {
        val configData = contentResolverConfigManager.queryAtClassPath(EnvironmentConfiguration::class)
        return FeatureFlagsConfiguration.fromMap(configData ?: emptyMap())
    }

    @Provides
    @Singleton
    public fun provideContentResolverConfigManager(@ApplicationContext context: Context): ContentResolverConfigManager =
        ContentResolverConfigManager(context)
}
