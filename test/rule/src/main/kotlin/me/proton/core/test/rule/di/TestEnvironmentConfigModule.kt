/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.test.rule.di

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.FeatureFlagsConfiguration
import me.proton.core.configuration.dagger.ContentResolverEnvironmentConfigModule
import me.proton.core.configuration.entity.ConfigContract
import me.proton.core.configuration.extension.primitiveFieldMap
import me.proton.core.featureflag.domain.FeatureFlagOverrider
import me.proton.core.test.rule.printInfo
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ContentResolverEnvironmentConfigModule::class]
)
public object TestEnvironmentConfigModule {

    public val overrideEnvironmentConfiguration: AtomicReference<EnvironmentConfiguration?> =
        AtomicReference(null)

    private val instrumentationArgumentsConfig by lazy {
        InstrumentationRegistry
            .getArguments()
            .takeIf { !it.isEmpty }
            ?.let { args ->
                if (!args.containsKey(ConfigContract::host.name)) {
                    printInfo(
                        "Instrumentation arguments fetched, but 'host' key is not present. " +
                                "Skipping EnvironmentConfiguration override."
                    )
                    return@let null
                }

                EnvironmentConfiguration.fromBundle(args).also {
                    printInfo(
                        "Overriding EnvironmentConfiguration with Instrumentation " +
                                "arguments: ${it.primitiveFieldMap}"
                    )
                }
            }
    }

    private val staticEnvironmentConfig by lazy(EnvironmentConfiguration::fromClass)

    @Provides
    @Singleton
    public fun provideContentResolverConfigManager(
        @ApplicationContext context: Context
    ): ContentResolverConfigManager =
        ContentResolverConfigManager(context)

    @Provides
    @Singleton
    public fun provideFeatureFlagOverrider(
        contentResolverConfigManager: ContentResolverConfigManager
    ): FeatureFlagOverrider {
        val configData =
            contentResolverConfigManager.queryAtClassPath(EnvironmentConfiguration::class)
        return FeatureFlagsConfiguration.fromMap(configData ?: emptyMap())
    }

    @Provides
    @Singleton
    public fun provideEnvironmentConfiguration(
        contentResolverConfigManager: ContentResolverConfigManager
    ): EnvironmentConfiguration {
        val contentResolverConfig = contentResolverConfigManager
            .queryAtClassPath(EnvironmentConfiguration::class)
            ?.let(EnvironmentConfiguration::fromMap)

        return overrideEnvironmentConfiguration.get()
            ?: instrumentationArgumentsConfig
            ?: contentResolverConfig
            ?: staticEnvironmentConfig
    }
}
