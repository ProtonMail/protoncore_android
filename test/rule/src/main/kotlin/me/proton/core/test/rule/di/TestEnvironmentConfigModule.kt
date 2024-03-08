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

import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.dagger.ContentResolverEnvironmentConfigModule
import me.proton.core.configuration.extension.configContractFieldsMap
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ContentResolverEnvironmentConfigModule::class]
)
public object TestEnvironmentConfigModule {
    @Provides
    @Singleton
    public fun provideEnvironmentConfiguration(): EnvironmentConfiguration =
        InstrumentationRegistry
            .getArguments()
            .configFields()
            .takeIf { it.isNotEmpty() }
            ?.let {
                EnvironmentConfiguration.fromMap(it)
            } ?: EnvironmentConfiguration(::getConfigValue)


    public val overrideConfig: AtomicReference<EnvironmentConfiguration?> = AtomicReference(null)

    private val defaultConfig = EnvironmentConfiguration.fromClass()

    private fun getConfigValue(key: String): String {
        val defaultValue = defaultConfig.configContractFieldsMap[key].toString()
        val overrideValue = overrideConfig.get()?.configContractFieldsMap?.get(key)?.toString()
        return overrideValue ?: defaultValue
    }

    private fun Bundle.configFields(): Map<String, Any?> {
        val hostKey = EnvironmentConfiguration::host.name
        val proxyTokenKey = EnvironmentConfiguration::proxyToken.name
        return mapOf(
            hostKey to (getString(hostKey) ?: getConfigValue(hostKey)),
            proxyTokenKey to (getString(proxyTokenKey) ?: getConfigValue(proxyTokenKey))
        )
    }
}
